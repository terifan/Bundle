package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import static org.terifan.bundle.BundleConstants.*;
import org.terifan.bundle.Bundle.BundleArray;


public class BinaryDecoder
{
	private BitInputStream mInput;


	public Bundle unmarshal(InputStream aInputStream, PathEvaluation aPath, Bundle aBundle) throws IOException
	{
		mInput = new BitInputStream(aInputStream);

		int version = mInput.readVar32();
		if (version != VERSION)
		{
			throw new IllegalArgumentException("Unsupported version");
		}

		return readBundle(aPath, aBundle);
	}


	private Bundle readBundle(PathEvaluation aPathEvaluation, Bundle bundle) throws IOException
	{
		int keyCount = mInput.readVar32();

		for (int i = 0; i < keyCount; i++)
		{
			int keyLen = mInput.readVar32S();

			byte[] buf = readBytes(Math.abs(keyLen));
			String key = UTF8.decodeUTF8(buf);

			Object value = null;

			boolean valid = aPathEvaluation.valid(key);

			if (keyLen >= 0)
			{
				value = readValue(aPathEvaluation.next(key), null, valid);
			}

			if (valid)
			{
				bundle.put(key, value);
			}
		}

		return bundle;
	}


	private Object readArray(BundleArray aSequence, PathEvaluation aPathEvaluation) throws IOException
	{
		int header = mInput.readVar32();
		boolean singleType = (header & 1) != 0;
		boolean hasNull = (header & 2) != 0;
		Integer type = singleType ? (header >> 2) & 15 : null;
		int elementCount = header >> (singleType ? 2+4 : 2);
		
		for (int i = 0; i < elementCount; i+=8)
		{
			int nullBits = 0;

			if (hasNull)
			{
				nullBits = mInput.readBits(8);
			}

			for (int j = 0; j < 8 && i+j < elementCount; j++)
			{
				boolean valid = aPathEvaluation.valid(i+j);
				Object value = null;

				if (!hasNull || (nullBits & (1 << j)) == 0)
				{
					value = readValue(aPathEvaluation.next(i), type, valid);
				}

				if (valid)
				{
					aSequence.add(value);
				}
			}
		}

		return aSequence;
	}


	private Object readValue(PathEvaluation aPathEvaluation, Integer aType, boolean aValid) throws IOException
	{
		if (aType == null)
		{
			aType = mInput.readBits(8);
		}

		switch (aType)
		{
			case BOOLEAN:
				return mInput.readBits(8) == 1;
			case BYTE:
				return (byte)mInput.readBits(8);
			case SHORT:
				return (short)mInput.readVar32S();
			case INT:
				return mInput.readVar32S();
			case LONG:
				return mInput.readVar64S();
			case FLOAT:
				return Float.intBitsToFloat(mInput.read32());
			case DOUBLE:
				return Double.longBitsToDouble(mInput.read64());
			case DATE:
				return new Date(mInput.read64());
			case UUID:
				return new UUID(mInput.read64(), mInput.read64());
			case CALENDAR:
				TimeZone timeZone = TimeZone.getDefault();
				timeZone.setRawOffset(mInput.readVar32());
				Calendar c = Calendar.getInstance(timeZone);
				c.setTimeInMillis(mInput.read64());
				return c;
			case STRING:
			case BUNDLE:
			case ARRAY:
			case BINARY:
				int len = mInput.readVar32();
				if (!aValid)
				{
					mInput.skipBits(8 * len);
					return null;
				}

				switch (aType)
				{
					case STRING:
						return UTF8.decodeUTF8(readBytes(len));
					case BUNDLE:
						return readBundle(aPathEvaluation, new Bundle());
					case ARRAY:
						return readArray(new BundleArray(), aPathEvaluation);
					case BINARY:
						return readBytes(len);
					default:
						throw new IOException("Unsupported field type: " + aType);
				}
			default:
				throw new IOException("Unsupported field type: " + aType);
		}
	}


	private String readVarString() throws IOException
	{
		return UTF8.decodeUTF8(readBytes(mInput.readVar32()));
	}


	private byte[] readBytes(int aLen) throws IOException
	{
		byte[] buf = new byte[aLen];
		mInput.read(buf);
		return buf;
	}


	public static class PathEvaluation
	{
		private Object[] mPath;
		private int mOffset;


		public PathEvaluation(Object... aPath)
		{
			mPath = aPath;
		}


		private PathEvaluation(int aOffset, Object[] aPath)
		{
			mOffset = aOffset;
			mPath = aPath;
		}


		private boolean valid(Object aKey)
		{
			return mOffset >= mPath.length || mPath[mOffset].equals(aKey);
		}


		private PathEvaluation next(Object aKey)
		{
			return new PathEvaluation(mOffset + 1, mPath);
		}
	}
}
