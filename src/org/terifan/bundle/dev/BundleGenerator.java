package org.terifan.bundle.dev;

import java.util.Date;
import java.util.Random;
import org.terifan.bundle.Array;
import org.terifan.bundle.Bundle;


class BundleGenerator
{
//	public static Bundle create()
//	{
//		return new Bundle()
//			.putBundle("bundle-one", new Bundle()
//				.putBundle("bundle-two", createBundle()
//					.putBundle("last", createSingleBundle(11))
//				)
//				.putBundle("last-two", createSingleBundle(12))
//			)
//			.putBundle("last-one", createSingleBundle(13))
//			;
//	}
//
//
//	public static Bundle createBundle()
//	{
//		return new Bundle()
//			.putBundle("bundle", createSingleBundle(0))
//			.putArray("bundleArray", Array.of(createSingleBundle(1), createSingleBundle(2), createSingleBundle(3)))
//			;
//	}
//
//
//	public static Bundle createSingleBundle(int aSeed)
//	{
//		Random rnd = new Random(aSeed);
//
//		boolean[] longBooleanArray = new boolean[10000];
//		byte[] longByteArray = new byte[10000];
//		int[] longIntArray = new int[10000];
//		long[] longLongArray = new long[10000];
//		Date[] longDateArray = new Date[10000];
//		double[] longDoubleArray = new double[10000];
//		String[] longStringArray = new String[10000];
//
//		for (int i = 0; i < longBooleanArray.length; i++) longBooleanArray[i] = rnd.nextBoolean();
//		for (int i = 0; i < longByteArray.length; i++) longByteArray[i] = (byte)rnd.nextInt();
//		for (int i = 0; i < longIntArray.length; i++) longIntArray[i] = rnd.nextInt();
//		for (int i = 0; i < longLongArray.length; i++) longLongArray[i] = rnd.nextLong();
//		for (int i = 0; i < longDoubleArray.length; i++) longDoubleArray[i] = rnd.nextDouble();
//		for (int i = 0; i < longDateArray.length; i++) longDateArray[i] = new Date(rnd.nextLong());
//		for (int i = 0; i < longStringArray.length; i++) longStringArray[i] = "" + rnd.nextLong();
//
//		return new Bundle()
//			.putBoolean("boolean", rnd.nextBoolean())
//			.putArray("booleanArray", Array.of(rnd.nextBoolean(),false,rnd.nextBoolean(),false,false,rnd.nextBoolean()))
//			.putArray("booleanArrayLarge", Array.of(longBooleanArray))
//			.putNumber("byte", 12)
//			.putArray("byteArray", Array.of(new byte[]{12, 17, 100, 0}))
//			.putArray("byteArrayLarge", Array.of(longByteArray))
//			.putNumber("int", 12)
//			.putArray("intArray", Array.of(new int[]{-16412, 17, 19324981, 0}))
//			.putArray("intArrayLarge", Array.of(longIntArray))
//			.putNumber("long", 12)
//			.putArray("longArray", Array.of(new long[]{-16412, 17, 19324981, 0}))
//			.putArray("longArrayLarge", Array.of(longLongArray))
//			.putNumber("double", 12)
//			.putArray("doubleArray", Array.of(new double[]{-16412, 17, 19324981, 0}))
//			.putArray("doubleArrayLarge", Array.of(longDoubleArray))
//			.putString("string", "str")
//			.putArray("stringArray", Array.of(new String[]{"short", "longer longer longer longer longer longer longer longer longer longer longer longer longer longer longer longer longer", ""}))
//			.putArray("stringArrayLarge", Array.of(longStringArray))
//			.putString("stringLarge", new String(new byte[10000]))
//			.putDate("date", new Date(1529321987167L))
//			.putArray("dateArray", Array.of(new Date[]{new Date(1529161967184L), new Date(1529321987167L), new Date(0L)}))
//			.putArray("dateArrayLarge", Array.of(longDateArray))
//			;
//	}
}
