package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;
import static org.terifan.bundle.BinaryConstants.*;


class BinaryDecoder
{
	private VLCInputStream mInput;


	public Container unmarshal(InputStream aInputStream, PathEvaluation aPath, Container aContainer) throws IOException
	{
		try (VLCInputStream in = new VLCInputStream(aInputStream))
		{
			mInput = in;

			int header = mInput.readVar32();
			int version = header & VERSION_MASK;

			if (version != VERSION)
			{
				throw new IllegalArgumentException("Unsupported version");
			}

			long length = mInput.readVar32();

			switch (header & CONTAINER_MASK)
			{
				case CONTAINER_BUNDLE:
					return readBundle(aPath, (Bundle)aContainer);
				case  CONTAINER_ARRAY:
					return readArray(aPath, (Array)aContainer);
				default:
					throw new IllegalArgumentException("Unsupported container type.");
			}
		}
	}


	private Bundle readBundle(PathEvaluation aPathEvaluation, Bundle bundle) throws IOException
	{
		int keyCount = mInput.readVar32();

		for (int i = 0; i < keyCount; i++)
		{
			String key = UTF8.decodeUTF8Z(mInput);

			boolean valid = aPathEvaluation.valid(key);

			Object value = readValue(aPathEvaluation.next(key), null, valid);

			if (valid)
			{
				bundle.set(key, value);
			}
		}

		return bundle;
	}


	private Array readArray(PathEvaluation aPathEvaluation, Array aArray) throws IOException
	{
		int header = mInput.readVar32();
		boolean hasSingleType = (header & 0x01) != 0;
		boolean hasNull = (header & 0x02) != 0;
		Integer singleType = hasSingleType ? (header >> 2) & 0x1f : null;
		int elementCount = header >> (hasSingleType ? 2 + 5 : 2);

		for (int i = 0; i < elementCount; i+=8)
		{
			int nullBits = 0;

			if (hasNull)
			{
				nullBits = mInput.readInt8();
			}

			for (int j = 0; j < 8 && i+j < elementCount; j++)
			{
				boolean valid = aPathEvaluation.valid(i+j);
				Object value = null;

				if (!hasNull || (nullBits & (1 << j)) == 0)
				{
					value = readValue(aPathEvaluation.next(i), singleType, valid);
				}

				if (valid)
				{
					// Add the value without any processing. (The varargs parameter of the normal 'add' method will turn binary chunks
					// into an array of bytes.)
					aArray.addImpl(value);
				}
			}
		}

		return aArray;
	}


	private Object readValue(PathEvaluation aPathEvaluation, Integer aType, boolean aValid) throws IOException
	{
		if (aType == null)
		{
			aType = mInput.readInt8();
		}

		switch (aType)
		{
			case NULL:
				return null;
			case BOOLEAN:
				return mInput.readInt8() == 1;
			case BYTE:
				return (byte)mInput.readInt8();
			case SHORT:
				return (short)mInput.readVar32S();
			case INT:
				return mInput.readVar32S();
			case LONG:
				return mInput.readVar64S();
			case FLOAT:
				return Float.intBitsToFloat(mInput.readInt32());
			case DOUBLE:
				return Double.longBitsToDouble(mInput.readInt64());
			case STRING:
			case BUNDLE:
			case ARRAY:
			case BINARY:
				int len = mInput.readVar32();
				if (!aValid)
				{
					mInput.skip(len);
					return null;
				}

				switch (aType)
				{
					case STRING:
						return UTF8.decodeUTF8(mInput.read(new byte[len]));
					case BUNDLE:
						return readBundle(aPathEvaluation, new Bundle());
					case ARRAY:
						return readArray(aPathEvaluation, new Array());
					case BINARY:
						return mInput.read(new byte[len]);
					default:
						throw new IOException("Unsupported field type: " + aType);
				}
			default:
				throw new IOException("Unsupported field type: " + aType);
		}
	}
}
