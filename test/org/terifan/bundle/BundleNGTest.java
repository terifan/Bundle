package org.terifan.bundle;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BundleNGTest
{
	@Test
	public void testMarshal() throws IOException
	{
		Bundle in = Util.createComplexBundle();

		byte[] data = in.marshal();

		Log.hexDump(data);

		Bundle out = new Bundle().unmarshal(data);

		assertEquals(in, out);
	}


	@Test
	public void testMarshalPSON() throws IOException
	{
		Bundle in = Util.createComplexBundle();

		String data = in.marshalPSON();

		Log.out.println(data);

		Bundle out = new Bundle().unmarshalPSON(data);

		assertEquals(in, out);
	}


	@Test
	public void testMarshalPSON2() throws IOException
	{
		String in = "{"
			+ "\"a\": \"s\", \"aa\": [\"s\", \"t\"], \"aaa\": [[\"s\", \"t\"], [\"u\", \"v\"]]"
			+ ", \"b\": null, \"bb\": [null, null], \"bbb\": [[null, null], [null]]"
			+ ", \"c\": true, \"cc\": [true, false], \"ccc\": [[true, false], [true]]"
			+ ", \"d\": 123, \"dd\": [123, 456], \"ddd\": [[123, 456], [789, 12]]"
			+ ", \"e\": 3.14, \"ee\": [3.14, 9.72], \"eee\": [[3.14, 9.72], [-3.14, 0.72]]"
			+ "}";

//		Log.out.println(in);

		Bundle bundle = new Bundle().unmarshalPSON(in);

		String out = bundle.marshalPSON(true);

//		Log.out.println(out);

		assertEquals(in, out);
	}
}
