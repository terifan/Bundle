package org.terifan.bundle;


public class LZJB
{
	private final static int NBBY = 8;
	private final static int MATCH_BITS	= 6;
	private final static int MATCH_MIN = 3;
	private final static int MATCH_MAX = ((1 << MATCH_BITS) + (MATCH_MIN - 1));
	private final static int OFFSET_MASK = ((1 << (16 - MATCH_BITS)) - 1);
//	private final static int WINDOW_SIZE = 1024 - 1;
	private final static int WINDOW_SIZE = 256 - 1;


	public static int compress(byte[] aInput, int aInputOffset, int aInputLength, byte[] aOutput, int aOutputOffset)
	{
		int src = aInputOffset;
		int dst = aOutputOffset;
		int cpy, copymap = 0;
		int copymask = 1 << (NBBY - 1);
		int mlen, offset, hash;
		int hp;
		int [] refs = new int[WINDOW_SIZE+1];

		while (src < aInputLength)
		{
			if ((copymask <<= 1) == (1 << NBBY))
			{
				copymask = 1;
				copymap = dst;
				aOutput[dst++] = 0;
			}

			if (src > aInputLength - MATCH_MAX)
			{
				aOutput[dst++] = aInput[src++];
				continue;
			}

			byte a = aInput[src];
			byte b = aInput[src + 1];
			byte c = aInput[src + 2];

			hash = (a << 16) + (b << 8) + c;
			hash += hash >> 9;
			hash += hash >> 5;

			hp = hash & WINDOW_SIZE;
			offset = (src - refs[hp]) & OFFSET_MASK;
			refs[hp] = src;
			cpy = src - offset;

			if (cpy >= 0 && cpy != src && a == aInput[cpy] && b == aInput[cpy+1] && c == aInput[cpy+2])
			{
				aOutput[copymap] |= copymask;

				for (mlen = MATCH_MIN; mlen < MATCH_MAX; mlen++)
				{
					if (aInput[src+mlen] != aInput[cpy+mlen])
					{
						break;
					}
				}

				aOutput[dst++] = (byte)(((mlen - MATCH_MIN) << (NBBY - MATCH_BITS)) | (offset >> NBBY));
				aOutput[dst++] = (byte)offset;
				src += mlen;
			}
			else
			{
				aOutput[dst++] = a;
				src++;
			}
		}

		return dst;
	}


	public static void decompress(byte[] aInput, int aInputOffset, int aInputLength, byte[] aOutput, int aOutputOffset, int aOutputLength)
	{
		int src = aInputOffset;
		int dst = aOutputOffset;
		int d_end = aOutputLength;
		int copymap = 0;
		int copymask = 1 << (NBBY - 1);

		while (dst < d_end)
		{
			if ((copymask <<= 1) == (1 << NBBY))
			{
				copymask = 1;
				copymap = 255 & aInput[src++];
			}
			if ((copymap & copymask) != 0)
			{
				int mlen = ((255 & aInput[src]) >> (NBBY - MATCH_BITS)) + MATCH_MIN;
				int offset = (((255 & aInput[src]) << NBBY) | (255 & aInput[src+1])) & OFFSET_MASK;
				src += 2;
				int cpy = dst - offset;
				if (cpy < 0)
				{
					throw new IllegalStateException();
				}
				while (--mlen >= 0 && dst < d_end)
				{
					aOutput[dst++] = aOutput[cpy++];
				}
			}
			else
			{
				aOutput[dst++] = aInput[src++];
			}
		}

		if (dst != aOutputOffset + aOutputLength)
		{
			throw new IllegalStateException("Failed to decompress data.");
		}
	}
}


//#define	MATCH_BITS	6
//#define	MATCH_MIN	3
//#define	MATCH_MAX	((1 << MATCH_BITS) + (MATCH_MIN - 1))
//#define	OFFSET_MASK	((1 << (16 - MATCH_BITS)) - 1)
//#define	LEMPEL_SIZE	1024
//
///*ARGSUSED*/
//size_t
//lzjb_compress(void *s_start, void *d_start, size_t s_len, size_t d_len, int n)
//{
//	uchar_t *src = s_start;
//	uchar_t *dst = d_start;
//	uchar_t *cpy, *copymap;
//	int copymask = 1 << (NBBY - 1);
//	int mlen, offset, hash;
//	uint16_t *hp;
//	uint16_t lempel[LEMPEL_SIZE] = { 0 };
//
//	while (src < (uchar_t *)s_start + s_len) {
//		if ((copymask <<= 1) == (1 << NBBY)) {
//			if (dst >= (uchar_t *)d_start + d_len - 1 - 2 * NBBY)
//				return (s_len);
//			copymask = 1;
//			copymap = dst;
//			*dst++ = 0;
//		}
//		if (src > (uchar_t *)s_start + s_len - MATCH_MAX) {
//			*dst++ = *src++;
//			continue;
//		}
//		hash = (src[0] << 16) + (src[1] << 8) + src[2];
//		hash += hash >> 9;
//		hash += hash >> 5;
//		hp = &lempel[hash & (LEMPEL_SIZE - 1)];
//		offset = (intptr_t)(src - *hp) & OFFSET_MASK;
//		*hp = (uint16_t)(uintptr_t)src;
//		cpy = src - offset;
//		if (cpy >= (uchar_t *)s_start && cpy != src &&
//		    src[0] == cpy[0] && src[1] == cpy[1] && src[2] == cpy[2]) {
//			*copymap |= copymask;
//			for (mlen = MATCH_MIN; mlen < MATCH_MAX; mlen++)
//				if (src[mlen] != cpy[mlen])
//					break;
//			*dst++ = ((mlen - MATCH_MIN) << (NBBY - MATCH_BITS)) |
//			    (offset >> NBBY);
//			*dst++ = (uchar_t)offset;
//			src += mlen;
//		} else {
//			*dst++ = *src++;
//		}
//	}
//	return (dst - (uchar_t *)d_start);
//}
//
///*ARGSUSED*/
//int
//lzjb_decompress(void *s_start, void *d_start, size_t s_len, size_t d_len, int n)
//{
//	uchar_t *src = s_start;
//	uchar_t *dst = d_start;
//	uchar_t *d_end = (uchar_t *)d_start + d_len;
//	uchar_t *cpy, copymap;
//	int copymask = 1 << (NBBY - 1);
//
//	while (dst < d_end) {
//		if ((copymask <<= 1) == (1 << NBBY)) {
//			copymask = 1;
//			copymap = *src++;
//		}
//		if (copymap & copymask) {
//			int mlen = (src[0] >> (NBBY - MATCH_BITS)) + MATCH_MIN;
//			int offset = ((src[0] << NBBY) | src[1]) & OFFSET_MASK;
//			src += 2;
//			if ((cpy = dst - offset) < (uchar_t *)d_start)
//				return (-1);
//			while (--mlen >= 0 && dst < d_end)
//				*dst++ = *cpy++;
//		} else {
//			*dst++ = *src++;
//		}
//	}
//	return (0);
//}