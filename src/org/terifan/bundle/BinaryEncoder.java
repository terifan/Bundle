package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import static org.terifan.bundle.BundleConstants.*;


public class BinaryEncoder
{
	private PredictorCodec mCompressor;
	private PredictorCodec mCompressor2;


	public BinaryEncoder()
	{
	}


	public byte[] marshal(Container aContainer) throws IOException
	{
		String dic = "Sender Transaction Name Article Measure Order Line Location Transmission From Type instance Process Reason Contacts Sequence Color Group Owner Remark Text Receiver Identifier Information Domain Terms Shipment Service Level Standard Element Object";
		byte[] trainData = ((dic + dic.toLowerCase() + dic.toUpperCase()).replace(" ", "") + (dic + dic.toLowerCase() + dic.toUpperCase())).getBytes();

		mCompressor = new PredictorCodec(20).learn(new ByteArrayInputStream(trainData));
		mCompressor2 = new PredictorCodec(20).learn(new ByteArrayInputStream(trainData));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(baos);

		byte[] data;
		if (aContainer instanceof Bundle)
		{
			output.writeVar32(CONTAINER_BUNDLE | VERSION);

			data = writeBundle((Bundle)aContainer);
		}
		else
		{
			output.writeVar32(CONTAINER_ARRAY | VERSION);

			data = writeArray((Array)aContainer);
		}

		output.writeVar32(data.length);
		output.write(data);
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
			if (key == null)
			{
				throw new IllegalArgumentException("A Bundle key cannot be null.");
			}

			if (true)
			{
				mCompressor.compress(new ByteArrayInputStream((key + "\u0000").getBytes("utf-8")), output);
			}
			else
			{
				UTF8.encodeUTF8(key, output);
			}

			Object value = aBundle.get(key);

			output.write(writeValue(value, true));
		}

		output.finish();

		return baos.toByteArray();
	}


	private byte[] writeArray(Array aSequence) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(baos);

		int elementCount = aSequence.size();
		boolean singleType = true;
		boolean hasNull = false;
		Class type = null;

		for (Object value : aSequence)
		{
			if (value == null)
			{
				hasNull = true;
			}
			else if (type == null)
			{
				assertSupportedType(value);

				type = value.getClass();
			}
			else if (singleType)
			{
				assertSupportedType(value);

				singleType = type == value.getClass();
			}

			if (!singleType && hasNull)
			{
				break;
			}
		}

		output.writeVar32((elementCount << (singleType ? 2 + 5 : 2)) + (singleType ? TYPES.get(type) << 2 : 0) + (hasNull ? 2 : 0) + (singleType ? 1 : 0));

		for (int i = 0; i < elementCount; i+=8)
		{
			if (hasNull)
			{
				int nullBits = 0;
				for (int j = 0; j < 8 && i+j < elementCount; j++)
				{
					if (aSequence.get(i+j) == null)
					{
						nullBits |= 1 << j;
					}
				}
				output.writeBits(nullBits, 8);
			}

			for (int j = 0; j < 8 && i+j < elementCount; j++)
			{
				Object value = aSequence.get(i+j);

				if (value != null)
				{
					byte[] data = writeValue(value, !singleType);
					output.write(data);
				}
			}
		}

		output.finish();

		return baos.toByteArray();
	}


	private byte[] writeValue(Object aValue, boolean aIncludeType) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream output = new BitOutputStream(baos);

		int type = aValue == null ? NULL : TYPES.get(aValue.getClass());

		if (aIncludeType)
		{
			output.writeBits(type, 8);
		}

		switch (type)
		{
			case NULL:
				break;
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
				output.write32(Float.floatToIntBits((Float)aValue));
				break;
			case DOUBLE:
				output.write64(Double.doubleToLongBits((Double)aValue));
				break;
			case DATE:
				output.write64(((Date)aValue).getTime());
				break;
			case UUID:
				UUID uuid = (UUID)aValue;
				output.write64(uuid.getLeastSignificantBits());
				output.write64(uuid.getMostSignificantBits());
				break;
			case CALENDAR:
				Calendar c = (Calendar)aValue;
				output.writeVar32(c.getTimeZone().getRawOffset());
				output.write64(c.getTimeInMillis());
				break;
			case STRING:
				byte[] s = ((String)aValue).getBytes("utf-8");
				output.writeVar32(s.length);
				mCompressor2.compress(new ByteArrayInputStream(s), output);
				break;
//			case STRING:
			case BUNDLE:
			case ARRAY:
			case BINARY:
				byte[] buf;

				switch (type)
				{
					case STRING:
						buf = UTF8.encodeUTF8((String)aValue);
						break;
					case BUNDLE:
						buf = writeBundle((Bundle)aValue);
						break;
					case ARRAY:
						buf = writeArray((Array)aValue);
						break;
					case BINARY:
						buf = (byte[])aValue;
						break;
					default:
						throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
				}

				output.writeVar32(buf.length);
				output.write(buf);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}

		output.finish();

		return baos.toByteArray();
	}


	private void writeVarString(BitOutputStream aOutput, String aText) throws IOException
	{
		aOutput.writeVar32(aText.length());
		UTF8.encodeUTF8(aText);
	}
}
