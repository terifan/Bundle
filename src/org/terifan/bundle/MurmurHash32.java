package org.terifan.bundle;


/**
 * Based on the MurmurHash3 algorithm was created by Austin Appleby.
 */
class MurmurHash32
{
	private int h1;
	private int ln;


	public MurmurHash32(int aSeed)
	{
		h1 = aSeed;
	}


	public MurmurHash32 update(int k1)
	{
		k1 *= 0xcc9e2d51;
		k1 = (k1 << 15) | (k1 >>> 17);
		k1 *= 0x1b873593;

		h1 ^= k1;
		h1 = (h1 << 13) | (h1 >>> 19);
		h1 = h1 * 5 + 0xe6546b64;

		ln++;

		return this;
	}


	public MurmurHash32 update(int... aData)
	{
		for (int k1 : aData)
		{
			k1 *= 0xcc9e2d51;
			k1 = (k1 << 15) | (k1 >>> 17);
			k1 *= 0x1b873593;

			h1 ^= k1;
			h1 = (h1 << 13) | (h1 >>> 19);
			h1 = h1 * 5 + 0xe6546b64;

			ln++;
		}

		return this;
	}


	public long finish()
	{
		h1 ^= ln;

		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;

		return h1;
	}


	public MurmurHash32 update(CharSequence aData)
	{
		int k1 = 0;
		int k2;
		int shift = 0;
		int bits;
		int nBytes = 0;

		for (int pos = 0, end = aData.length(); pos < end; )
		{
			int code = aData.charAt(pos++);
			if (code < 0x80)
			{
				k2 = code;
				bits = 8;
			}
			else if (code < 0x800)
			{
				k2 = (0xC0 | (code >> 6)) | ((0x80 | (code & 0x3F)) << 8);
				bits = 16;
			}
			else if (code < 0xD800 || code > 0xDFFF || pos >= end)
			{
				// we check for pos>=end to encode an unpaired surrogate as 3 bytes.
				k2 = (0xE0 | (code >> 12)) | ((0x80 | ((code >> 6) & 0x3F)) << 8) | ((0x80 | (code & 0x3F)) << 16);
				bits = 24;
			}
			else
			{
				// surrogate pair
				// int utf32 = pos < end ? (int) data.charAt(pos++) : 0;
				int utf32 = (int)aData.charAt(pos++);
				utf32 = ((code - 0xD7C0) << 10) + (utf32 & 0x3FF);
				k2 = (0xff & (0xF0 | (utf32 >> 18))) | ((0x80 | ((utf32 >> 12) & 0x3F))) << 8 | ((0x80 | ((utf32 >> 6) & 0x3F))) << 16 | (0x80 | (utf32 & 0x3F)) << 24;
				bits = 32;
			}

			k1 |= k2 << shift;

			// int used_bits = 32 - shift; // how many bits of k2 were used in k1.
			// int unused_bits = bits - used_bits; // (bits-(32-shift)) == bits+shift-32 == bits-newshift

			shift += bits;
			if (shift >= 32)
			{
				// mix after we have a complete word

				k1 *= 0xcc9e2d51;
				k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
				k1 *= 0x1b873593;

				h1 ^= k1;
				h1 = (h1 << 13) | (h1 >>> 19); // ROTL32(h1,13);
				h1 = h1 * 5 + 0xe6546b64;

				shift -= 32;
				// unfortunately, java won't let you shift 32 bits off, so we need to check for 0
				if (shift != 0)
				{
					k1 = k2 >>> (bits - shift); // bits used == bits - newshift
				}
				else
				{
					k1 = 0;
				}
				nBytes += 4;
			}

		} // inner

		// handle tail
		if (shift > 0)
		{
			nBytes += shift >> 3;
			k1 *= 0xcc9e2d51;
			k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
			k1 *= 0x1b873593;
			h1 ^= k1;
		}

		ln += nBytes;

		return this;
	}


	public MurmurHash32 update(byte[] aData)
	{
		int roundedEnd = aData.length & 0xfffffffc; // round down to 4 byte block

		for (int i = 0; i < roundedEnd; i += 4)
		{
			// little endian load order
			int k1 = (aData[i] & 0xff) | ((aData[i + 1] & 0xff) << 8) | ((aData[i + 2] & 0xff) << 16) | (aData[i + 3] << 24);
			k1 *= 0xcc9e2d51;
			k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
			k1 *= 0x1b873593;

			h1 ^= k1;
			h1 = (h1 << 13) | (h1 >>> 19); // ROTL32(h1,13);
			h1 = h1 * 5 + 0xe6546b64;
		}

		// tail
		int k1 = 0;

		switch (aData.length & 0x03)
		{
			case 3:
				k1 = (aData[roundedEnd + 2] & 0xff) << 16;
			// fallthrough
			case 2:
				k1 |= (aData[roundedEnd + 1] & 0xff) << 8;
			// fallthrough
			case 1:
				k1 |= (aData[roundedEnd] & 0xff);
				k1 *= 0xcc9e2d51;
				k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
				k1 *= 0x1b873593;
				h1 ^= k1;
		}

		ln += aData.length;

		return this;
	}


//	public static void main(String ... args)
//	{
//		try
//		{
//			System.out.println(new MurmurHash32(0).update("a").finish());
//			System.out.println(new MurmurHash32(0).update("aa").finish());
//			System.out.println(new MurmurHash32(0).update("aaa").finish());
//			System.out.println(new MurmurHash32(0).update("aaaa").finish());
//			System.out.println(new MurmurHash32(0).update(new byte[0]).finish());
//			System.out.println(new MurmurHash32(0).update(new byte[1]).finish());
//			System.out.println(new MurmurHash32(0).update(new byte[2]).finish());
//			System.out.println(new MurmurHash32(0).update(new byte[3]).finish());
//			System.out.println(new MurmurHash32(0).update(new byte[4]).finish());
//			System.out.println(new MurmurHash32(0).update(new int[0]).finish());
//			System.out.println(new MurmurHash32(0).update(new int[1]).finish());
//			System.out.println(new MurmurHash32(0).update(new int[2]).finish());
//			System.out.println(new MurmurHash32(0).update(new int[3]).finish());
//		}
//		catch (Throwable e)
//		{
//			e.printStackTrace(System.out);
//		}
//	}
}
