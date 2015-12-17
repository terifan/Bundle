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

		byte[] data = in.marshal();

//		Log.hexDump(data);

		Bundle out = new Bundle().unmarshal(data);

		assertEquals(in, out);
	}
}
