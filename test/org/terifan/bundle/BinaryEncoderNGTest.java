package org.terifan.bundle;

import java.io.IOException;
import java.util.Random;
import org.testng.annotations.Test;
import samples.Log;


public class BinaryEncoderNGTest
{
	@Test
	public void testEncode() throws IOException
	{
		Bundle bundle = _Helper.createBigBundle(new Random());

 		byte[] data = bundle.marshal();

		Log.hexDump(data);

		System.out.println(bundle.marshalJSON(false));
	}
}
