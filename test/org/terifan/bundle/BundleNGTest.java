package org.terifan.bundle;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import samples._PersonEntity;
import samples._RGB;
import samples._Vector;


public class BundleNGTest
{
	@Test
	public void testBoolean() throws IOException
	{
		for (Boolean value : new Boolean[]{null, true, false})
		{
			Bundle out = new Bundle()
				.putBoolean("value", value);

			Bundle in = new Bundle().unmarshal(out.marshal());

			assertEquals(in.getBoolean("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			assertEquals(in.getBoolean("value"), value);
		}
	}


	@Test
	public void testByte() throws IOException
	{
		for (Byte value : new Byte[]{null, Byte.MIN_VALUE, Byte.MAX_VALUE})
		{
			Bundle out = new Bundle()
				.putNumber("value", value);

			Bundle in = new Bundle().unmarshal(out.marshal());

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getByte("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getByte("value"), value);
		}
	}


	@Test
	public void testShort() throws IOException
	{
		for (Short value : new Short[]{null, Short.MIN_VALUE, Short.MAX_VALUE})
		{
			Bundle out = new Bundle()
				.putNumber("value", value);

			byte[] data = out.marshal();

			Bundle in = new Bundle().unmarshal(data);

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getShort("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getShort("value"), value);
		}
	}


	@Test
	public void testInt() throws IOException
	{
		for (Integer value : new Integer[]{null, Integer.MIN_VALUE, Integer.MAX_VALUE})
		{
			Bundle out = new Bundle()
				.putNumber("value", value);

			byte[] data = out.marshal();

			Bundle in = new Bundle().unmarshal(data);

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getInt("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getInt("value"), value);
		}
	}


	@Test
	public void testLong() throws IOException
	{
		for (Long value : new Long[]{null, Long.MIN_VALUE, Long.MAX_VALUE})
		{
			Bundle out = new Bundle()
				.putNumber("value", value);

			byte[] data = out.marshal();

			Bundle in = new Bundle().unmarshal(data);

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getLong("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getLong("value"), value);
		}
	}


	@Test
	public void testFloat() throws IOException
	{
		for (Float value : new Float[]{null, (float)Math.PI, -Float.MAX_VALUE, Float.MAX_VALUE})
		{
			Bundle out = new Bundle()
				.putNumber("value", value);

			byte[] data = out.marshal();

			Bundle in = new Bundle().unmarshal(data);

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getFloat("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			if (value != null)
			{
				assertEquals(in.getNumber("value").floatValue(), value); // JSON decoder only parse Doubles
				assertEquals(in.getFloat("value"), value);
			}
			else
			{
				assertNull(in.getFloat("value"));
			}
		}
	}


	@Test
	public void testDouble() throws IOException
	{
		for (Double value : new Double[]{null, Math.PI, -Double.MAX_VALUE, Double.MAX_VALUE})
		{
			Bundle out = new Bundle()
				.putNumber("value", value);

			byte[] data = out.marshal();

			Bundle in = new Bundle().unmarshal(data);

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getDouble("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			assertEquals(in.getNumber("value"), value);
			assertEquals(in.getDouble("value"), value);
		}
	}


	@Test
	public void testString() throws IOException
	{
		for (String value : new String[]{null, "", "text", "first'second", "first\"second"})
		{
			Bundle out = new Bundle().putString("value", value);

			String data = out.marshalJSON(true);

			Bundle in = new Bundle().unmarshalJSON(data);

			assertEquals(in.getString("value"), value);

			in = new Bundle().unmarshalJSON(out.marshalJSON(true));

			assertEquals(in.getString("value"), value);
		}
	}


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
		assertEquals(out.getByteArray("bytes"), Arrays.asList((byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0));
	}


	@Test
	public void testToArray() throws IOException
	{
		Bundle in = new Bundle().putArray("array", Array.of(1,2,3));
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
		Bundle in = new Bundle(new _RGB(64,128,255));
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
	public void testAsObject() throws IOException
	{
		_RGB in = new _RGB(64,128,255);

		Bundle bundle = new Bundle().putNumber("r", in.getRed()).putNumber("g", in.getGreen()).putNumber("b", in.getBlue());

		_RGB out = bundle.newInstance(_RGB.class);

		assertEquals(out, in);
		assertEquals(bundle.marshalJSON(true), "{\"r\":64,\"g\":128,\"b\":255}");
	}


	@Test
	public void testOfAndAsObject() throws IOException
	{
		_Vector in = new _Vector(64,128,255);

		Bundle bundle = Bundle.of(in);

		_Vector out = bundle.newInstance(_Vector.class);

		assertEquals(out, in);
		assertEquals(bundle.marshalJSON(true), "{\"x\":64.0,\"y\":128.0,\"z\":255.0}");
	}


	@Test
	public void testMarshalSerializable() throws IOException
	{
		_RGB tz = new _RGB(1,2,3);

		Bundle out = new Bundle().putSerializable("color", tz);

		byte[] data = out.marshal();

		Bundle in = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.getSerializable(_RGB.class, "color"), in.getSerializable(_RGB.class, "color"));
	}


	@Test
	public void testMarshalSerializable2() throws IOException
	{
		Block block = new Block();
		block.data = new byte[1000000];
		new Random().nextBytes(block.data);

		Bundle out = new Bundle().putSerializable("block", block);

		byte[] data = out.marshal();

		Bundle in = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.getSerializable(Block.class, "block"), in.getSerializable(Block.class, "block"));
	}


	@Test
	public void testSerializable() throws IOException
	{
		Point point = new Point(0,0);

		Bundle out = new Bundle().putSerializable("point", point);

		Point in = out.getSerializable(Point.class, "point");

		assertEquals(point, in);
	}


	@Test
	public void testBinary() throws IOException
	{
		byte[] data = "binarydata".getBytes();

		Bundle out = new Bundle().putBinary("data", data);

		byte[] in = out.getBinary("data");

		assertEquals(data, in);
		assertEquals(out.marshalJSON(true), "{\"data\":\"YmluYXJ5ZGF0YQ==\"}");
	}


	@Test
	public void testGetByteArray() throws IOException
	{
		Array data = Array.of((byte)0,(byte)100,(byte)-100);

		Bundle out = new Bundle().putArray("values", data);

		ArrayList<Byte> in = out.getByteArray("values");

		assertEquals(data, in);
	}


	@Test
	public void testGetShortArray() throws IOException
	{
		Array data = Array.of((short)0,(short)100,(short)10000);

		Bundle out = new Bundle().putArray("values", data);

		ArrayList<Short> in = out.getShortArray("values");

		assertEquals(data, in);
	}


	@Test
	public void testGetIntArray() throws IOException
	{
		Array ints = Array.of(0,100,10000);

		Array data = Array.of((short)0,(short)100,(short)10000);

		Bundle out = new Bundle().putArray("values", data);

		ArrayList<Integer> in = out.getIntArray("values");

		assertEquals(ints, in);
	}


	@Test
	public void testGetNumberArray() throws IOException
	{
		Array data = Array.of((byte)0,(short)1000,100000,Math.PI);

		Bundle out = new Bundle().putArray("values", data);

		out = new Bundle().unmarshal(out.marshal()); // BinaryDecoder decodes all types

		ArrayList<Number> in = out.getNumberArray("values");

		assertEquals(data, in);

		out = new Bundle().unmarshalJSON(out.marshalJSON(true)); // JSONDecoder decodes numbers to their most narrow boxing instance

		in = out.getNumberArray("values");

		assertEquals(data, in);
	}



	@Test
	public void testJavaObjectSerializationOutput() throws IOException, ClassNotFoundException
	{
		_PersonEntity outEntity = new _PersonEntity("Adam", new Date(1965,10,7), 180, 90);

		Bundle out = new Bundle();
		out.putSerializable("object", outEntity);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			oos.writeObject(out);
		}

		byte[] data = baos.toByteArray();

		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data)))
		{
			Bundle in = (Bundle)ois.readObject();

			assertEquals(in, out);

			_PersonEntity inEntity = in.getSerializable(_PersonEntity.class, "object");

			assertEquals(inEntity, outEntity);
		}
	}
}
