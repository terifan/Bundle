package org.terifan.bundle2;

import java.io.IOException;
import java.io.InputStream;
import org.terifan.bundle.BitInputStream;
import static org.terifan.bundle2.BundleConstants.*;
import org.terifan.bundle2.BundleX.BundleArray;


public class BinaryDecoderX
{
	private BitInputStream mInput;


	public BundleX unmarshal(InputStream aInputStream, PathEvaluation aPath) throws IOException
	{
		mInput = new BitInputStream(aInputStream);

		int version = mInput.readVar32();
		if (version != VERSION)
		{
			throw new IllegalArgumentException("Unsupported version");
		}

		return readBundle(aPath);
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
				return readUTF();
			case BUNDLE:
				return readBundle(aPathEvaluation);
			case ARRAY:
				return readArray(new BundleArray(), aPathEvaluation);
			default:
				throw new IOException("Unsupported field type: " + type);
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
