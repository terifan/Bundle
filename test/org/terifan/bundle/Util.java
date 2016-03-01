package org.terifan.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.UUID;


public class Util
{
	static Bundle createComplexBundle()
	{
		Bundle bundle = new Bundle()
//			.putIntArray("null", null)
			.putBundle("bundle", createSimpleBundle())
			.putBundleArray("bundle_array", createSimpleBundle(), createSimpleBundle())
			.putBundleArrayList("bundle_list", new ArrayList<>(Arrays.asList(createSimpleBundle(), createSimpleBundle())))
			.putBundleMatrix("bundle_matrix", new Bundle[][]{{createSimpleBundle(), createSimpleBundle(), createSimpleBundle()}, null, {createSimpleBundle(), createSimpleBundle()}, {}})
			.putBundleMatrix("bundle_matrix_sq", new Bundle[][]{{createSimpleBundle(), createSimpleBundle()},{createSimpleBundle(), createSimpleBundle()}})
		;

		return bundle;
	}


	static Bundle createSimpleBundle()
	{
		Random r = new Random();

		Bundle bundle = new Bundle()
			.putBoolean("boolean", r.nextBoolean())
			.putBooleanArray("boolean_array", r.nextBoolean(), r.nextBoolean(), r.nextBoolean())
			.putBooleanArrayList("boolean_list", new ArrayList<>(Arrays.asList(r.nextBoolean(), r.nextBoolean(), null, r.nextBoolean())))
			.putBooleanMatrix("boolean_matrix", new boolean[][]{{r.nextBoolean(), r.nextBoolean(), r.nextBoolean()}, null, {r.nextBoolean(), r.nextBoolean()}, {}})
			.putBooleanMatrix("boolean_matrix_sq", new boolean[][]{{r.nextBoolean(), r.nextBoolean(), r.nextBoolean()}, {r.nextBoolean(), r.nextBoolean(), r.nextBoolean()}})

			.putByte("byte", (byte)r.nextInt())
			.putByteArray("byte_array", (byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt())
			.putByteArrayList("byte_list", new ArrayList<>(Arrays.asList((byte)r.nextInt(), (byte)r.nextInt(), null, (byte)r.nextInt())))
			.putByteMatrix("byte_matrix", new byte[][]{{(byte)r.nextInt(), (byte)r.nextInt()}, null, {(byte)r.nextInt(), (byte)r.nextInt()}, {}})
			.putByteMatrix("byte_matrix_sq", new byte[][]{{(byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt()}, {(byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt()}})

			.putShort("short", (short)r.nextInt())
			.putShortArray("short_array", (short)r.nextInt(), (short)r.nextInt(), (short)r.nextInt())
			.putShortArrayList("short_list", new ArrayList<>(Arrays.asList((short)r.nextInt(), (short)r.nextInt(), null, (short)r.nextInt())))
			.putShortMatrix("short_matrix", new short[][]{{(short)r.nextInt(), (short)r.nextInt()},null,{(short)r.nextInt(), (short)r.nextInt()}, {}})
			.putShortMatrix("short_matrix_sq", new short[][]{{(short)r.nextInt(), (short)r.nextInt(), (short)r.nextInt()}, {(short)r.nextInt(), (short)r.nextInt(), (short)r.nextInt()}})

			.putChar("char", (char)r.nextInt())
			.putCharArray("char_array", (char)r.nextInt(), (char)r.nextInt(), (char)r.nextInt())
			.putCharArrayList("char_list", new ArrayList<>(Arrays.asList((char)r.nextInt(), (char)r.nextInt(), null, (char)r.nextInt())))
			.putCharMatrix("char_matrix", new char[][]{{(char)r.nextInt(), (char)r.nextInt(), (char)r.nextInt()}, null, {(char)r.nextInt(), (char)r.nextInt()}, {}})
			.putCharMatrix("char_matrix_sq", new char[][]{{(char)r.nextInt(), (char)r.nextInt(), (char)r.nextInt()}, {(char)r.nextInt(), (char)r.nextInt(), (char)r.nextInt()}})

			.putInt("int", r.nextInt())
			.putIntArray("int_array", r.nextInt(), r.nextInt(), r.nextInt())
			.putIntArray("int_array_empty")
			.putIntArrayList("int_list", new ArrayList<>(Arrays.asList(r.nextInt(), r.nextInt(), null, r.nextInt())))
			.putIntArrayList("int_list_empty", new ArrayList<>())
			.putIntMatrix("int_matrix", new int[][]{{r.nextInt(),r.nextInt(),r.nextInt()},null,{r.nextInt(),r.nextInt()}, {}})
			.putIntMatrix("int_matrix_sq", new int[][]{{r.nextInt(),r.nextInt()},{r.nextInt(),r.nextInt()}})
			.putIntMatrix("int_matrix_empty", new int[][]{{}})

			.putLong("long", r.nextLong())
			.putLongArray("long_array", r.nextLong(), r.nextLong(), r.nextLong())
			.putLongArrayList("long_list", new ArrayList<>(Arrays.asList(r.nextLong(), r.nextLong(), null, r.nextLong())))
			.putLongMatrix("long_matrix", new long[][]{{r.nextLong(),r.nextLong(),r.nextLong()},null,{r.nextLong(),r.nextLong()}, {}})
			.putLongMatrix("long_matrix_sq", new long[][]{{r.nextLong(),r.nextLong(),r.nextLong()},{r.nextLong(),r.nextLong(),r.nextLong()}})

			.putFloat("float", r.nextFloat())
			.putFloatArray("float_array", r.nextFloat(), r.nextFloat(), r.nextFloat())
			.putFloatArrayList("float_list", new ArrayList<>(Arrays.asList(r.nextFloat(), r.nextFloat(), null, r.nextFloat())))
			.putFloatMatrix("float_matrix", new float[][]{{r.nextFloat(),r.nextFloat(),r.nextFloat()}, null, {r.nextFloat(),r.nextFloat()}, {}})
			.putFloatMatrix("float_matrix_sq", new float[][]{{r.nextFloat(),r.nextFloat(),r.nextFloat()},{r.nextFloat(),r.nextFloat()}})

			.putDouble("double", r.nextDouble())
			.putDoubleArray("double_array", r.nextDouble(), r.nextDouble(), r.nextDouble())
			.putDoubleArrayList("double_list", new ArrayList<>(Arrays.asList(r.nextDouble(), r.nextDouble(), null, r.nextDouble())))
			.putDoubleMatrix("double_matrix", new double[][]{{r.nextDouble(),r.nextDouble(),r.nextDouble()}, null, {r.nextDouble(),r.nextDouble()}, {}})
			.putDoubleMatrix("double_matrix_sq", new double[][]{{r.nextDouble(),r.nextDouble(),r.nextDouble()},{r.nextDouble(),r.nextDouble()}})

			.putDate("date_null", null)
			.putDate("date", new Date(r.nextLong() & 0x3FFFFFFFFFFL))
			.putDateArray("date_array", new Date(r.nextLong() & 0x3FFFFFFFFFFL), new Date(r.nextLong() & 0x3FFFFFFFFFFL), null)
			.putDateArrayList("date_list", new ArrayList<>(Arrays.asList(new Date(r.nextLong() & 0x3FFFFFFFFFFL), new Date(r.nextLong() & 0x3FFFFFFFFFFL), null)))

			.putString("string_null", null)
			.putString("string", randomString(r))
			.putStringArray("string_array", randomString(r), randomString(r), null)
			.putStringArrayList("string_list", new ArrayList<>(Arrays.asList(randomString(r), randomString(r), null)))
			.putStringMatrix("string_matrix", new String[][]{{randomString(r),randomString(r),randomString(r)}, null, {randomString(r)}, {}})
			.putStringMatrix("string_matrix_sq", new String[][]{{randomString(r),randomString(r),randomString(r)}, {randomString(r),randomString(r),randomString(r)}})

			.putBundle("bundle_null", null)
			.putBundle("bundle", new Bundle()
				.putInt("one", r.nextInt())
				.putString("two", randomString(r))
				.putBundle("bundle", new Bundle()
					.putInt("three", r.nextInt())
				)
			)

			.putSerializable("serializable_null", null)
			.putSerializable("serializable", new GregorianCalendar())
			.putSerializableArray("serializable_array", new GregorianCalendar(), null, UUID.randomUUID())
			.putSerializableArrayList("serializable_arraylist", new ArrayList<>(Arrays.asList(new GregorianCalendar())))
			.putSerializableMatrix("serializable_matrix", new UUID[][]{{UUID.randomUUID(),UUID.randomUUID()},{UUID.randomUUID(),UUID.randomUUID()}})
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
