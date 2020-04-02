package samples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DeflaterOutputStream;
import org.terifan.bundle.Array;
import org.terifan.bundle.PathEvaluation;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.Bundlable;


public class Test
{
	public static void main(String... args)
	{
		try
		{
//			y();
//			x();
			xml();
//			big();
//			small();
//			array();
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static void y() throws IOException
	{
		Bundle bundle = new Bundle();

		bundle.putArray("array", Array.of("1",2,3f,4.0,5));

		System.out.println(bundle);

		System.out.println(bundle.getDoubleArray("array"));
	}


	private static void x() throws IOException
	{
		Bundle bundle = new Bundle();

		bundle.putBundlable("object", new _Vector(1, 2, 3));
		bundle.putBundle("bundle", new _RGB(1, 2, 3));

		System.out.println(bundle);
	}


	private static void xml() throws IOException
	{
		Bundle bundle = new Bundle();

		bundle.unmarshalXML(Test.class.getResourceAsStream("test2.xml"));

		byte[] data = bundle.marshal();

//		Log.hexDump(data);

		ByteArrayOutputStream baosJSON = new ByteArrayOutputStream();
		DeflaterOutputStream dos = new DeflaterOutputStream(baosJSON);
		dos.write(bundle.toString().getBytes("utf-8"));
		dos.close();

		ByteArrayOutputStream baosBin = new ByteArrayOutputStream();
		DeflaterOutputStream dos2 = new DeflaterOutputStream(baosBin);
		dos2.write(data);
		dos2.close();

		System.out.println("json=" + bundle.toString().length() + ", binaryBundle=" + data.length + ", json+zip=" + baosJSON.size() + ", binaryBundle+zip=" + baosBin.size());

//		System.out.println(new Bundle().unmarshal(data).marshalJSON(new StringBuilder(), false));
	}


	private static void small() throws IOException
	{
		Bundle bundle = new Bundle()
			.putArray("BSON", Array.of("awesome", 5.05, 1986));

		byte[] data = bundle.marshal();

		Log.hexDump(data);

		System.out.println(new Bundle().unmarshal(data));
	}


	private static void array() throws IOException
	{
		Array array = new Array().add("awesome", 5.05, 1986);

		byte[] data = array.marshal();
		String json = array.marshalJSON(true);

		Log.hexDump(data);

		System.out.println(json);
		System.out.println(new Array().unmarshalJSON(json));
		System.out.println(new Array().unmarshal(data));
	}


	private static void big() throws Exception
	{
		Bundle bundle = new Bundle()
			.putBundle("numbers", new Bundle()
				.putNumber("number", 7)
				.putArray("ints", new Array().add(1, 4, 9))
				.putArray("doubles", new Array().add(1.3).add(2.2).add(3.1))
			)
			.putNumber("byte", (byte)7)
			.putNumber("short", (short)7777)
			.putNumber("int", 654984)
			.putNumber("long", 164516191981L)
			.putNumber("float", 7.2f)
			.putNumber("double", 3.14)
			.putArray("arrays", new Array().add("horse", new Array().add("monkey", "pig"), 777, new Array().add("girl", "boy")))
			.putArray("strings", new Array().add("a", null).add("b").add("c"))
			.putString("null", null)
			.putBoolean("boolean", true)
			.putArray("booleans", new Array().add(true).add(false).add(true))
			.putArray("bundles", new Array().add(new Bundle().putString("key", "value")))
			.putBundle("bundle", new Bundle().putString("key", "value"))
			.putString("string", "text")
			.putBundle("color", new _RGB(196, 128, 20))
			.putArray("colors", new Array().add(new _RGB(196, 128, 20), new _RGB(96, 128, 220)))
			.putBundlable("rgb", new _RGB(196, 128, 20))
			.putArray("rgbs", new Array().add(new _RGB(196, 128, 20), new _RGB(96, 128, 220)))
			.putSerializable("date1", new Date())
			.putDate("date2", new Date())
			.putBinary("binary", "test".getBytes())
			.putUUID("uuid", UUID.randomUUID())
			.putCalendar("calendar", new GregorianCalendar(TimeZone.getTimeZone("CET")))
			.putArray("empty", new Array())
			.putArray("big", new Array().add("test", new Bundle().putString("a", "A").putString("b", "B").putString("c", "C"), new Bundle().putString("a", "A").putString("b", "B").putString("c", "C").putArray("d", new Array().add(1, 2, 3))));

		System.out.println(bundle);

		System.out.println(new Bundle().unmarshalJSON(bundle.toString()));

		System.out.println((Object)bundle.getBundle("numbers").getArray("ints").get(1));
		System.out.println(bundle.getBundle("numbers").toArray("ints")[1]);
		System.out.println(bundle.getBundle("numbers").getArray("ints").stream().collect(Collectors.averagingDouble(e -> (Integer)e)));
		System.out.println(bundle.getArray("strings").stream().collect(Collectors.averagingDouble(e -> e == null ? 0 : e.toString().length())));
		System.out.println(bundle.getSerializable(Date.class, "date1"));
		System.out.println(bundle.getDate("date2"));
		System.out.println(bundle.getUUID("uuid"));

		_RGB color = bundle.getBundlable(_RGB.class, "rgb");
		System.out.println(color);

		for (Object v : bundle.getArray("colors"))
		{
			System.out.println(v);
		}

		for (_RGB v : bundle.getObjectArray(_RGB.class, "colors"))
		{
			System.out.println(v);
		}

		System.out.println();

		byte[] data = bundle.marshal();

		Log.hexDump(data);
		System.out.println();

//		PathEvaluation path = new PathEvaluation();
//		PathEvaluation path = new PathEvaluation("colors", 1);
		PathEvaluation path = new PathEvaluation("arrays", 1, 1);

//		Bundle b = new Bundle().unmarshal(data, path);
//		System.out.println(b);
		ByteArrayOutputStream baosJSON = new ByteArrayOutputStream();
		DeflaterOutputStream dos = new DeflaterOutputStream(baosJSON);
		dos.write(bundle.toString().getBytes("utf-8"));
		dos.close();

		ByteArrayOutputStream baosBin = new ByteArrayOutputStream();
		DeflaterOutputStream dos2 = new DeflaterOutputStream(baosBin);
		dos2.write(data);
		dos2.close();

		System.out.println("json=" + bundle.toString().length() + ", bin=" + data.length + ", zipJSON=" + baosJSON.size() + ", zipBIN=" + baosBin.size());

		for (int v : bundle.getBundle("numbers").getIntArray("ints"))
		{
			System.out.println(v);
		}

		for (double v : bundle.getBundle("numbers").getDoubleArray("doubles"))
		{
			System.out.println(v);
		}
	}
}
