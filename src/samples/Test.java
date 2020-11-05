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
//			xml();
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

		System.out.println(bundle.getDoubleArrayList("array"));
	}


//	private static void x() throws IOException
//	{
//		Bundle bundle = new Bundle();
//
//		bundle.putBundlable("object", new _Vector(1, 2, 3));
//		bundle.putBundle("bundle", new _RGB(1, 2, 3));
//
//		System.out.println(bundle);
//	}


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

//		System.out.println("json=" + bundle.toString().length() + ", binaryBundle=" + data.length + ", json+zip=" + baosJSON.size() + ", binaryBundle+zip=" + baosBin.size());

		System.out.println(new Bundle().unmarshal(data).marshalJSON(new StringBuilder(), false));
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


//		System.out.println(bundle);
//
//		System.out.println(new Bundle().unmarshalJSON(bundle.toString()));
//
//		System.out.println((Object)bundle.getBundle("numbers").getArray("ints").get(1));
//		System.out.println(bundle.getBundle("numbers").getIntArrayList("ints").get(1));
//		System.out.println(bundle.getBundle("numbers").getArray("ints").stream().collect(Collectors.averagingDouble(e -> (Integer)e)));
//		System.out.println(bundle.getArray("strings").stream().collect(Collectors.averagingDouble(e -> e == null ? 0 : e.toString().length())));
//		System.out.println(bundle.getSerializable("date1", Date.class));
//		System.out.println(bundle.getDate("date2"));
//		System.out.println(bundle.getUUID("uuid"));
//
////		_RGB color = bundle.getBundlable(_RGB.class, "rgb");
////		System.out.println(color);
////
////		for (Object v : bundle.getArray("colors"))
////		{
////			System.out.println(v);
////		}
////
////		for (_RGB v : bundle.getObjectArray(_RGB.class, "colors"))
////		{
////			System.out.println(v);
////		}
//		System.out.println();
//
//		byte[] data = bundle.marshal();
//
//		Log.hexDump(data);
//		System.out.println();
//
////		PathEvaluation path = new PathEvaluation();
////		PathEvaluation path = new PathEvaluation("colors", 1);
//		PathEvaluation path = new PathEvaluation("arrays", 1, 1);
//
////		Bundle b = new Bundle().unmarshal(data, path);
////		System.out.println(b);
//		ByteArrayOutputStream baosJSON = new ByteArrayOutputStream();
//		DeflaterOutputStream dos = new DeflaterOutputStream(baosJSON);
//		dos.write(bundle.toString().getBytes("utf-8"));
//		dos.close();
//
//		ByteArrayOutputStream baosBin = new ByteArrayOutputStream();
//		DeflaterOutputStream dos2 = new DeflaterOutputStream(baosBin);
//		dos2.write(data);
//		dos2.close();
//
//		System.out.println("json=" + bundle.toString().length() + ", bin=" + data.length + ", zipJSON=" + baosJSON.size() + ", zipBIN=" + baosBin.size());
//
//		for (int v : bundle.getBundle("numbers").getIntArrayList("ints"))
//		{
//			System.out.println(v);
//		}
//
//		for (double v : bundle.getBundle("numbers").getDoubleArrayList("doubles"))
//		{
//			System.out.println(v);
//		}
}
