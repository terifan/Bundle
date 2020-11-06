package org.terifan.bundle;

import java.io.IOException;
import java.util.Random;
import org.testng.annotations.Test;


public class BinaryEncoderNGTest
{
	@Test
	public void testEncode() throws IOException
	{
		Bundle bundle = Helper.createBigBundle(new Random());

 		byte[] data = bundle.marshal();

		Helper.hexDump(data);

		System.out.println(bundle.marshalJSON(false));
	}
}
