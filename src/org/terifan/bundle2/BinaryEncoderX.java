package org.terifan.bundle2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.terifan.bundle.BitOutputStream;
import static org.terifan.bundle2.BundleConstants.TYPES;
import static org.terifan.bundle2.BundleConstants.VERSION;
import org.terifan.bundle2.BundleX.BundleArray;
import org.terifan.bundle2.BundleX.BundleArrayType;


public class BinaryEncoderX
{
	public void marshal(BundleX aBundle, OutputStream aOutputStream) throws IOException
	{
		BitOutputStream output = new BitOutputStream(aOutputStream);

		output.writeVar32(VERSION);

		output.write(writeBundle(aBundle));

		output.finish();
	}


	private byte[] writeBundle(BundleX aBundle) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(baos);

		output.writeVar32(aBundle.size());

		for (String key : aBundle.keySet())
		{
			Object value = aBundle.get(key);

			byte[] buf = UTF8.encodeUTF8(key);
			output.writeVar32S(value == null ? -buf.length : buf.length);
			output.write(buf);

			if (value != null)
			{
				byte[] data = writeValue(value);
				output.writeVar32(data.length);
				output.write(data);
			}
		}

		output.finish();

		return baos.toByteArray();
	}


	private void writeSequence(BundleArrayType aSequence, BitOutputStream aOutput) throws IOException
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

		aOutput.writeVar32S(hasNull ? -aSequence.size() : aSequence.size());

		if (hasNull)
		{
			for (int i = 0; i < aSequence.size(); i++)
			{
				aOutput.writeBit(aSequence.get(i) == null);
			}

			aOutput.align();
		}

		for (int i = 0; i < aSequence.size(); i++)
		{
			Object value = aSequence.get(i);

			if (value != null)
			{
				byte[] data = writeValue(value);
				aOutput.write(data);
			}
		}
	}


	private void writeBundleArray(BundleArray aSequence, BitOutputStream aOutput) throws IOException
	{
		aOutput.writeVar32(aSequence.size());

		for (int i = 0; i < aSequence.size(); i++)
		{
			Object value = aSequence.get(i);

			if (value != null)
			{
				byte[] data = writeValue(value);
				aOutput.writeVar32(1 + data.length);
				aOutput.write(data);
			}
			else
			{
				aOutput.writeVar32(0);
			}
		}
	}


	private byte[] writeValue(Object aValue) throws IOException
	{
		int type = TYPES.get(aValue.getClass());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(baos);

		output.writeBits(type, 8);

		switch (type)
		{
			case 0:
				output.writeBits((Boolean)aValue ? 1 : 0, 8);
				break;
			case 1:
				output.writeBits(0xff & (Byte)aValue, 8);
				break;
			case 2:
				output.writeVar32S((Short)aValue);
				break;
			case 3:
				output.writeVar32S((Integer)aValue);
				break;
			case 4:
				output.writeVar64S((Long)aValue);
				break;
			case 5:
				output.writeVar32S(Float.floatToIntBits((Float)aValue));
				break;
			case 6:
				output.writeVar64S(Double.doubleToLongBits((Double)aValue));
				break;
			case 7:
				byte[] buf = UTF8.encodeUTF8((String)aValue);
				output.writeVar32(buf.length);
				output.write(buf);
				break;
			case 8:
				output.write(writeBundle((BundleX)aValue));
				break;
			case 9:
			case 10:
			case 11:
				writeSequence((BundleArrayType)aValue, output);
				break;
			case 12:
				writeBundleArray((BundleArray)aValue, output);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}

		output.finish();

		return baos.toByteArray();
	}
}
