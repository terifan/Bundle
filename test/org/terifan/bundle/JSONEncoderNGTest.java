package org.terifan.bundle;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class JSONEncoderNGTest
{
	@Test
	public void testSomeMethod() throws IOException
	{
		Bundle in = Util.createComplexBundle();

		String data = in.marshalJSON();

//		Log.out.println(data);

		Bundle out = new Bundle().unmarshalJSON(data);

		assertEquals(in, out);
	}
}
