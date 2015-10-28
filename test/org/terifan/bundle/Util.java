package org.terifan.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;


public class Util
{
	static Bundle createComplexBundle()
	{
		Bundle bundle = new Bundle()
			.putBundle("bundle", createSimpleBundle())
			.putBundleArray("bundle_array", createSimpleBundle(), createSimpleBundle())
			.putBundleArrayList("bundle_list", new ArrayList<>(Arrays.asList(createSimpleBundle(), createSimpleBundle())))
			.putBundleMatrix("bundle_matrix_sq", new Bundle[][]{{createSimpleBundle(), createSimpleBundle()},{createSimpleBundle(), createSimpleBundle()}})
		;

		return bundle;
	}


	static Bundle createSimpleBundle()
	{
		Random r = new Random();

		Bundle bundle = new Bundle()
			.putIntArrayList("empty_list", new ArrayList())

			.putBoolean("boolean", r.nextBoolean())
			.putBooleanArray("boolean_array", r.nextBoolean(), r.nextBoolean(), r.nextBoolean())

			.putByte("byte", (byte)r.nextInt())
			.putByteArray("byte_array", (byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt())
			.putByteMatrix("byte_matrix", new byte[][]{{(byte)r.nextInt(), (byte)r.nextInt()},null,{(byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt()}})
			.putByteMatrix("byte_matrix_sq", new byte[][]{{(byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt()},{(byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt()}})

			.putShort("short", (short)r.nextInt())
			.putShortArray("short_array", (short)r.nextInt(), (short)r.nextInt(), (short)r.nextInt())

			.putChar("char", (char)r.nextInt())
			.putCharArray("char_array", (char)r.nextInt(), (char)r.nextInt(), (char)r.nextInt())

			.putInt("int", r.nextInt())
			.putIntArray("int_array", r.nextInt(), r.nextInt(), r.nextInt())
			.putIntArrayList("int_list", new ArrayList<>(Arrays.asList(r.nextInt(), r.nextInt(), r.nextInt())))
			.putIntMatrix("int_matrix", new int[][]{{r.nextInt(),r.nextInt()},null,{r.nextInt(),r.nextInt(),r.nextInt()}})
			.putIntMatrix("int_matrix_sq", new int[][]{{r.nextInt(),r.nextInt()},{r.nextInt(),r.nextInt()}})

			.putLong("long", r.nextLong())
			.putLongArray("long_array", r.nextLong(), r.nextLong(), r.nextLong())
			.putLongMatrix("long_matrix_sq", new long[][]{{r.nextLong(),r.nextLong()},{r.nextLong(),r.nextLong()}})

			.putFloat("float", r.nextFloat())
			.putFloatArray("float_array", r.nextFloat(), r.nextFloat(), r.nextFloat())
			.putFloatMatrix("float_matrix_sq", new float[][]{{r.nextFloat(),r.nextFloat()},{r.nextFloat(),r.nextFloat()}})

			.putDouble("double", r.nextDouble())
			.putDoubleArray("double_array", r.nextDouble(), r.nextDouble(), r.nextDouble())
			.putDoubleMatrix("double_matrix_sq", new double[][]{{r.nextDouble(),r.nextDouble()},{r.nextDouble(),r.nextDouble()}})

			.putDate("date", new Date(r.nextLong() & -1))
			.putDateArray("date_array", new Date(r.nextLong() & -1), new Date(r.nextLong() & -1), null)

			.putString("string_null", null)
			.putString("string", randomString(r))
			.putStringArray("string_array", randomString(r), randomString(r), null)
			.putStringArrayList("string_list", new ArrayList<>(Arrays.asList(randomString(r), randomString(r), null)))
			.putStringMatrix("string_matrix", new String[][]{{randomString(r),randomString(r)},null,{randomString(r)},{}})
			.putStringMatrix("string_matrix_sq", new String[][]{{randomString(r),randomString(r)},{randomString(r),randomString(r)}})

			.putBundle("bundle", new Bundle()
				.putInt("one", r.nextInt())
				.putString("two", randomString(r))
				.putBundle("bundle", new Bundle()
					.putInt("three", r.nextInt())
				)
			)
			;

		return bundle;
	}


	static String randomString(Random r)
	{
		char[] chr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789 åäöüÅÄÖÜéè€".toCharArray();

		StringBuilder s = new StringBuilder();
		for (int i = 1 + r.nextInt(20); --i >= 0;)
		{
			s.append(chr[r.nextInt(chr.length)]);
		}
		return s.toString();
	}
}
