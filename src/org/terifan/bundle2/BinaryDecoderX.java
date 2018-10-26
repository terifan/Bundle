package org.terifan.bundle2;

import org.terifan.bundle.*;
import java.io.IOException;
import java.io.InputStream;
import static org.terifan.bundle2.BundleConstants.VERSION;
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

		return readBundle();
	}


	private BundleX readBundle() throws IOException
	{
		BundleX bundle = new BundleX();

		int length = mInput.readVar32S();
		boolean[] notNull = null;

		if (length < 0)
		{
			length = -length;
			notNull = new boolean[length];

			for (int i = 0; i < length; i++)
			{
				notNull[i] = mInput.readBit() == 0;
			}

			mInput.align();
		}

		for (int i = 0; i < length; i++)
		{
			String key = readUTF();
			Object value = null;

			if (notNull == null || notNull[i])
			{
				value = readValue();
			}

			bundle.put(key, value);
		}

		return bundle;
	}


	private Object readSequence(BundleArrayType aSequence) throws IOException
	{
		int length = mInput.readVar32S();
		boolean[] notNull = null;

		if (length < 0)
		{
			length = -length;
			notNull = new boolean[length];

			for (int i = 0; i < length; i++)
			{
				notNull[i] = mInput.readBit() == 0;
			}

			mInput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object value = null;
			if (notNull == null || notNull[i])
			{
				value = readValue();
			}
			aSequence.add(value);
		}

		return aSequence;
	}


	private Object readValue() throws IOException
	{
		int type = mInput.readBits(4);

		switch (type)
		{
			case 0:
				return mInput.readBit() == 1;
			case 1:
				return (byte)mInput.readBits(8);
			case 2:
				return (short)mInput.readVar32S();
			case 3:
				return mInput.readVar32S();
			case 4:
				return mInput.readVar64S();
			case 5:
				return Float.intBitsToFloat(mInput.readVar32S());
			case 6:
				return Double.longBitsToDouble(mInput.readVar64S());
			case 7:
				mInput.align();
				return readUTF();
			case 8:
				mInput.align();
				return readBundle();
			case 9:
				mInput.align();
				return readSequence(new BooleanArray());
			case 10:
				mInput.align();
				return readSequence(new NumberArray());
			case 11:
				mInput.align();
				return readSequence(new StringArray());
			case 12:
				mInput.align();
				return readSequence(new BundleArray());
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

		return UTF8.decodeUTF8(buf, 0, buf.length);
	}
}
