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
			.putBoolean("booleanNull", false)
			.putBoolean("boolean1", false)
			.putBoolean("boolean2", true)
			.putNumber("numberNull", null)
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
			.putString("stringUTF", "åäö");

		byte[] data = in.marshal();
//		Log.hexDump(data);

		assertEquals(in, new Bundle(data));
	}


	@Test
	public void testBundableObjectConstructor() throws IOException
	{
		Bundle in = new Bundle(new Color(64,128,255));

		assertEquals((int)in.getInt("r"), 64);
		assertEquals((int)in.getInt("g"), 128);
		assertEquals((int)in.getInt("b"), 255);
	}


	@Test
	public void testBundableValue() throws IOException
	{
		Bundle in = new Bundle().putObject("rgb", new Color(64,128,255));

		assertEquals(in.getInt("rgb"), new Color(64,128,255).writeExternal());
	}
}
