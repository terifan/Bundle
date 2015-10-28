package org.terifan.bundle;

import org.terifan.bundle.bundle_test.Log;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class TextEncoderNGTest
{
	public TextEncoderNGTest()
	{
	}


	@Test
	public void testSomeMethod() throws IOException
	{
//		Bundle in = Util.createSimpleBundle();
		Bundle in = Util.createComplexBundle();

		String data = new TextEncoder().marshal(in);

		Bundle out = new TextDecoder().unmarshal(data);

		assertEquals(in, out);

		Log.out.println(data);
	}
}
