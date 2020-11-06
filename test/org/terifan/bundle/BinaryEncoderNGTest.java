package org.terifan.bundle;

import java.io.IOException;
import java.util.Random;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;


public class BinaryEncoderNGTest
{
	@Test
	public void testEncode() throws IOException
	{
		Bundle out = Helper.createBigBundle(new Random());

 		byte[] data = out.marshal();

//		Helper.hexDump(data);
//		System.out.println(in.marshalJSON(false));

		Bundle in = new Bundle().unmarshal(data);

		assertEquals(out, in);
	}
}
