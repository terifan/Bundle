package org.terifan.bundle2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.terifan.bundle.BitOutputStream;
import static org.terifan.bundle2.BundleConstants.*;
import org.terifan.bundle2.BundleX.BundleArray;
import org.terifan.bundle2.BundleX.BundleArrayType;


public class BinaryEncoderX
{
	public byte[] marshal(BundleX aBundle) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		BitOutputStream output = new BitOutputStream(baos);
		output.writeVar32(VERSION);
		output.write(writeBundle(aBundle));
		output.finish();

		return baos.toByteArray();
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
				byte[] data = writeValue(value, true);
				output.writeVar32(data.length);
				output.write(data);
			}
		}

		output.finish();

		return baos.toByteArray();
	}


	private void writeSequence(BundleArrayType aSequence, BitOutputStream aOutput, boolean aIncludeType) throws IOException
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
				byte[] data = writeValue(value, aIncludeType);
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
				byte[] data = writeValue(value, false);
				aOutput.writeVar32(1 + data.length);
				aOutput.write(data);
			}
			else
			{
				aOutput.writeVar32(0);
			}
		}
	}


	private byte[] writeValue(Object aValue, boolean aIncludeType) throws IOException
	{
		int type = TYPES.get(aValue.getClass());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(baos);

		if (aIncludeType)
		{
			output.writeBits(type, 8);
		}

		switch (type)
		{
			case BOOLEAN:
				output.writeBits((Boolean)aValue ? 1 : 0, 8);
				break;
			case BYTE:
				output.writeBits(0xff & (Byte)aValue, 8);
				break;
			case SHORT:
				output.writeVar32S((Short)aValue);
				break;
			case INT:
				output.writeVar32S((Integer)aValue);
				break;
			case LONG:
				output.writeVar64S((Long)aValue);
				break;
			case FLOAT:
				output.writeVar32S(Float.floatToIntBits((Float)aValue));
				break;
			case DOUBLE:
				output.writeVar64S(Double.doubleToLongBits((Double)aValue));
				break;
			case STRING:
				byte[] buf = UTF8.encodeUTF8((String)aValue);
				output.writeVar32(buf.length);
				output.write(buf);
				break;
			case BUNDLE:
				output.write(writeBundle((BundleX)aValue));
				break;
			case BOOLEANARRAY:
				writeSequence((BundleArrayType)aValue, output, false);
				break;
			case NUMBERARRAY:
				writeSequence((BundleArrayType)aValue, output, true);
				break;
			case STRINGARRAY:
				writeSequence((BundleArrayType)aValue, output, false);
				break;
			case BUNDLEARRAY:
				writeBundleArray((BundleArray)aValue, output);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}

		output.finish();

		return baos.toByteArray();
	}
}
