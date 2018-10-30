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
import org.terifan.bundle.BinaryDecoder.PathEvaluation;
import org.terifan.bundle.BinaryEncoder;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.BundlableValue;
import org.terifan.bundle.Bundlable;


public class Test
{
	public static void main(String... args)
	{
		try
		{
//			xml();
			big();
//			small();
//			array();
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static void xml() throws IOException
	{
		Bundle bundle = new Bundle();

		bundle.unmarshalXML(Test.class.getResourceAsStream("test.xml"), true);

		byte[] data = bundle.marshal();

		Log.hexDump(data);

		System.out.println(new Bundle().unmarshal(data).marshalJSON(new StringBuilder(), false));
	}


	private static void small() throws IOException
	{
		Bundle bundle = new Bundle()
			.putArray("BSON", new Array().add("awesome", 5.05, 1986))
			;

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
			.putBundle("color", new Color(196,128,20))
			.putArray("colors", new Array().add(new Color(196,128,20), new Color(96,128,220)))
			.putObject("rgb", new Color(196,128,20))
			.putArray("rgbs", new Array().add(new Color(196,128,20), new Color(96,128,220)))
			.putObject("values", new PackedArray(96,128,220))
			.putSerializable("date1", new Date())
			.putDate("date2", new Date())
			.putBinary("binary", "test".getBytes())
			.putUUID("uuid", UUID.randomUUID())
			.putCalendar("calendar", new GregorianCalendar(TimeZone.getTimeZone("CET")))
			.putArray("empty", new Array())
			.putArray("big", new Array().add("test", new Bundle().putString("a","A").putString("b","B").putString("c","C"), new Bundle().putString("a","A").putString("b","B").putString("c","C").putArray("d", new Array().add(1,2,3))))
		;

		System.out.println(bundle);

		System.out.println(new Bundle().unmarshalJSON(bundle.toString()));

		System.out.println(bundle.getBundle("numbers").getArray("ints").get(1));
		System.out.println(bundle.getBundle("numbers").toArray("ints")[1]);
		System.out.println(bundle.getBundle("numbers").getArray("ints").stream().collect(Collectors.averagingDouble(e->(Integer)e)));
		System.out.println(bundle.getArray("strings").stream().collect(Collectors.averagingDouble(e->e==null?0:e.toString().length())));
		System.out.println(bundle.getSerializable(Date.class, "date1"));
		System.out.println(bundle.getDate("date2"));
		System.out.println(bundle.getUUID("uuid"));

		Color color = bundle.getObject(Color.class, "rgb");
		System.out.println(color);

		PackedArray pa = bundle.getObject(PackedArray.class, "values");
		System.out.println(pa);

		for (Object v : bundle.getArray("colors"))
		{
			System.out.println(v);
		}

		for (Color v : bundle.getObjectArray(Color.class, "colors"))
		{
			System.out.println(v);
		}

		System.out.println();

		byte[] data = new BinaryEncoder().marshal(bundle);

		Log.hexDump(data);
		System.out.println();

//		PathEvaluation path = new PathEvaluation();
//		PathEvaluation path = new PathEvaluation("colors", 1);
		PathEvaluation path = new PathEvaluation("arrays", 1, 1);

		Bundle b = new Bundle().unmarshal(data, path);
		System.out.println(b);

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


	static class Color implements Bundlable, BundlableValue<Integer>
	{
		private int r, g, b;


		public Color()
		{
		}


		public Color(int aR, int aG, int aB)
		{
			this.r = aR;
			this.g = aG;
			this.b = aB;
		}


		@Override
		public void readExternal(Bundle aBundle)
		{
			r = aBundle.getInt("r");
			g = aBundle.getInt("g");
			b = aBundle.getInt("b");
		}


		@Override
		public void writeExternal(Bundle aBundle)
		{
			aBundle.putNumber("r", r);
			aBundle.putNumber("g", g);
			aBundle.putNumber("b", b);
		}


		@Override
		public void readExternal(Integer aValue)
		{
			r = 0xff & (aValue >> 16);
			g = 0xff & (aValue >> 8);
			b = 0xff & (aValue);
		}


		@Override
		public Integer writeExternal()
		{
			return (r << 16) + (g << 8) + b;
		}


		@Override
		public String toString()
		{
			return "Color{r=" + r + ", g=" + g + ", b=" + b + '}';
		}
	}


	static class PackedArray implements BundlableValue<String>
	{
		private int[] mValues;


		public PackedArray()
		{
		}


		public PackedArray(int... aValues)
		{
			mValues = aValues;
		}


		@Override
		public void readExternal(String aValue)
		{
			mValues = Stream.of(aValue.split(" ")).mapToInt(Integer::parseInt).toArray();
		}


		@Override
		public String writeExternal()
		{
			return Arrays.stream(mValues).mapToObj(Integer::toString).collect(Collectors.joining(" "));
		}


		@Override
		public String toString()
		{
			return "PackedArray{mValues=" + Arrays.toString(mValues) + '}';
		}
	}
}
