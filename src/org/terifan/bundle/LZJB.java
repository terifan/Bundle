package org.terifan.bundle;

import java.io.File;
import java.io.FileInputStream;
import org.terifan.bundle.bundle_test.Log;



public class LZJB
{
	private final static int NBBY = 8;
	private final static int MATCH_BITS	= 6;
	private final static int MATCH_MIN = 3;
	private final static int MATCH_MAX = ((1 << MATCH_BITS) + (MATCH_MIN - 1));
	private final static int OFFSET_MASK = ((1 << (16 - MATCH_BITS)) - 1);
//	private final static int WINDOW_SIZE = 1024 - 1;
	private final static int WINDOW_SIZE = 256 - 1;


//	public static int compress(byte[] aInput, int aInputOffset, int aInputLength, byte[] aOutput, int aOutputOffset)
//	{
//		int src = aInputOffset;
//		int dst = aOutputOffset;
//		int cpy, copymap = 0;
//		int copymask = 1 << (NBBY - 1);
//		int mlen, offset, hash;
//		int hp;
//		int [] refs = new int[WINDOW_SIZE+1];
//
//		while (src < aInputLength)
//		{
//			if ((copymask <<= 1) == (1 << NBBY))
//			{
//				copymask = 1;
//				copymap = dst;
//				aOutput[dst++] = 0;
//			}
//
//			if (src > aInputLength - MATCH_MAX)
//			{
//				aOutput[dst++] = aInput[src++];
//				continue;
//			}
//
//			byte a = aInput[src];
//			byte b = aInput[src + 1];
//			byte c = aInput[src + 2];
//
//			hash = (a << 16) + (b << 8) + c;
//			hash += hash >> 9;
//			hash += hash >> 5;
//
//			hp = hash & WINDOW_SIZE;
//			offset = (src - refs[hp]) & OFFSET_MASK;
//			refs[hp] = src;
//			cpy = src - offset;
//
//			if (cpy >= 0 && cpy != src && a == aInput[cpy] && b == aInput[cpy+1] && c == aInput[cpy+2])
//			{
//				aOutput[copymap] |= copymask;
//
//				for (mlen = MATCH_MIN; mlen < MATCH_MAX; mlen++)
//				{
//					if (aInput[src+mlen] != aInput[cpy+mlen])
//					{
//						break;
//					}
//				}
//
//				aOutput[dst++] = (byte)(((mlen - MATCH_MIN) << (NBBY - MATCH_BITS)) | (offset >> NBBY));
//				aOutput[dst++] = (byte)offset;
//				src += mlen;
//			}
//			else
//			{
//				aOutput[dst++] = a;
//				src++;
//			}
//		}
//
//		return dst;
//	}
//
//
//	public static void decompress(byte[] aInput, int aInputOffset, int aInputLength, byte[] aOutput, int aOutputOffset, int aOutputLength)
//	{
//		int src = aInputOffset;
//		int dst = aOutputOffset;
//		int d_end = aOutputLength;
//		int copymap = 0;
//		int copymask = 1 << (NBBY - 1);
//
//		while (dst < d_end)
//		{
//			if ((copymask <<= 1) == (1 << NBBY))
//			{
//				copymask = 1;
//				copymap = 255 & aInput[src++];
//			}
//			if ((copymap & copymask) != 0)
//			{
//				int mlen = ((255 & aInput[src]) >> (NBBY - MATCH_BITS)) + MATCH_MIN;
//				int offset = (((255 & aInput[src]) << NBBY) | (255 & aInput[src+1])) & OFFSET_MASK;
//				src += 2;
//				int cpy = dst - offset;
//				if (cpy < 0)
//				{
//					throw new IllegalStateException();
//				}
//				while (--mlen >= 0 && dst < d_end)
//				{
//					aOutput[dst++] = aOutput[cpy++];
//				}
//			}
//			else
//			{
//				aOutput[dst++] = aInput[src++];
//			}
//		}
//
//		if (dst != aOutputOffset + aOutputLength)
//		{
//			throw new IllegalStateException("Failed to decompress data.");
//		}
//	}


	private final static int W = (1 << 16) - 1;
	private final static int P = (1 << 24) - 1;
	private byte[] mWindow = new byte[W + 1];
	private int[] mPointers = new int[P + 1];
	private int mPosition;


	public LZJB()
	{
	}


	public byte[] compress(byte[] aBuffer)
	{
		int copys = 0;
		int copylen = 0;
		int literals = 0;

		for (int offset = 0; offset < aBuffer.length; )
		{
			int matchLen = 1;
			int position = 0;

			if (offset < aBuffer.length - 2)
			{
				int hash = hash(aBuffer, offset);
				position = mPointers[hash];
				boolean match = aBuffer[offset] == mWindow[position & W] && aBuffer[offset + 1] == mWindow[(position + 1) & W] && aBuffer[offset + 2] == mWindow[(position + 2) & W];

				insert(offset, 0, aBuffer);

				if (match)
				{
					for (; offset + matchLen < aBuffer.length; matchLen++)
					{
						if (aBuffer[offset + matchLen] != mWindow[(position + matchLen) & W])
						{
							break;
						}

						insert(offset, matchLen, aBuffer);
					}
				}
			}
			else
			{
				insert(offset, 0, aBuffer);
			}

			if (matchLen==1) literals++;
			else {copys++; copylen+=matchLen;}

//			Log.out.printf("%3d %2d [%s]\n", matchLen==1?0:mPosition-position-matchLen, matchLen, new String(aBuffer, offset, matchLen));

			offset += matchLen;
		}

		Log.out.println(literals+" "+copys+" "+copylen);

		return null;
	}


	private void insert(int aOffset, int aMatchLen, byte[] aBuffer)
	{
		if (aOffset + aMatchLen < aBuffer.length - 2)
		{
			mPointers[hash(aBuffer, aOffset + aMatchLen)] = mPosition;
		}
		mWindow[mPosition & W] = aBuffer[aOffset + aMatchLen];
		mPosition++;
	}


	private int hash(byte[] aBuffer, int aOffset)
	{
		return ((aBuffer[aOffset    ] << 16)
			  ^ (aBuffer[aOffset + 1] <<  8)
			  ^ (aBuffer[aOffset + 2]      )) & P;
	}


	public static void main(String ... args)
	{
		try
		{
			LZJB compressor = new LZJB();
//			compressor.compress("helling".getBytes());
//			compressor.compress("helloworld".getBytes());
//			compressor.compress("world".getBytes());
//			compressor.compress("helloworld".getBytes());
//			compressor.compress("worldwar".getBytes());
//			compressor.compress("warcrafting".getBytes());

//			compressor.compress("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww".getBytes());

//			compressor.compress("banana apple lemon cigar car monkey monkey".getBytes());

			File file = new File("d:/ex151012.log");
			byte[] buf = new byte[(int)file.length()];
			try (FileInputStream in = new FileInputStream(file))
			{
				in.read(buf);
			}

			compressor.compress(buf);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
