package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;
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
			int header = mInput.readVar32S();

			byte[] buf = new byte[Math.abs(header)];
			mInput.read(buf);
			String key = UTF8.decodeUTF8(buf);

			Object value = null;

			boolean valid = aPathEvaluation.valid(key);

			if (header >= 0)
			{
				int size = mInput.readVar32();

				if (valid)
				{
					value = readValue(aPathEvaluation.next(key));
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


	private Object readArray(BundleArray aSequence, PathEvaluation aPathEvaluation) throws IOException
	{
		int elementCount = mInput.readVar32();

		for (int i = 0; i < elementCount; i++)
		{
			Object value = null;
			boolean valid = aPathEvaluation.valid(i);

			int size = mInput.readVar32();

			if (size > 0)
			{
				if (valid)
				{
					value = readValue(aPathEvaluation.next(i));
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


	private Object readValue(PathEvaluation aPathEvaluation) throws IOException
	{
		int type = mInput.readBits(8);

		switch (type)
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
				byte[] buf = new byte[mInput.readVar32()];
				mInput.read(buf);
				return UTF8.decodeUTF8(buf);
			case BUNDLE:
				return readBundle(aPathEvaluation, new Bundle());
			case ARRAY:
				return readArray(new BundleArray(), aPathEvaluation);
			default:
				throw new IOException("Unsupported field type: " + type);
		}
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
