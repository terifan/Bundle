package org.terifan.bundle;

import java.io.IOException;
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
//			.putNumber("int2", Integer.MAX_VALUE)
//			.putNumber("long1", Long.MIN_VALUE)
//			.putNumber("long2", Long.MAX_VALUE)
			.putNumber("float1", Float.MIN_VALUE)
//			.putNumber("float2", Float.MAX_VALUE)
//			.putNumber("double1", Double.MIN_VALUE)
//			.putNumber("double2", Double.MAX_VALUE)
			.putString("StringNull", null)
			.putString("stringASCII", "text")
			.putString("stringUTF", "åäö")
			;

		byte[] data = in.marshal();

		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out, in);
	}


	@Test
	public void testToArray() throws IOException
	{
		Bundle in = new Bundle().putArray("array", new Array().add("TWO"));
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.toArray("array")[0], "TWO");
	}


	@Test
	public void testArray1() throws IOException
	{
		Bundle in = new Bundle().putArray("array", new Array().add("TWO"));
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.getArray("array").get(0), "TWO");
	}


	@Test
	public void testString1() throws IOException
	{
		Bundle in = new Bundle().putString("one", "ONE").putArray("array", new Array().add("TWO"));
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.getString("one"), "ONE");
		assertEquals(out.getArray("array").get(0), "TWO");
	}


	@Test
	public void testBundableObjectConstructor() throws IOException
	{
		Bundle in = new Bundle(new Color(64,128,255));
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		assertEquals((int)out.getInt("r"), 64);
		assertEquals((int)out.getInt("g"), 128);
		assertEquals((int)out.getInt("b"), 255);
	}


	@Test
	public void testBundableValue() throws IOException
	{
		Color color = new Color(64,128,255);

		Bundle in = new Bundle().putObject("rgb", color);
		byte[] data = in.marshal();
		Bundle out = new Bundle().unmarshal(data);

		assertEquals(out.getInt("rgb"), color.writeExternal());
	}
}
