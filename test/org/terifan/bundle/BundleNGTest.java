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
		String in = "{\"a\": \"4\", \"b\": null, \"c\": true, \"d\": 123, \"e\": 3.14, \"aa\": [\"4\"], \"bb\": [null], \"cc\": [true], \"dd\": [123], \"ee\": [3.14], \"aaa\": [[\"4\"]], \"bbb\": [[null]], \"ccc\": [[true]], \"ddd\": [[123]], \"eee\": [[3.14]]}";

		Log.out.println(in);

		Bundle bundle = new Bundle().unmarshalPSON(in);

		String out = bundle.marshalPSON();

		Log.out.println(out);

//		assertEquals(in, out);
	}
}
