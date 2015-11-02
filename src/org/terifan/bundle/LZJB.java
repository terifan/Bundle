package org.terifan.bundle;

import java.util.Arrays;
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


	private final static int W = 32 - 1;
	private final static int P = 4096 - 1;
	private byte[] mWindow = new byte[W + 1];
	private int[] mPointers = new int[P + 1];
	private int mPosition;


	public LZJB()
	{
		Arrays.fill(mPointers, -1);
	}


	public byte[] compress(byte[] aBuffer)
	{
		for (int offset = 0; offset < aBuffer.length; )
		{
			int matchLen = 1;
			int position;

			if (offset < aBuffer.length - 1)
			{
				int hash = hash(aBuffer, offset);
				position = mPointers[hash];
				boolean match = aBuffer[offset] == mWindow[position & W];

				mPointers[hash] = mPosition;
				mWindow[mPosition & W] = aBuffer[offset];
				mPosition++;

				if (match)
				{
					for (; offset + matchLen < aBuffer.length; matchLen++)
					{
						if (aBuffer[offset + matchLen] != mWindow[(position + matchLen) & W])
						{
							break;
						}

						if (offset + matchLen < aBuffer.length - 1)
						{
							mPointers[hash(aBuffer, offset + matchLen)] = mPosition;
						}
						mWindow[mPosition & W] = aBuffer[offset + matchLen];
						mPosition++;
					}
				}
			}
			else
			{
				position = -1;
				mWindow[mPosition & W] = aBuffer[offset];
				mPosition++;
			}

			Log.out.printf("%3d %2d [%s]\n", mPosition-position-matchLen, matchLen, new String(aBuffer, offset, matchLen));

			offset += matchLen;

//			Log.hexDump(mWindow);
		}

		return null;
	}


	private int hash(byte[] aBuffer, int aOffset)
	{
		return (((0xff & aBuffer[aOffset]) << 4) ^ (0xff & aBuffer[aOffset + 1])) & P;
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
			compressor.compress("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww".getBytes());
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
