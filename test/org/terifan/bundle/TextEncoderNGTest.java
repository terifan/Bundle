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

		String data = new JSONEncoder().marshal(in);

		Log.out.println(data);

		Bundle out = new JSONDecoder().unmarshal(data);

		assertEquals(in, out);
	}
}
