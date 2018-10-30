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
	byte[] table = new byte[256 * 256];
	

	public byte[] marshal(Container aContainer) throws IOException
	{
		PredictorCodec.compress(new ByteArrayInputStream(("The CCP Protocol Identifier that starts the packet is always 0xfd. If PPP Protocol field compression has not be negotiated, it MUST be a 16-bit field. The Compressed data is the Protocol Identifier and the Info fields of the original PPP packet described in [1], but not the Address, Control, FCS, or Flag. The CCP Protocol field MAY be compressed as described in [1], regardless of whether the Protocol field of the CCP Protocol Identifier is compressed or whether PPP Protocol field compression It is not required that any field land on an even word boundary - the compressed data may be of any length. If during the decode procedure, the CRC-16 does not match the decoded frame, it means that the compress or decompress process has become desyncronized. This will happen as a result of a frame being lost in transit if LAPB is not used. In this case, a new configure-request must be sent, and the CCP will drop out of the open state. Upon receipt of the configure-ack, the predictor tables are cleared to zero, and compression can be resumed without data loss. The correct encapsulation for type 2 compression is the protocol type, followed by the data stream. Within the data stream is the current frame length (uncompressed), compressed data, and uncompressed CRC-16 of the two octets of unsigned length in network byte order, followed by the original, uncompressed data. The data stream may be broken at any convenient place for encapsulation purposes. With type 2 encapsulation, LAPB is almost essential for correct delivery.  Predictor is a high speed compression algorithm, available without license fees. The compression ratio obtained using predictor is not as good as other compression algorithms, but it remains one of the fastest algorithms available. Note that although care has been taken to ensure that the following code does not infringe any patents, there is no assurance that it is The CCP Protocol Identifier that starts the packet is always 0xfd. If PPP Protocol field compression has not be negotiated, it MUST be a 16-bit field. The Compressed data is the Protocol Identifier and the Info fields of the original PPP packet described in [1], but not the Address, Control, FCS, or Flag. The CCP Protocol field MAY be compressed as described in [1], regardless of whether the Protocol field of the CCP Protocol Identifier is compressed or whether PPP Protocol field compression has been negotiated. It is not required that any of the fields land on an even word boundary - the compressed data may be of any length. If during the decode procedure, the CRC-16 does not match the decoded frame, it means that the compress or decompress process has become desyncronized. This will happen as a result of a frame being lost in transit if LAPB is not used. In this case, a new configure-request must be sent, and the CCP will drop out of the open state. Upon receipt of the configure-ack, the predictor tables are cleared to zero, and compression can be resumed without data loss.  Before any Predictor packets may be communicated, PPP must reach the Network-Layer Protocol phase, and the Compression Control Protocol must reach the Opened state. Exactly one Predictor datagram is encapsulated in the PPP Information field, where the PPP Protocol field indicates type hex 00FD (compressed datagram). The maximum length of the Predictor datagram transmitted over a PPP link is the same as the maximum length of the Information field of a PPP encapsulated packet. Prior to compression, the uncompressed data begins with the PPP Protocol number. This value MAY be compressed when Protocol-Field- Compression is negotiated. PPP Link Control Protocol packets MUST NOT be send within compressed data.".replace(' ', '\u0000')).getBytes()), table, new ByteArrayOutputStream(), true);

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

			// json=785, bin=578, zipJSON=477, zipBIN=483
			
//			output.writeVar32(key.length());
			
			{
			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//			PredictorCodec.compress(new ByteArrayInputStream(key.getBytes("utf-8")), table.clone(), baos2, false);
			PredictorCodec.compress(new ByteArrayInputStream((key+"\u0000").getBytes("utf-8")), table.clone(), baos2, false);
			output.write(baos2.toByteArray());
			}

//			UTF8.encodeUTF8(key, output);

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
