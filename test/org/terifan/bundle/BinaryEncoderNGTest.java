package org.terifan.bundle;

import org.terifan.bundle.bundle_test.Log;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BinaryEncoderNGTest
{
	public BinaryEncoderNGTest()
	{
	}


	@Test
	public void testSomeMethod() throws IOException
	{
		Bundle in = Util.createSimpleBundle();
//		Bundle in = Util.createComplexBundle();

		Log.out.println(in);

		byte[] data = new BinaryEncoder().marshal(in);

		Bundle out = new BinaryDecoder().unmarshal(data);

		assertEquals(in, out);

		Log.hexDump(data);
	}
}
