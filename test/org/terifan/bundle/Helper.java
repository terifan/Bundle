package org.terifan.bundle;

import java.util.Date;
import java.util.Random;
import java.util.UUID;


public class Helper
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


	public static void hexDump(byte[] aBuffer)
	{
		int LW = 56;
		int MR = 1000;

		StringBuilder binText = new StringBuilder("");
		StringBuilder hexText = new StringBuilder("");

		for (int row = 0, offset = 0; offset < aBuffer.length && row < MR; row++)
		{
			hexText.append(String.format("%04d: ", row * LW));

			int padding = 3 * LW + LW / 8;

			for (int i = 0; offset < aBuffer.length && i < LW; i++)
			{
				int c = 0xff & aBuffer[offset++];

				hexText.append(String.format("%02x ", c));
				binText.append(Character.isISOControl(c) ? '.' : (char)c);
				padding -= 3;

				if ((i & 7) == 7)
				{
					hexText.append(" ");
					padding--;
				}
			}

			for (int i = 0; i < padding; i++)
			{
				hexText.append(" ");
			}

			System.out.println(hexText.append(binText).toString());

			binText.setLength(0);
			hexText.setLength(0);
		}
	}
}
