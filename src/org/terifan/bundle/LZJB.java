package org.terifan.bundle;

import java.util.Arrays;
import samples.Log;



public class LZJB
{
	private final static int MATCH_BITS = 6;
	private final static int MATCH_MIN = 3;
	private final static int MATCH_MAX = ((1 << MATCH_BITS) + (MATCH_MIN - 1));
	private final static int OFFSET_MASK = ((1 << (16 - MATCH_BITS)) - 1);
	private final static int WINDOW_SIZE = 1024 - 1;


	public static int compress(byte[] aSrcBuffer, byte[] aDstBuffer, int aSrcLen, int aDstLen)
	{
		int src = 0;
		int dst = 0;
		int copymapOffset = 0;
		int copymask = 128;
		int[] refs = new int[WINDOW_SIZE + 1];

		while (src < aSrcLen)
		{
			copymask <<= 1;
			if (copymask == 256)
			{
				copymask = 1;
				copymapOffset = dst;
				aDstBuffer[dst++] = 0;
			}

			if (src > aSrcLen - MATCH_MIN)
			{
				aDstBuffer[dst++] = aSrcBuffer[src++];
				continue;
			}

			int hash = ((0xff & aSrcBuffer[src]) << 16) + ((0xff & aSrcBuffer[src + 1]) << 8) + (0xff & aSrcBuffer[src + 2]);
			hash += hash >> 9;
			hash += hash >> 5;
			hash &= WINDOW_SIZE;

			int offset = (src - refs[hash]) & OFFSET_MASK;

			refs[hash] = src;
			int cpy = src - offset;

			if (cpy >= 0 && cpy + MATCH_MIN < src && aSrcBuffer[src] == aSrcBuffer[cpy] && aSrcBuffer[src + 1] == aSrcBuffer[cpy + 1] && aSrcBuffer[src + 2] == aSrcBuffer[cpy + 2])
			{
				aDstBuffer[copymapOffset] |= copymask;

				int mlen = MATCH_MIN;
				for (; src + mlen < aSrcLen && mlen < MATCH_MAX; mlen++)
				{
					if (aSrcBuffer[src + mlen] != aSrcBuffer[cpy + mlen])
					{
						break;
					}
				}
				aDstBuffer[dst++] = (byte)(((mlen - MATCH_MIN) << (8 - MATCH_BITS)) | (offset >> 8));
				aDstBuffer[dst++] = (byte)offset;
				src += mlen;
			}
			else
			{
				aDstBuffer[dst++] = aSrcBuffer[src++];
			}
		}

		return dst;
	}


	public static void decompress(byte[] aSrcBuffer, byte[] aDstBuffer, int aSrcLen, int aDstLen)
	{
		int src = 0;
		int dst = 0;
		int d_end = aDstLen;
		int copymap = 0;
		int copymask = 128;

		while (dst < d_end)
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
				int cpy = dst - offset;
				if (cpy < 0)
				{
					throw new RuntimeException();
				}
				while (--mlen >= 0 && dst < d_end)
				{
					aDstBuffer[dst++] = aDstBuffer[cpy++];
				}
			}
			else
			{
				aDstBuffer[dst++] = aSrcBuffer[src++];
			}
		}
	}


	public static void main(String... args)
	{
		try
		{
//			byte[] src = "For us, it's a really exciting outcome, because this novel litigation approach worked and would get us a resolution really quickly, and it gave us a way to get our client's data deleted. We were prepared for much more pushback. It's incredibly useful to have this tool in our toolkit for when phones are taken in the future. I can't see any reason why this couldn't be done whenever another traveler is facing this sort of phone seizure.".getBytes();
			byte[] src = "testtest".getBytes();
			byte[] dst = new byte[1024 * 1024];
			byte[] unpack = new byte[src.length];

			int len = compress(src, dst, src.length, dst.length);

			Log.hexDump(Arrays.copyOfRange(dst, 0, len));

			System.out.println(src.length + " / " + len);

			decompress(dst, unpack, dst.length, unpack.length);

			System.out.println(new String(unpack).equals(new String(src)));

			System.out.println(new String(src));
			System.out.println(new String(unpack));
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}
}
