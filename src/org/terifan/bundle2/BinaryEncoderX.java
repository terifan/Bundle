package org.terifan.bundle2;

import java.io.IOException;
import java.io.OutputStream;
import org.terifan.bundle.BitOutputStream;
import static org.terifan.bundle2.BundleConstants.TYPES;
import static org.terifan.bundle2.BundleConstants.VERSION;
import org.terifan.bundle2.BundleX.BundleArrayType;


public class BinaryEncoderX
{
	private BitOutputStream mOutput;


	public void marshal(BundleX aBundle, OutputStream aOutputStream) throws IOException
	{
		mOutput = new BitOutputStream(aOutputStream);

		mOutput.writeVar32(VERSION);

		writeBundle(aBundle);

		mOutput.finish();
		mOutput = null;
	}


	private void writeBundle(BundleX aBundle) throws IOException
	{
		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);
		boolean hasNull = false;

		for (String key : keys)
		{
			if (aBundle.get(key) == null)
			{
				hasNull = true;
				break;
			}
		}

		mOutput.writeVar32S(hasNull ? -aBundle.size() : aBundle.size());

		if (hasNull)
		{
			for (String key : keys)
			{
				mOutput.writeBit(aBundle.get(key) == null);
			}

			mOutput.align();
		}

		for (String key : keys)
		{
			writeUTF(key);

			Object value = aBundle.get(key);
			if (value != null)
			{
				writeValue(value);
			}
		}
	}


	private void writeSequence(BundleArrayType aSequence) throws IOException
	{
		boolean hasNull = false;

		for (int i = 0; i < aSequence.size(); i++)
		{
			if (aSequence.get(i) == null)
			{
				hasNull = true;
				break;
			}
		}

		mOutput.writeVar32S(hasNull ? -aSequence.size() : aSequence.size());

		if (hasNull)
		{
			for (int i = 0; i < aSequence.size(); i++)
			{
				mOutput.writeBit(aSequence.get(i) == null);
			}

			mOutput.align();
		}

		for (int i = 0; i < aSequence.size(); i++)
		{
			Object value = aSequence.get(i);

			if (value != null)
			{
				writeValue(value);
			}
		}
	}


	private void writeValue(Object aValue) throws IOException
	{
		int type = TYPES.get(aValue.getClass());

		mOutput.writeBits(type, 4);

		switch (type)
		{
			case 0:
				mOutput.writeBit((Boolean)aValue);
				break;
			case 1:
				mOutput.writeBits(0xff & (Byte)aValue, 8);
				break;
			case 2:
				mOutput.writeVar32S((Short)aValue);
				break;
			case 3:
				mOutput.writeVar32S((Integer)aValue);
				break;
			case 4:
				mOutput.writeVar64S((Long)aValue);
				break;
			case 5:
				mOutput.writeVar32S(Float.floatToIntBits((Float)aValue));
				break;
			case 6:
				mOutput.writeVar64S(Double.doubleToLongBits((Double)aValue));
				break;
			case 7:
				mOutput.align();
				writeUTF((String)aValue);
				break;
			case 8:
				mOutput.align();
				writeBundle((BundleX)aValue);
				break;
			case 9:
			case 10:
			case 11:
			case 12:
				mOutput.align();
				writeSequence((BundleArrayType)aValue);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}
	}


	private void writeUTF(String aKey) throws IOException
	{
		byte[] buf = UTF8.encodeUTF8(aKey);
		mOutput.writeVar32(buf.length);
		mOutput.write(buf);
	}
}
