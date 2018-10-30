package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import samples.Log;


public class PredictorCodec 
{
	public static void main(String... args)
	{
		try
		{
			byte[] input = "Predictor works by filling a guess table with values, based on the hash of the previous characters seen. Since we are either emitting the source data, or depending on the guess table, we add a flag bit for every byte of input, telling the decompressor if it should retrieve the byte from the compressed data stream, or the guess table. Blocking the input into groups of 8 characters means that we don't have to bit-insert the compressed output - a flag byte preceeds every 8 bytes of compressed data. Each bit of the flag byte corresponds to one byte of reconstructed data.".getBytes();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] table = new byte[256 * 256];
			compress(new ByteArrayInputStream("The CCP Protocol Identifier that starts the packet is always 0xfd. If PPP Protocol field compression has not be negotiated, it MUST be a 16-bit field. The Compressed data is the Protocol Identifier and the Info fields of the original PPP packet described in [1], but not the Address, Control, FCS, or Flag. The CCP Protocol field MAY be compressed as described in [1], regardless of whether the Protocol field of the CCP Protocol Identifier is compressed or whether PPP Protocol field compression It is not required that any field land on an even word boundary - the compressed data may be of any length. If during the decode procedure, the CRC-16 does not match the decoded frame, it means that the compress or decompress process has become desyncronized. This will happen as a result of a frame being lost in transit if LAPB is not used. In this case, a new configure-request must be sent, and the CCP will drop out of the open state. Upon receipt of the configure-ack, the predictor tables are cleared to zero, and compression can be resumed without data loss. The correct encapsulation for type 2 compression is the protocol type, followed by the data stream. Within the data stream is the current frame length (uncompressed), compressed data, and uncompressed CRC-16 of the two octets of unsigned length in network byte order, followed by the original, uncompressed data. The data stream may be broken at any convenient place for encapsulation purposes. With type 2 encapsulation, LAPB is almost essential for correct delivery.  Predictor is a high speed compression algorithm, available without license fees. The compression ratio obtained using predictor is not as good as other compression algorithms, but it remains one of the fastest algorithms available. Note that although care has been taken to ensure that the following code does not infringe any patents, there is no assurance that it is The CCP Protocol Identifier that starts the packet is always 0xfd. If PPP Protocol field compression has not be negotiated, it MUST be a 16-bit field. The Compressed data is the Protocol Identifier and the Info fields of the original PPP packet described in [1], but not the Address, Control, FCS, or Flag. The CCP Protocol field MAY be compressed as described in [1], regardless of whether the Protocol field of the CCP Protocol Identifier is compressed or whether PPP Protocol field compression has been negotiated. It is not required that any of the fields land on an even word boundary - the compressed data may be of any length. If during the decode procedure, the CRC-16 does not match the decoded frame, it means that the compress or decompress process has become desyncronized. This will happen as a result of a frame being lost in transit if LAPB is not used. In this case, a new configure-request must be sent, and the CCP will drop out of the open state. Upon receipt of the configure-ack, the predictor tables are cleared to zero, and compression can be resumed without data loss.  Before any Predictor packets may be communicated, PPP must reach the Network-Layer Protocol phase, and the Compression Control Protocol must reach the Opened state. Exactly one Predictor datagram is encapsulated in the PPP Information field, where the PPP Protocol field indicates type hex 00FD (compressed datagram). The maximum length of the Predictor datagram transmitted over a PPP link is the same as the maximum length of the Information field of a PPP encapsulated packet. Prior to compression, the uncompressed data begins with the PPP Protocol number. This value MAY be compressed when Protocol-Field- Compression is negotiated. PPP Link Control Protocol packets MUST NOT be send within compressed data.".getBytes()), table, new ByteArrayOutputStream(), true);

			compress(new ByteArrayInputStream(input), table.clone(), baos, false);
			
			byte[] compressedData = baos.toByteArray();
			baos.reset();
			
			decompress(new ByteArrayInputStream(compressedData), table.clone(), baos, false);

			Log.hexDump(compressedData);
			
			System.out.println();
			System.out.println("input: " + input.length);
			System.out.println("compressed: " + compressedData.length);
			System.out.println("match: " + Arrays.equals(baos.toByteArray(), input));
			
//			System.out.println();
//			System.out.println(new String(input));
//			System.out.println(baos.toString());
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	public static void compress(InputStream aInput, byte[] aTable, OutputStream aOutput, boolean aTrain) throws IOException
	{
		byte[] buf = new byte[8];
		int hash = 0;

		for (;;)
		{
			int flags = 0;
			int offset = 0;

			for (int i = 0; i < 8; i++)
			{
				int c = aInput.read();

				if (c == -1)
				{
					if (i == 0)
					{
						return;
					}
					break;
				}

				if (c == aTable[hash])
				{
					flags |= 1 << i;
				}
				else
				{
					buf[offset++] = (byte)c;

					if (aTrain)
					{
						aTable[hash] = (byte)c;
					}
				}

//				hash = ((hash << 4) ^ c) & 0xffff;
				hash = ((hash << 8) ^ c) & 0xffff;
			}

			aOutput.write((byte)flags);
			aOutput.write(buf, 0, offset);
		}
	}


	public static void decompress(InputStream aInput, byte[] aTable, OutputStream aOutput, boolean aTrain) throws IOException
	{
		int hash = 0;

		for (;;)
		{
			int flags = aInput.read();
			
			if (flags == -1)
			{
				return;
			}

			for (int i = 0; i < 8; i++)
			{
				int c;

				if ((flags & (1 << i)) != 0)
				{
					c = aTable[hash];
				}
				else
				{
					c = aInput.read();

					if (c == -1)
					{
						return;
					}

					if (aTrain)
					{
						aTable[hash] = (byte)c;
					}
				}

				aOutput.write((byte)c);

//				hash = ((hash << 4) ^ c) & 0xffff;
				hash = ((hash << 8) ^ c) & 0xffff;
			}
		}
	}
}
