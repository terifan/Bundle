package org.terifan.bundle2;

import java.io.IOException;
import java.io.InputStream;
import org.terifan.bundle.BitInputStream;
import static org.terifan.bundle2.BundleConstants.*;
import org.terifan.bundle2.BundleX.BooleanArray;
import org.terifan.bundle2.BundleX.BundleArray;
import org.terifan.bundle2.BundleX.BundleArrayType;
import org.terifan.bundle2.BundleX.NumberArray;
import org.terifan.bundle2.BundleX.StringArray;


public class BinaryDecoderX
{
	private BitInputStream mInput;


	public BundleX unmarshal(InputStream aInputStream) throws IOException
	{
		mInput = new BitInputStream(aInputStream);

		int version = mInput.readVar32();
		if (version != VERSION)
		{
			throw new IllegalArgumentException("Unsupported version");
		}

//		PathEvaluation path = new PathEvaluation("colors", 1);
		PathEvaluation path = new PathEvaluation();

		return readBundle(path);
	}


	private BundleX readBundle(PathEvaluation aPathEvaluation) throws IOException
	{
		BundleX bundle = new BundleX();

		int keyCount = mInput.readVar32();

		for (int i = 0; i < keyCount; i++)
		{
			int header = mInput.readVar32S();
			byte[] buf = new byte[Math.abs(header)];

			if (mInput.read(buf) != buf.length)
			{
				throw new IOException("Unexpected end of stream");
			}

			String key = UTF8.decodeUTF8(buf);
			Object value = null;

			boolean valid = aPathEvaluation.valid(key);

			if (header >= 0)
			{
				int size = mInput.readVar32();

				if (valid)
				{
					value = readValue(aPathEvaluation.next(key), null);
				}
				else
				{
					mInput.skipBits(8 * size);
				}
			}

			if (valid)
			{
				bundle.put(key, value);
			}
		}

		return bundle;
	}


	private Object readSequence(BundleArrayType aSequence, PathEvaluation aPathEvaluation, Integer aType) throws IOException
	{
		int elementCount = mInput.readVar32S();
		boolean[] notNull = null;

		if (elementCount < 0)
		{
			elementCount = -elementCount;
			notNull = new boolean[elementCount];

			for (int i = 0; i < elementCount; i++)
			{
				notNull[i] = mInput.readBit() == 0;
			}

			mInput.align();
		}

		for (int i = 0; i < elementCount; i++)
		{
			Object value = null;
			boolean valid = aPathEvaluation.valid(i);

			if (valid)
			{
				if (notNull == null || notNull[i])
				{
					value = readValue(aPathEvaluation.next(i), aType);
				}

				aSequence.add(value);
			}
		}

		return aSequence;
	}


	private Object readBundleArray(BundleArray aSequence, PathEvaluation aPathEvaluation) throws IOException
	{
		int elementCount = mInput.readVar32();

		for (int i = 0; i < elementCount; i++)
		{
			BundleX value = null;
			boolean valid = aPathEvaluation.valid(i);

			int size = mInput.readVar32();

			if (size > 0)
			{
				if (valid)
				{
					value = (BundleX)readValue(aPathEvaluation.next(i), BUNDLE);
				}
				else
				{
					mInput.skipBits(8 * (size - 1));
				}
			}

			if (valid)
			{
				aSequence.add(value);
			}
		}

		return aSequence;
	}


	private Object readValue(PathEvaluation aPathEvaluation, Integer aType) throws IOException
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
				return Float.intBitsToFloat(mInput.readVar32S());
			case DOUBLE:
				return Double.longBitsToDouble(mInput.readVar64S());
			case STRING:
				return readUTF();
			case BUNDLE:
				return readBundle(aPathEvaluation);
			case BOOLEANARRAY:
				return readSequence(new BooleanArray(), aPathEvaluation, BOOLEAN);
			case NUMBERARRAY:
				return readSequence(new NumberArray(), aPathEvaluation, null);
			case STRINGARRAY:
				return readSequence(new StringArray(), aPathEvaluation, STRING);
			case BUNDLEARRAY:
				return readBundleArray(new BundleArray(), aPathEvaluation);
			default:
				throw new IOException("Unsupported field type: " + aType);
		}
	}


	private String readUTF() throws IOException
	{
		byte[] buf = new byte[mInput.readVar32()];

		if (mInput.read(buf) != buf.length)
		{
			throw new IOException("Unexpected end of stream");
		}

		return UTF8.decodeUTF8(buf);
	}


	private static class PathEvaluation
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
