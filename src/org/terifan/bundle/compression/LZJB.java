package org.terifan.bundle.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.terifan.bundle.io.BitOutputStream;
import org.terifan.bundle.bundle_test.Log;


public class LZJB
{
	private final static int WINDOW_SIZE_BITS = 15; //16;
	private final static int POINTERS_SIZE_BITS = 12;
	private final static int POINTER_MAX_CHAIN = 8;
//	private final static int MAX_MATCH = 1264;
	private final static int MAX_MATCH = 258;

	private final static int W = (1 << WINDOW_SIZE_BITS) - 1;
	private final static int P = (1 << POINTERS_SIZE_BITS) - 1;
	private byte[] mWindow = new byte[W + 1];
	private int[][] mPointers = new int[P + 1][POINTER_MAX_CHAIN];
	private int mWindowPosition;
	
	private int mTotal;
	
//	private FrequencyTable[] mLiteralStats = new FrequencyTable[64];
//	private FrequencyTable mLengthStats = new FrequencyTable(W + 1);

	private int[] distances = {
		4,8,9,10
	};

//	Huffman huffmanDistance;
	Huffman huffmanLiteral;

	static int[][] kBlockLengthPrefixCode = {
		{   1,  2}, {    5,  2}, {  9,   2}, {  13,  2},
		{  17,  3}, {   25,  3}, {  33,  3}, {  41,  3},
		{  49,  4}, {   65,  4}, {  81,  4}, {  97,  4},
		{ 113,  5}, {  145,  5}, { 177,  5}, { 209,  5},
		{ 241,  6}, {  305,  6}, { 369,  7}, { 497,  8},
		{ 753,  9}, { 1265, 10}, {2289, 11}, {4337, 12},
		{8433, 13}, {16625, 24}
	};

	static int[][] literalCodes = 
	{
		{257, 0,  3}, {258, 0,  4}, {259, 0,  5}, 
		{260, 0,  6}, {261, 0,  7}, {262, 0,  8}, 
		{263, 0,  9}, {264, 0, 10}, {265, 1, 11}, 
		{266, 1, 13}, {267, 1, 15}, {268, 1, 17}, 
		{269, 2, 19}, {270, 2, 23}, {271, 2, 27}, 
		{272, 2, 31}, {273, 3, 35}, {274, 3, 43}, 
		{275, 3, 51}, {276, 3, 59}, {277, 4, 67},
		{278, 4, 83}, {279, 4, 99}, {280, 4, 115},
		{281, 5, 131}, {282, 5, 163}, {283, 5, 195},
		{284, 5, 227}, {285, 0, 258}
	};

	static int[][] distanceCodes = 
	{
		{0, 0, 1}, {1, 0, 2}, {2, 0, 3}, 
		{3, 0, 4}, {4, 1, 5}, {5, 1, 7}, 
		{6, 2, 9}, {7, 2, 13}, {8, 3, 17}, 
		{9, 3, 25}, {10, 4, 33}, {11, 4, 49}, 
		{12, 5, 65}, {13, 5, 97}, {14, 6, 129}, 
		{15, 6, 193}, {16, 7, 257}, {17, 7, 385}, 
		{18, 8, 513}, {19, 8, 769}, {20, 9, 1025},
		{21, 9, 1537}, {22, 10, 2049}, {23, 10, 3073},
		{24, 11, 4097}, {25, 11, 6145}, {26, 12, 8193},
		{27, 12, 12289}, {28, 13, 16385}, {29, 13, 24577}
	};

	public LZJB() throws IOException
	{
//		huffmanDistance = new Huffman(W + 1);
		huffmanLiteral = new Huffman(285);

		int[][] tree = new int[285][2];
		for (int d = 0; d < 285; d++)
		{
			if (d <= 143) 
			{
				tree[d][0] = 0b00110000 + d;
				tree[d][1] = 8;
			}
			else if (d <= 255) 
			{
				tree[d][0] = 0b110010000 + d - 144;
				tree[d][1] = 9;
			}
			else if (d <= 279) 
			{
				tree[d][0] = 0b0000000 + d - 256;
				tree[d][1] = 7;
			}
			else 
			{
				tree[d][0] = 0b11000000 + d - 280;
				tree[d][1] = 8;
			}
		}

		huffmanLiteral.buildCustom(tree);
		
		
//		for (int i = 0; i < mLiteralStats.length; i++)
//		{
//			mLiteralStats[i] = new FrequencyTable(256);
//		}

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

//				int ch = mLiteralStats[context].encode(literal);

//				literal = 0xff & (literal - context);
				
				huffmanLiteral.encode(aBitOutputStream, literal);
				
//				Log.out.print((char)literal);
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

//				aBitOutputStream.writeBit(1);
//				aBitOutputStream.writeBits(huffmanDistance[d][0], huffmanDistance[d][1]);
//				aBitOutputStream.writeBits(huffmanLength[l][0], huffmanLength[l][1]);

				for (int[] lit : literalCodes)
				{
					if (matchLen < lit[2]+(1<<lit[1]))
					{
//						Log.out.print("[" + (lit[0] - 1) + "-" + (matchLen - lit[2]) + ",");
						huffmanLiteral.encode(aBitOutputStream, lit[0] - 1);
						aBitOutputStream.writeBits(matchLen - lit[2], lit[1]);
						break;
					}
				}

				for (int[] dist : distanceCodes)
				{
					if (d < dist[2] + (1<<dist[1]))
					{
						aBitOutputStream.writeBits(dist[0], 5);
						aBitOutputStream.writeBits(d - dist[2], dist[1]);

//						huffmanDistance.encode(aBitOutputStream, d - dist[2]);

//						Log.out.print("" + dist[0] + "-" + d + "],");
						
						break;
					}
				}


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


	private static int[][] constructSuffixTree(int... aSuffixLengths)
	{
		int symbolCount = 0;
		int levels = aSuffixLengths.length - 1;
		for (int i = 0; i <= levels; i++)
		{
			symbolCount += 1 << aSuffixLengths[i];
		}
		
		int[][] huffman = new int[symbolCount][2];

		for (int i = 0, k = 0; i <= levels; i++)
		{
			int prefixLen = i == levels ? i : i + 1;
			int prefix = (i == levels ? (1 << i) - 1 : (2 << i) - 2) << aSuffixLengths[i];

			for (int j = 0; j < 1 << aSuffixLengths[i]; j++ ,k++)
			{
				huffman[k][0] = prefix + j;
				huffman[k][1] = (prefixLen + aSuffixLengths[i]);
			}
		}
		
		return huffman;
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
