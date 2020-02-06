package org.terifan.bundle;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.TimeZone;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BundleNGTest
{
	@Test
	public void testMarshalBasicTypes() throws IOException
	{
		Bundle in = new Bundle()
			.putBoolean("booleanNull", null)
			.putBoolean("boolean1", false)
			.putBoolean("boolean2", true)
			.putNumber("numberNull", null)
			.putNumber("byte1", Byte.MIN_VALUE)
			.putNumber("byte2", Byte.MAX_VALUE)
			.putNumber("short1", Short.MIN_VALUE)
			.putNumber("short2", Short.MAX_VALUE)
			.putNumber("int1", Integer.MIN_VALUE)
			.putNumber("int2", Integer.MAX_VALUE)
			.putNumber("long1", Long.MIN_VALUE)
			.putNumber("long2", Long.MAX_VALUE)
			.putNumber("float1", Float.MIN_VALUE)
			.putNumber("float2", Float.MAX_VALUE)
			.putNumber("double1", Double.MIN_VALUE)
			.putNumber("double2", Double.MAX_VALUE)
			.putString("StringNull", null)
			.putString("stringASCII", "text")
			.putString("stringUTF", "åäö")
			.putArray("bytes", Array.of(new byte[10]))
			;

		byte[] data = in.marshal();

		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));

		assertEquals(out.getBoolean("booleanNull"), null);
		assertEquals((boolean)out.getBoolean("boolean1"), false);
		assertEquals((boolean)out.getBoolean("boolean2"), true);
		assertEquals(out.getByteArray("bytes"), new byte[10]);
	}


	@Test
	public void testToArray() throws IOException
	{
		Bundle in = new Bundle().putArray("array", new Array().add(1,2,3));
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		Object[] array = out.toArray("array");

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(array[0], 1);
		assertEquals(array[1], 2);
		assertEquals(array[2], 3);
	}


	@Test
	public void testBundableObjectConstructor() throws IOException
	{
		Bundle in = new Bundle(new RGB(64,128,255));
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals((int)out.getInt("r"), 64);
		assertEquals((int)out.getInt("g"), 128);
		assertEquals((int)out.getInt("b"), 255);
	}


	@Test
	public void testBundableValue() throws IOException
	{
		RGB rgb = new RGB(64,128,255);

		Bundle in = new Bundle().putObject("rgb", rgb);
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.getInt("rgb"), rgb.writeExternal());
	}


	@Test
	public void testAsObject() throws IOException
	{
		RGB in = new RGB(64,128,255);

		Bundle bundle = new Bundle().putNumber("r", in.getRed()).putNumber("g", in.getGreen()).putNumber("b", in.getBlue());

		RGB out = bundle.newInstance(RGB.class);

		assertEquals(out, in);
		assertEquals(bundle.marshalJSON(true), "{\"r\":64,\"g\":128,\"b\":255}");
	}


	@Test
	public void testOfAndAsObject() throws IOException
	{
		RGB in = new RGB(64,128,255);

		Bundle bundle = Bundle.of(in);

		RGB out = bundle.newInstance(RGB.class);

		assertEquals(out, in);
		assertEquals(bundle.marshalJSON(true), "{\"r\":64,\"g\":128,\"b\":255}");
	}


	@Test
	public void testMarshalSerializable() throws IOException
	{
		TimeZone tz = TimeZone.getDefault();

		Bundle out = new Bundle().putSerializable("tz", tz);

		byte[] data = out.marshal();
		
		Bundle in = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.getSerializable(TimeZone.class, "tz"), in.getSerializable(TimeZone.class, "tz"));
	}


	@Test
	public void testMarshalSerializable2() throws IOException
	{
		TestObject tz = new TestObject();
		tz.data = new byte[1000000];
		new Random().nextBytes(tz.data);

		Bundle out = new Bundle().putSerializable("tz", tz);

		byte[] data = out.marshal();
		
		Bundle in = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.getSerializable(TestObject.class, "tz"), in.getSerializable(TestObject.class, "tz"));
	}
	
	
	private static class TestObject implements Serializable
	{
		private final static long serialVersionUID = 1L;
		
		byte[] data;


		@Override
		public int hashCode()
		{
			int hash = 7;
			hash = 79 * hash + Arrays.hashCode(this.data);
			return hash;
		}


		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			final TestObject other = (TestObject)obj;
			if (!Arrays.equals(this.data, other.data))
			{
				return false;
			}
			return true;
		}
	}
}
