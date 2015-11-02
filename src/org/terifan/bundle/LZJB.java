package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.terifan.bundle.bundle_test.Log;


public class LZJB
{
	private final static int WINDOW_SIZE_BITS = 16;
	private final static int POINTERS_SIZE_BITS = 12;
	private final static int POINTER_MAX_CHAIN = 8;

	private final static int W = (1 << WINDOW_SIZE_BITS) - 1;
	private final static int P = (1 << POINTERS_SIZE_BITS) - 1;
	private byte[] mWindow = new byte[W + 1];
	private int[][] mPointers = new int[P + 1][POINTER_MAX_CHAIN];
	private int mPosition;
	
	private int mTotal;
	
	private FrequencyTable mLiteralStats = new FrequencyTable(256);
	private FrequencyTable mDistanceStats = new FrequencyTable(W + 1);


	public LZJB()
	{
		mLiteralStats.encode(' ');
		mLiteralStats.encode('.');
		mLiteralStats.encode(',');
		for (int i = '0'; i <= '9'; i++)
		{
			mLiteralStats.encode(i);
		}
		for (int i = 'a'; i <= 'z'; i++)
		{
			mLiteralStats.encode(i);
		}
		for (int i = 'A'; i <= 'Z'; i++)
		{
			mLiteralStats.encode(i);
		}
	}


	public int getTotal()
	{
		return mTotal;
	}


	public void write(BitOutputStream aBitOutputStream, byte[] aBuffer) throws IOException
	{
		mTotal += aBuffer.length;
		
		for (int offset = 0; offset < aBuffer.length; )
		{
			int matchLen = 1;
			int position = 0;

			if (offset < aBuffer.length - 2)
			{
				int[] ptr = mPointers[hash(aBuffer, offset)];

				int bi = 0;
				int bl = 0;
				for (int i = 0; i < POINTER_MAX_CHAIN; i++)
				{
					int x = 0;
					for (int j = 0; offset+j < aBuffer.length && j < 1000; j++, x++)
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

				position = ptr[bi];

				boolean match = aBuffer[offset] == mWindow[position & W] && aBuffer[offset + 1] == mWindow[(position + 1) & W] && aBuffer[offset + 2] == mWindow[(position + 2) & W];

				insert(aBuffer, offset++);

				if (bl >= 3 || match)
				{
					for (position++; offset < aBuffer.length && matchLen < 1000; matchLen++, position++)
					{
						if (aBuffer[offset] != mWindow[position & W])
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

//			Log.hexDump(mWindow);

			if (matchLen == 1)
			{
				int literal = 0xff & aBuffer[offset - matchLen];

//				Log.out.print((char)aBuffer[offset - matchLen]);
				aBitOutputStream.writeBit(0);
				aBitOutputStream.writeExpGolomb(mLiteralStats.encode(literal), 3);
			}
			else
			{
//				Log.out.print("["+(mPosition - position - 1)+","+(matchLen - 3)+"]");
				aBitOutputStream.writeBit(1);
				aBitOutputStream.writeExpGolomb(mDistanceStats.encode(mPosition - position - 1), 4);
				aBitOutputStream.writeExpGolomb(matchLen - 3, 0);
			}

//			Log.out.printf("%3d %2d [%s]\n", matchLen==1?0:mPosition-position, matchLen, new String(aBuffer, offset - matchLen, matchLen));
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
			mPointers[hash][0] = mPosition - 2;
		}
		mWindow[mPosition & W] = aBuffer[aOffset];
		mPosition++;
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
				compressor.write(bos, "helling".getBytes());
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
