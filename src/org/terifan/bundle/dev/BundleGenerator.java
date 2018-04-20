package org.terifan.bundle.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import org.terifan.bundle.Bundle;


class BundleGenerator
{
	public static Bundle create()
	{
		return new Bundle()
			.putBundle("bundle-one", new Bundle()
				.putBundle("bundle-two", createBundle()
					.putBundle("last", createSingleBundle(11))
				)
				.putBundle("last-two", createSingleBundle(12))
			)
			.putBundle("last-one", createSingleBundle(13))
			;
	}


	public static Bundle createBundle()
	{
		return new Bundle()
			.putBundle("bundle", createSingleBundle(0))
			.putBundleArray("bundleArray", createSingleBundle(1), createSingleBundle(2), createSingleBundle(3))
			.putBundleArrayList("bundleArrayList", new ArrayList<>(Arrays.asList(createSingleBundle(4), createSingleBundle(5), createSingleBundle(6))))
			.putBundleMatrix("bundleMatrix", new Bundle[][]{{createSingleBundle(7), createSingleBundle(8), createSingleBundle(9)}, {}, {createSingleBundle(10)}})
			;
	}


	public static Bundle createSingleBundle(int aSeed)
	{
		Random rnd = new Random(aSeed);

		boolean[] longBooleanArray = new boolean[10000];
		byte[] longByteArray = new byte[10000];
		int[] longIntArray = new int[10000];
		long[] longLongArray = new long[10000];
		Date[] longDateArray = new Date[10000];
		double[] longDoubleArray = new double[10000];
		String[] longStringArray = new String[10000];

		for (int i = 0; i < longBooleanArray.length; i++) longBooleanArray[i] = rnd.nextBoolean();
		for (int i = 0; i < longByteArray.length; i++) longByteArray[i] = (byte)rnd.nextInt();
		for (int i = 0; i < longIntArray.length; i++) longIntArray[i] = rnd.nextInt();
		for (int i = 0; i < longLongArray.length; i++) longLongArray[i] = rnd.nextLong();
		for (int i = 0; i < longDoubleArray.length; i++) longDoubleArray[i] = rnd.nextDouble();
		for (int i = 0; i < longDateArray.length; i++) longDateArray[i] = new Date(rnd.nextLong());
		for (int i = 0; i < longStringArray.length; i++) longStringArray[i] = "" + rnd.nextLong();

		ArrayList<Byte> byteArrayList = new ArrayList<>(Arrays.asList((byte)12, (byte)17, (byte)100, (byte)0));
		ArrayList<Integer> intArrayList = new ArrayList<>(Arrays.asList(-16412, 17, 19324981, 0));
		ArrayList<Boolean> booleanArrayList = new ArrayList<>(Arrays.asList((Boolean)rnd.nextBoolean(),(Boolean)rnd.nextBoolean(),null,(Boolean)rnd.nextBoolean(),(Boolean)rnd.nextBoolean(),(Boolean)rnd.nextBoolean()));
		ArrayList<String> stringArrayList = new ArrayList<>(Arrays.asList("dog", "cat", "tiger", ""));
		ArrayList<Double> doubleArrayList = new ArrayList<>(Arrays.asList(-16412.0, 17.0, 19324981.0, 0.0));
		ArrayList<Long> longArrayList = new ArrayList<>(Arrays.asList(-16412L, 17L, 19324981L, 0L));
		ArrayList<Date> dateArrayList = new ArrayList<>(Arrays.asList(new Date(1529161967184L), new Date(1529321987167L), new Date(0L)));

		return new Bundle()
			.putBoolean("boolean", rnd.nextBoolean())
			.putBooleanArray("booleanArray", rnd.nextBoolean(),false,rnd.nextBoolean(),false,false,rnd.nextBoolean())
			.putBooleanArrayList("booleanArrayList", booleanArrayList)
			.putBooleanMatrix("booleanMatrix", new boolean[][]{{rnd.nextBoolean(),false},{false,rnd.nextBoolean()},{rnd.nextBoolean(),rnd.nextBoolean(),rnd.nextBoolean(),rnd.nextBoolean()},null,{},{false}})
			.putBooleanArray("booleanArrayLarge", longBooleanArray)
			.putByte("byte", 12)
			.putByteArray("byteArray", new byte[]{12, 17, 100, 0})
			.putByteArrayList("byteArrayList", byteArrayList)
			.putByteMatrix("byteMatrix", new byte[][]{{12,17,100,0},{},null,{0}})
			.putByteArray("byteArrayLarge", longByteArray)
			.putInt("int", 12)
			.putIntArray("intArray", new int[]{-16412, 17, 19324981, 0})
			.putIntArrayList("intArrayList", intArrayList)
			.putIntMatrix("intMatrix", new int[][]{{-16412, 17, 19324981,0},{},null,{0}})
			.putIntArray("intArrayLarge", longIntArray)
			.putLong("long", 12)
			.putLongArray("longArray", new long[]{-16412, 17, 19324981, 0})
			.putLongArrayList("longArrayList", longArrayList)
			.putLongMatrix("longMatrix", new long[][]{{-16412, 17, 19324981,0},{},null,{0}})
			.putLongArray("longArrayLarge", longLongArray)
			.putDouble("double", 12)
			.putDoubleArray("doubleArray", new double[]{-16412, 17, 19324981, 0})
			.putDoubleArrayList("doubleArrayList", doubleArrayList)
			.putDoubleMatrix("doubleMatrix", new double[][]{{-16412, 17, 19324981,0},{},null,{0}})
			.putDoubleArray("doubleArrayLarge", longDoubleArray)
			.putString("string", "str")
			.putStringArray("stringArray", new String[]{"short", "longer longer longer longer longer longer longer longer longer longer longer longer longer longer longer longer longer", ""})
			.putStringArrayList("stringArrayList", stringArrayList)
			.putStringMatrix("stringMatrix", new String[][]{{"dog", "cat", "tiger", ""},{},null,{"final"}})
			.putStringArray("stringArrayLarge", longStringArray)
			.putStringArray("stringLarge", new String(new byte[10000]))
			.putDate("date", new Date(1529321987167L))
			.putDateArray("dateArray", new Date[]{new Date(1529161967184L), new Date(1529321987167L), new Date(0L)})
			.putDateArrayList("dateArrayList", dateArrayList)
			.putDateMatrix("dateMatrix", new Date[][]{{new Date(1529161967184L), new Date(1529321987167L), new Date(0L)},{},null,{new Date(1529161967184L)}})
			.putDateArray("dateArrayLarge", longDateArray)
			;
	}
}
