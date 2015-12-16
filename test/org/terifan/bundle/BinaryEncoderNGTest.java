package org.terifan.bundle;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BinaryEncoderNGTest
{
	@Test
	public void testSomeMethod() throws IOException
	{
		Bundle in = Util.createComplexBundle();

		byte[] data = new BinaryEncoder().marshal(in);

		Bundle out = new BinaryDecoder().unmarshal(data);

		assertEquals(in, out);

//		Log.hexDump(data);
	}
}
