package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.terifan.bundle.BundleConstants.*;
import org.terifan.bundle.Bundle.BundleArray;


public class BinaryEncoder
{
	public byte[] marshal(Bundle aBundle) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		BitOutputStream output = new BitOutputStream(baos);
		output.writeVar32(VERSION);
		output.write(writeBundle(aBundle));
		output.finish();

		return baos.toByteArray();
	}


	private byte[] writeBundle(Bundle aBundle) throws IOException
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


	private void writeSequence(BundleArray aSequence, BitOutputStream aOutput) throws IOException
	{
		boolean singleType = true;
		boolean hasNull = false;
		Class type = null;

		for (int i = 0; singleType && i < aSequence.size(); i++)
		{
			if (aSequence.get(i) == null)
			{
				hasNull = true;
			}
			else
			{
				if (type == null)
				{
					type = aSequence.get(0).getClass();
				}
				singleType = type == aSequence.get(i).getClass();
			}
		}

		boolean hasChunckSize = TYPES.get(type) >= STRING;

		aOutput.writeVar32((aSequence.size() << 2) + (hasNull ? 2 : 0) + (singleType ? 1 : 0));

		if (singleType)
		{
			aOutput.writeBits(TYPES.get(type), 8);
		}

		for (int i = 0; i < aSequence.size(); i++)
		{
			Object value = aSequence.get(i);

			if (value != null)
			{
				byte[] data = writeValue(value, !singleType);
				if (hasChunckSize)
				{
					aOutput.writeVar32(1 + data.length);
				}
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(baos);

		int type = TYPES.get(aValue.getClass());

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
				output.write(writeBundle((Bundle)aValue));
				break;
			case ARRAY:
				writeSequence((BundleArray)aValue, output);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}

		output.finish();

		return baos.toByteArray();
	}
}
