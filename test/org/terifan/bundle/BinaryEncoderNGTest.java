package org.terifan.bundle;

import java.io.IOException;
import org.terifan.bundle.bundle_test.Log;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BinaryEncoderNGTest
{
	@Test
	public void testSomeMethod() throws IOException
	{
		Bundle in = Util.createComplexBundle();

		byte[] data = new BinaryEncoder().marshal(in);

		Log.out.println(data.length);

		Bundle out = new BinaryDecoder().unmarshal(data);

		assertEquals(in, out);

//		Log.out.println(out);
		
//		Log.hexDump(data);
	}
}
