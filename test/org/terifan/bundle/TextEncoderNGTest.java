package org.terifan.bundle;

import java.io.IOException;
import org.terifan.bundle.bundle_test.Log;
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
		Bundle in = Util.createSimpleBundle();
//		Bundle in = Util.createComplexBundle();

		String data = new TextEncoder().marshal(in);

//		Bundle out = new TextDecoder().unmarshal(data);

		Log.out.println(data);

//		assertEquals(in, out);
	}
}
