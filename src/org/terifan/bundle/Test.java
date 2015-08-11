package org.terifan.bundle;


public class Test
{
//	public static void main(String ... args)
//	{
//		try
//		{
//			Bundle bundle = newBundle();
//
//			String expected = new TextEncoder().marshal(bundle, true);
//			Log.out.println(expected);
//
//			byte[] data = new BinaryEncoder().marshal(bundle);
//			Debug.hexDump(data);
//
//			Bundle unbundled = new BinaryDecoder().unmarshal(data);
//
//			String actual = new TextEncoder().marshal(unbundled, true);
//			Log.out.println(actual);
//
//			Log.out.println(new TextEncoder().marshal(unbundled));
//
//			ByteArrayOutputStream zip = new ByteArrayOutputStream();
//			try (DeflaterOutputStream dos = new DeflaterOutputStream(zip))
//			{
//				dos.write(data);
//			}
//
//			Log.out.println();
//			Log.out.println("result: " + expected.equals(actual));
//			Log.out.println("length: " + data.length + " bytes (zip: " + zip.size() + ")");
//		}
//		catch (Throwable e)
//		{
//			e.printStackTrace(System.out);
//		}
//	}
//
//
//	private static Bundle newBundle()
//	{
//		return newSingleBundle()
//			.putBundle("bundle", newSingleBundle())
//			.putBundleArray("bundle-array", newSingleBundle(), null, new Bundle())
//			.putBundleArrayList("bundle-arraylist", new ArrayList<>(Arrays.asList(newSingleBundle(), null, new Bundle())))
//			;
//	}
//
//
//	private static Bundle newSingleBundle()
//	{
//		return new Bundle()
//			.putBoolean("boolean", true)
//			.putByte("byte", 127)
//			.putShort("short", -654)
//			.putChar("char", 'x')
//			.putInt("int", 654321)
//			.putLong("long", 987654321L)
//			.putFloat("float", 321.321f)
//			.putDouble("double", 654321.654321)
//			.putDate("date-1", new Date(0))
//			.putDate("date-2", null)
//			.putString("string-1", "hello")
//			.putString("string-2", null)
//			.putString("string-3", "")
//
//			.putBooleanArray("boolean-array", true, false)
//			.putByteArray("byte-array", Byte.MAX_VALUE, Byte.MIN_VALUE, (byte)0x00, (byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x77, (byte)0x88, (byte)0x99, (byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd, (byte)0xee, (byte)0xff)
//			.putShortArray("short-array", Short.MAX_VALUE, (short)0, Short.MIN_VALUE)
//			.putCharArray("char-array", Character.MAX_VALUE, (char)0, Character.MIN_VALUE)
//			.putIntArray("int-array", Integer.MAX_VALUE, 0, Integer.MIN_VALUE)
//			.putLongArray("long-array", Long.MAX_VALUE, 0L, Long.MIN_VALUE)
//			.putFloatArray("float-array", 321.321f, 0f, -321.321f)
//			.putDoubleArray("double-array", 654321.654321, 0.0, -654321.654321)
//			.putDateArray("date-array", new Date(0), null, new Date())
//			.putStringArray("string-array-1", "hello", null, "", "world")
//			.putStringArray("string-array-2")
//			.putStringArray("string-array-3", null)
//
//			.putBooleanArrayList("boolean-arraylist", new ArrayList<>(Arrays.asList(true, false)))
//			.putByteArrayList("byte-arraylist", new ArrayList<>(Arrays.asList(Byte.MAX_VALUE, (byte)0, Byte.MIN_VALUE)))
//			.putShortArrayList("short-arraylist", new ArrayList<>(Arrays.asList(Short.MAX_VALUE, (short)0, Short.MIN_VALUE)))
//			.putCharArrayList("char-arraylist", new ArrayList<>(Arrays.asList(Character.MAX_VALUE, (char)0, Character.MIN_VALUE)))
//			.putIntArrayList("int-arraylist", new ArrayList<>(Arrays.asList(Integer.MAX_VALUE, 0, Integer.MIN_VALUE)))
//			.putLongArrayList("long-arraylist", new ArrayList<>(Arrays.asList(Long.MAX_VALUE, 0L, Long.MIN_VALUE)))
//			.putFloatArrayList("float-arraylist", new ArrayList<>(Arrays.asList(321.321f, 0f, -321.321f)))
//			.putDoubleArrayList("double-arraylist", new ArrayList<>(Arrays.asList(654321.654321, 0.0, -654321.654321)))
//			.putDateArrayList("date-arraylist", new ArrayList<>(Arrays.asList(new Date(0), null, new Date())))
//			.putStringArrayList("string-arraylist-1", new ArrayList<>(Arrays.asList("hello", null, "", "world")))
//			.putStringArrayList("string-arraylist-2", new ArrayList<>())
//			.putStringArrayList("string-arraylist-3", null)
//			;
//	}
}
