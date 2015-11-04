package org.terifan.bundle.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.terifan.bundle.BitOutputStream;
import org.terifan.bundle.bundle_test.Log;


public class LZJB
{
	private final static int WINDOW_SIZE_BITS = 16;
	private final static int POINTERS_SIZE_BITS = 12;
	private final static int POINTER_MAX_CHAIN = 8;
	private final static int MAX_MATCH = 1264;

	private final static int W = (1 << WINDOW_SIZE_BITS) - 1;
	private final static int P = (1 << POINTERS_SIZE_BITS) - 1;
	private byte[] mWindow = new byte[W + 1];
	private int[][] mPointers = new int[P + 1][POINTER_MAX_CHAIN];
	private int mWindowPosition;
	
	private int mTotal;
	
	private FrequencyTable[] mLiteralStats = new FrequencyTable[64];
	private FrequencyTable mLengthStats = new FrequencyTable(W + 1);

	private int[] distances = {
		4,8,9,10
	};

	private int[][] huffmanLength;
	private int[][] huffmanDistance;
	private int[][] huffmanLiteral;

	public LZJB() throws IOException
	{
		huffmanLength = HuffmanTree.construct(3,3,5,6,7,10);
		huffmanDistance = HuffmanTree.construct(1,4,6,9,11,12,14,14,15);
		huffmanLiteral = HuffmanTree.construct(4,4,5,6,7);
		
		for (int i = 0; i < mLiteralStats.length; i++)
		{
			mLiteralStats[i] = new FrequencyTable(256);

			mLiteralStats[i].encode(' ');
			mLiteralStats[i].encode('.');
			mLiteralStats[i].encode(',');
			for (int j = '0'; j <= '9'; j++)
			{
				mLiteralStats[i].encode(j);
			}
			for (int j = 'a'; j <= 'z'; j++)
			{
				mLiteralStats[i].encode(j);
			}
			for (int j = 'A'; j <= 'Z'; j++)
			{
				mLiteralStats[i].encode(j);
			}
		}

//		byte[] buf = new byte[122784];
//		try (InputStream in = getClass().getResourceAsStream("dic.bin"))
//		{
//			in.read(buf);
//		}

//		byte[] buf = "date true false addresses currency telephone handle name version country location latitude longitude create User identifier email shipment contacts phoneNumber order party measurement value Type postal Code Date Time Locations type Partner Address Details City Name Index Shared Street Text Measure Value Total Order Description Quantity	 Unit Entry List Domain Contact Identifier Remark".getBytes();

//		for (int i = 0; i < buf.length; i++)
//		{
//			if (buf[i] != ' ')
//			{
//				insert(buf, i);
//			}
//		}
	}


	public int getTotal()
	{
		return mTotal;
	}


	public void write(BitOutputStream aBitOutputStream, byte[] aBuffer) throws IOException
	{
		mTotal += aBuffer.length;

		int[][] commands = new int[64][4];

		for (int offset = 0, commandIndex = 0; offset < aBuffer.length;)
		{
			int matchLen = 1;
			int matchPosition = 0;

			if (offset < aBuffer.length - 2)
			{
				int[] ptr = mPointers[hash(aBuffer, offset)];

				int bi = 0;
				int bl = 0;
				for (int i = 0; i < POINTER_MAX_CHAIN; i++)
				{
					int x = 0;
					for (int j = 0; offset+j < aBuffer.length && j < MAX_MATCH; j++, x++)
					{
						if (aBuffer[offset+j] != mWindow[(ptr[i]+j) & W])
						{
							break;
						}
					}
					if (x > bl)
					{
						bl = x;
						bi = i;
					}
				}

				matchPosition = ptr[bi];

				boolean match = aBuffer[offset] == mWindow[matchPosition & W] && aBuffer[offset + 1] == mWindow[(matchPosition + 1) & W] && aBuffer[offset + 2] == mWindow[(matchPosition + 2) & W];

				insert(aBuffer, offset++);

				if (bl >= 3 || match)
				{
					for (matchPosition++; offset < aBuffer.length && matchLen < MAX_MATCH; matchLen++, matchPosition++)
					{
						if (aBuffer[offset] != mWindow[matchPosition & W])
						{
							break;
						}

						insert(aBuffer, offset++);
					}
				}
			}
			else
			{
				insert(aBuffer, offset++);
			}

			commands[commandIndex][0] = matchLen;
			commands[commandIndex][1] = matchPosition;
			commands[commandIndex][2] = offset;
			commands[commandIndex][3] = mWindowPosition;
			commandIndex++;

			if (commandIndex == commands.length || offset == aBuffer.length)
			{
				flushBlock(aBitOutputStream, commands, commandIndex, aBuffer);
				commandIndex = 0;
			}
		}
	}
	
	
	private void flushBlock(BitOutputStream aBitOutputStream, int[][] aCommands, int aCommandCount, byte[] aBuffer) throws IOException
	{
		boolean fail = false;
		for (int i = 0; i < aCommandCount; i++)
		{
			fail |= aCommands[i][0] == 1;
		}
		
		aBitOutputStream.writeBit(fail ? 1 : 0);

		for (int i = 0; i < aCommandCount; i++)
		{
			int matchLen = aCommands[i][0];
			int matchPosition = aCommands[i][1];
			int offset = aCommands[i][2];
			int windowPosition = aCommands[i][3];

			if (matchLen == 1)
			{
				int context = 0;
				int c = 0xff & mWindow[(windowPosition - 3) & W];
				int b = 0xff & mWindow[(windowPosition - 2) & W];
				int a = 0xff & mWindow[(windowPosition - 1) & W];
				if (c == 0)       context  = 0b000_00_0;
				else              context  = 0b000_00_1;
				if      (b == 0)  context += 0b000_00_0;
				else if (b < 64)  context += 0b000_01_0;
				else if (b < 255) context += 0b000_10_0;
				else              context += 0b000_11_0;
				if      (a == 0)  context += 0b000_00_0;
				else if (a < 16)  context += 0b001_00_0;
				else if (a < 64)  context += 0b010_00_0;
				else if (a < 128) context += 0b011_00_0;
				else if (a < 192) context += 0b100_00_0;
				else if (a < 240) context += 0b101_00_0;
				else if (a < 255) context += 0b110_00_0;
				else              context += 0b111_00_0;
				
				int literal = 0xff & aBuffer[offset - matchLen];

				int ch = mLiteralStats[context].encode(literal);

				if (!fail)
				{
					aBitOutputStream.writeBit(0);
				}

				aBitOutputStream.writeBits(huffmanLiteral[ch][0], huffmanLiteral[ch][1]);

//				Log.out.print((char)aBuffer[offset - matchLen]);
			}
			else
			{
//				Log.out.print("["+(windowPosition - matchPosition - 1)+","+(matchLen - 3)+"]");

				int distance = windowPosition - matchPosition - 1;
				int d;
				
				if (distance == distances[0]) d = 0;
				else if (distance == distances[1]) d = 1;
				else if (distance == distances[2]) d = 2;
				else if (distance == distances[3]) d = 3;
				else if (distance == distances[0] + 1) d = 4;
				else if (distance == distances[0] - 1) d = 5;
				else if (distance == distances[0] + 2) d = 6;
				else if (distance == distances[0] - 2) d = 7;
				else if (distance == distances[0] + 3) d = 8;
				else if (distance == distances[0] - 3) d = 9;
				else if (distance == distances[1] + 1) d = 10;
				else if (distance == distances[1] - 1) d = 11;
				else if (distance == distances[1] + 2) d = 12;
				else if (distance == distances[1] - 2) d = 13;
				else if (distance == distances[1] + 3) d = 14;
				else if (distance == distances[1] - 3) d = 15;
				else d = 16 + distance;

				int l = mLengthStats.encode(matchLen - 3);

				aBitOutputStream.writeBit(1);
				aBitOutputStream.writeBits(huffmanDistance[d][0], huffmanDistance[d][1]);
				aBitOutputStream.writeBits(huffmanLength[l][0], huffmanLength[l][1]);

				distances[3] = distances[2];
				distances[2] = distances[1];
				distances[1] = distances[0];
				distances[0] = distance;
			}
		}
	}
	

	private void insert(byte[] aBuffer, int aOffset)
	{
		if (aOffset >= 2)
		{
			int hash = hash(aBuffer, aOffset - 2);
			for (int i = POINTER_MAX_CHAIN; --i >= 1;)
			{
				mPointers[hash][i] = mPointers[hash][i - 1];
			}
			mPointers[hash][0] = mWindowPosition - 2;
		}
		mWindow[mWindowPosition & W] = aBuffer[aOffset];
		mWindowPosition++;
	}


	private int hash(byte[] aBuffer, int aOffset)
	{
		return ((aBuffer[aOffset    ] << 12)
			  ^ (aBuffer[aOffset + 1] <<  4)
			  ^ (aBuffer[aOffset + 2]      )) & P;
	}

	
	public void analyze()
	{
//		Log.out.println(mHashCollision);
	}
	

	public static void main(String ... args)
	{
		try
		{
			LZJB compressor = new LZJB();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (BitOutputStream bos = new BitOutputStream(baos))
			{
				compressor.write(bos, "hello".getBytes());
				compressor.write(bos, "helloworld".getBytes());
				compressor.write(bos, "world".getBytes());
				compressor.write(bos, "helloworld".getBytes());
				compressor.write(bos, "worldwar".getBytes());
				compressor.write(bos, "warcrafting".getBytes());

//				compressor.compress(bos, "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww".getBytes());

//				compressor.compress(bos, "banana apple lemon cigar car monkey monkey".getBytes());

//				File file = new File("d:/model.bundle");
//				byte[] buf = new byte[(int)file.length()];
//				try (FileInputStream in = new FileInputStream(file))
//				{
//					in.read(buf);
//				}
//				compressor.compress(bos, buf);
			}

			Log.out.println();
			Log.out.println(baos.size()+"/"+compressor.getTotal());
			Log.hexDump(baos.toByteArray());
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
