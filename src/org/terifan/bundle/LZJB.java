package org.terifan.bundle;

import java.util.Arrays;
import samples.Log;


public class LZJB
{
	private final static int MATCH_BITS = 3;
	private final static int MATCH_MIN = 2;
	private final static int MATCH_MAX = (1 << MATCH_BITS) + (MATCH_MIN - 1);
	private final static int WINDOW_SIZE = 1 << (16 - MATCH_BITS);
	private final static int OFFSET_MASK = WINDOW_SIZE - 1;

	private byte[] mWindow = new byte[0];
	private int[] mRefs = new int[WINDOW_SIZE];
	private int mWindowOffset;


	public int compress(byte[] aSrcBuffer, byte[] aDstBuffer, int aSrcLen, int aDstLen)
	{
		int src = mWindowOffset;
		int dst = 0;
		int copymapOffset = 0;
		int copymask = 128;
		int end = mWindowOffset + aSrcLen;

		mWindow = Arrays.copyOfRange(mWindow, 0, end);
		System.arraycopy(aSrcBuffer, 0, mWindow, mWindowOffset, aSrcLen);

		while (src < end)
		{
			copymask <<= 1;
			if (copymask == 256)
			{
				copymask = 1;
				copymapOffset = dst;
				aDstBuffer[dst++] = 0;
			}

			if (src >= end - MATCH_MIN)
			{
				aDstBuffer[dst++] = mWindow[src++];
				continue;
			}

			int hash = ((0xff & mWindow[src]) << 16) + ((0xff & mWindow[src + 1]) << 8) + (0xff & mWindow[src + 2]);
			hash += hash >> 9;
			hash += hash >> 5;
			hash &= OFFSET_MASK;

			int offset = (src - mRefs[hash]) & OFFSET_MASK;
			int cpy = src - offset;

			mRefs[hash] = src;

			if (cpy >= 0 && cpy + MATCH_MIN < src && mWindow[src] == mWindow[cpy] && mWindow[src + 1] == mWindow[cpy + 1] && mWindow[src + 2] == mWindow[cpy + 2])
			{
				aDstBuffer[copymapOffset] |= copymask;

				int mlen = MATCH_MIN;
				for (; src + mlen < end && mlen < MATCH_MAX + 64+0*256; mlen++)
				{
					if (mWindow[src + mlen] != mWindow[cpy + mlen])
					{
						break;
					}
				}
				if (mlen >= MATCH_MAX)
				{
					aDstBuffer[dst++] = (byte)((((1 << MATCH_BITS) - 1) << (8 - MATCH_BITS)) | (offset >> 8));
					aDstBuffer[dst++] = (byte)offset;
					aDstBuffer[dst++] = (byte)(mlen - 0*MATCH_MAX);
				}
				else
				{
					aDstBuffer[dst++] = (byte)(((mlen - MATCH_MIN) << (8 - MATCH_BITS)) | (offset >> 8));
					aDstBuffer[dst++] = (byte)offset;
				}
				src += mlen;
			}
			else
			{
				aDstBuffer[dst++] = mWindow[src++];
			}
		}

		mWindowOffset += aSrcLen;

		return dst;
	}


	public void decompress(byte[] aSrcBuffer, byte[] aDstBuffer, int aSrcLen, int aDstLen)
	{
		int src = 0;
		int dst = 0;
		int end = aDstLen;
		int copymap = 0;
		int copymask = 128;

		mWindow = Arrays.copyOfRange(mWindow, 0, mWindowOffset + end);

		while (dst < end)
		{
			copymask <<= 1;
			if (copymask == 256)
			{
				copymask = 1;
				copymap = 255 & aSrcBuffer[src++];
			}
			if ((copymap & copymask) != 0)
			{
				int mlen = ((255 & aSrcBuffer[src]) >> (8 - MATCH_BITS)) + MATCH_MIN;
				int offset = (((255 & aSrcBuffer[src]) << 8) | (255 & aSrcBuffer[src + 1])) & OFFSET_MASK;
				src += 2;
				if (mlen == (1 << MATCH_BITS) - 1)
				{
					mlen = /*MATCH_MAX +*/ (255 & aSrcBuffer[src++]);
				}
				int cpy = mWindowOffset + dst - offset;
				if (cpy < 0)
				{
					throw new RuntimeException();
				}
				while (--mlen >= 0 && dst < end)
				{
					mWindow[mWindowOffset++] = mWindow[cpy];
					aDstBuffer[dst++] = mWindow[cpy++];
				}
			}
			else
			{
				mWindow[mWindowOffset++] = aSrcBuffer[src];
				aDstBuffer[dst++] = aSrcBuffer[src++];
			}
		}
	}


	public static void main(String... args)
	{
		try
		{
			byte[] src1 = "For us, it's a really exciting outcome, because this novel litigation approach worked and would get us a resolution really quickly, and it gave us a way to get our client's data deleted. We were prepared for much more pushback. It's incredibly useful to have this tool in our toolkit for when phones are taken in the future. I can't see any reason why this couldn't be done whenever another traveler is facing this sort of phone seizure.".getBytes();
			byte[] src2 = "litigation".getBytes();
			byte[] src3 = "approach".getBytes();

//			byte[] src1 = "test".getBytes();
//			byte[] src2 = "testtest".getBytes();
//			byte[] src3 = "test".getBytes();
			byte[] dst1 = new byte[(src1.length + 7) * 9 / 8];
			byte[] dst2 = new byte[(src2.length + 7) * 9 / 8];
			byte[] dst3 = new byte[(src3.length + 7) * 9 / 8];
			byte[] unpack1 = new byte[src1.length];
			byte[] unpack2 = new byte[src2.length];
			byte[] unpack3 = new byte[src3.length];

			LZJB lzjb = new LZJB();

			int len1 = lzjb.compress(src1, dst1, src1.length, dst1.length);
			int len2 = lzjb.compress(src2, dst2, src2.length, dst2.length);
			int len3 = lzjb.compress(src3, dst3, src3.length, dst3.length);

			Log.hexDump(Arrays.copyOfRange(dst1, 0, len1));
			Log.hexDump(Arrays.copyOfRange(dst2, 0, len2));
			Log.hexDump(Arrays.copyOfRange(dst3, 0, len3));

			System.out.println(src1.length + " / " + len1);
			System.out.println(src2.length + " / " + len2);
			System.out.println(src3.length + " / " + len3);

			lzjb = new LZJB();
			lzjb.decompress(dst1, unpack1, dst1.length, unpack1.length);
			lzjb.decompress(dst2, unpack2, dst2.length, unpack2.length);
			lzjb.decompress(dst3, unpack3, dst3.length, unpack3.length);

			System.out.println();
			System.out.println(new String(unpack1).equals(new String(src1)));
			System.out.println(new String(unpack2).equals(new String(src2)));
			System.out.println(new String(unpack3).equals(new String(src3)));

			System.out.println();
			System.out.println(new String(unpack1));
			System.out.println(new String(unpack2));
			System.out.println(new String(unpack3));
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
}
