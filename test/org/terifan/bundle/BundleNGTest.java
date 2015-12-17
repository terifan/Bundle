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

//		Log.hexDump(data);

		Bundle out = new Bundle().unmarshal(data);

		assertEquals(in, out);
	}

	
	@Test
	public void testMarshalJSON() throws IOException
	{
		Bundle in = Util.createComplexBundle();

		String data = in.marshalJSON();

//		Log.out.println(data);

		Bundle out = new Bundle().unmarshalJSON(data);

		assertEquals(in, out);
	}
}
