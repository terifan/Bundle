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
import java.util.List;
import java.util.Random;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


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
		assertEquals(out.getByteArrayList("bytes"), Arrays.asList((byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)0));
	}


//	@Test
//	public void testToArray() throws IOException
//	{
//		Bundle in = new Bundle().putArray("array", Array.of(1,2,3));
//		byte[] data = in.marshal();
//		Bundle out = new Bundle().unmarshal(data);
//
//		Object[] array = out.toArray("array");
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(array[0], 1);
//		assertEquals(array[1], 2);
//		assertEquals(array[2], 3);
//	}


	@Test
	public void testMarshalSerializable2() throws IOException
	{
		_Block block = new _Block();
		block.data = new byte[1000000];
		new Random().nextBytes(block.data);

		Bundle out = new Bundle().putSerializable("block", block);

		byte[] data = out.marshal();

		Bundle in = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.getSerializable("block", _Block.class), in.getSerializable("block", _Block.class));
	}


	@Test
	public void testSerializable() throws IOException
	{
		Point point = new Point(0,0);

		Bundle out = new Bundle().putSerializable("point", point);

		Point in = out.getSerializable("point", Point.class);

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

		ArrayList<Byte> in = out.getByteArrayList("values");

		assertEquals(data, in);
	}


	@Test
	public void testGetShortArray() throws IOException
	{
		Array data = Array.of((short)0,(short)100,(short)10000);

		Bundle out = new Bundle().putArray("values", data);

		ArrayList<Short> in = out.getShortArrayList("values");

		assertEquals(data, in);
	}


	@Test
	public void testGetIntArray() throws IOException
	{
		Array ints = Array.of(0,100,10000);

		Array data = Array.of((short)0,(short)100,(short)10000);

		Bundle out = new Bundle().putArray("values", data);

		ArrayList<Integer> in = out.getIntArrayList("values");

		assertEquals(ints, in);
	}


	@Test
	public void testGetNumberArray() throws IOException
	{
		Array data = Array.of((byte)0,(short)1000,100000,Math.PI);

		Bundle out = new Bundle().putArray("values", data);

		out = new Bundle().unmarshal(out.marshal()); // BinaryDecoder decodes all types

		ArrayList<Number> in = out.getNumberArrayList("values");

		assertEquals(data, in);

		out = new Bundle().unmarshalJSON(out.marshalJSON(true)); // JSONDecoder decodes numbers to their most narrow boxing instance

		in = out.getNumberArrayList("values");

		assertEquals(data, in);
	}



	@Test
	public void testJavaObjectSerializationOutput() throws IOException, ClassNotFoundException
	{
		Date outEntity = new Date();

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

			Date inEntity = in.getSerializable("object", Date.class);

			assertEquals(inEntity, outEntity);
		}
	}


	@Test
	public void testBundlable()
	{
		_Color colIn = new _Color(1,2,3);
		_Vector vecIn = new _Vector(1,2,3);
		_Triangle triIn = new _Triangle(new _Vector[]{new _Vector(1,2,3), new _Vector(4,5,6), new _Vector(7,8,9)}, new _Color[]{new _Color(11, 12, 13), new _Color(14, 15, 16), new _Color(17, 18, 19)});

		Bundle bundle = new Bundle();
		bundle.putBundlable("#col", colIn);
		bundle.putBundlable("#vec", vecIn);
		bundle.putBundlable("#tri", triIn);

		Array array = Array.of(colIn, vecIn, triIn);

		_Color colOut = bundle.getBundlable("#col", _Color.class);
		_Vector vecOut = bundle.getBundlable("#vec", _Vector.class);
		_Triangle triOut = bundle.getBundlable("#tri", _Triangle.class);

		assertEquals(colOut.toString(), colIn.toString());
		assertEquals(vecOut.toString(), vecIn.toString());
		assertEquals(triOut.toString(), triIn.toString());

		assertEquals(bundle.toString(), "{\"#col\":{\"r\":1,\"g\":2,\"b\":3},\"#vec\":[1,2,3],\"#tri\":[[[1,2,3],[4,5,6],[7,8,9]],[{\"r\":11,\"g\":12,\"b\":13},{\"r\":14,\"g\":15,\"b\":16},{\"r\":17,\"g\":18,\"b\":19}]]}");
		assertEquals(array.toString(), "[{\"r\":1,\"g\":2,\"b\":3},[1,2,3],[[[1,2,3],[4,5,6],[7,8,9]],[{\"r\":11,\"g\":12,\"b\":13},{\"r\":14,\"g\":15,\"b\":16},{\"r\":17,\"g\":18,\"b\":19}]]]");

		colOut = array.getBundlable(0, _Color.class);
		vecOut = array.getBundlable(1, _Vector.class);
		triOut = array.getBundlable(2, _Triangle.class);

		assertEquals(colOut.toString(), colIn.toString());
		assertEquals(vecOut.toString(), vecIn.toString());
		assertEquals(triOut.toString(), triIn.toString());
	}


	@Test
	public void testBundlableArrayList()
	{
		_Triangle triIn1 = new _Triangle(new _Vector[]{new _Vector(0.1,0.2,0.3), new _Vector(0.4,0.5,0.6), new _Vector(0.7,0.8,0.9)}, new _Color[]{new _Color(11, 12, 13), new _Color(14, 15, 16), new _Color(17, 18, 19)});
		_Triangle triIn2 = new _Triangle(new _Vector[]{new _Vector(1.1,1.2,1.3), new _Vector(1.4,1.5,1.6), new _Vector(1.7,1.8,1.9)}, new _Color[]{new _Color(21, 22, 23), new _Color(24, 25, 26), new _Color(27, 28, 29)});
		_Model modIn = new _Model(triIn1, triIn2);

		Bundle bundle = new Bundle();
		bundle.putBundlable("model", modIn);

		_Model modOut = bundle.getBundlable("model", _Model.class);

		assertEquals(modOut.toString(), modIn.toString());
	}


	@Test
	public void testBundlableSupplier()
	{
		_Vector in = new _Vector(0.1,0.2,0.3);

		Bundle bundle = new Bundle();
		bundle.putBundlable("v", in);

		_Vector out = bundle.getBundlable("v", _Vector::new);

		assertEquals(out.toString(), in.toString());
	}


	@Test
	public void testBundlableArrayListSupplier()
	{
		List<_Vector> in = Arrays.asList(new _Vector(0.1,0.2,0.3), new _Vector(0.4,0.5,0.6));

		Bundle bundle = new Bundle();
		bundle.putBundlableArrayList("v", in);

		ArrayList<_Vector> out = bundle.getBundlableArrayList("v", _Vector::new);

		assertEquals(out.toString(), in.toString());
	}
}
