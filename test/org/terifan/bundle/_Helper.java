package org.terifan.bundle;

import java.util.Date;
import java.util.Random;
import java.util.UUID;


public class _Helper
{
	static Bundle createBigBundle(Random rnd)
	{
		return new Bundle()
			.put("bundle", createBasicBundle(rnd))
			.put("array", createBasicArray(rnd))
			.put("bundleArray", Array.of(createBasicBundle(rnd), null, createBasicBundle(rnd)))
			.put("nestedArray", Array.of(Array.of(1,2,3),null,Array.of(),Array.of(Array.of(4,5,6),Array.of(7,8,9))))
			;
	}


	static Bundle createBasicBundle(Random rnd)
	{
		return new Bundle()
			.putBoolean("boolean", rnd.nextBoolean())
			.putNumber("byte", (byte)rnd.nextInt())
			.putNumber("short", (short)rnd.nextInt())
			.putNumber("int", rnd.nextInt())
			.putNumber("long", rnd.nextLong())
			.putNumber("float", 1000 * rnd.nextFloat())
			.putNumber("double", 1000000 * rnd.nextDouble())
			.putNumber("null", null)
			.putString("string", createString(rnd))
			.putSerializable("serializable", new Date(rnd.nextLong()))
			.putBinary("binary", createBinary(rnd))
			;
	}


	static Array createBasicArray(Random rnd)
	{
		return Array.of(
			rnd.nextBoolean(),
			(byte)rnd.nextInt(),
			(short)rnd.nextInt(),
			rnd.nextInt(),
			rnd.nextLong(),
			1000 * rnd.nextFloat(),
			1000000 * rnd.nextDouble(),
			null,
			createString(rnd),
			new Date(rnd.nextLong()),
			new UUID(rnd.nextLong(), rnd.nextLong())
			);
	}


	static String createString(Random rnd)
	{
		String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
		StringBuilder sb = new StringBuilder();
		for (int i = 5 + rnd.nextInt(40); --i >= 0;)
		{
			sb.append(s.charAt(rnd.nextInt(s.length())));
		}
		return sb.toString();
	}


	static byte[] createBinary(Random rnd)
	{
		byte[] bytes = new byte[5 + rnd.nextInt(40)];
		rnd.nextBytes(bytes);
		return bytes;
	}
}
