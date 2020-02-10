package org.terifan.bundle;

import java.io.IOException;
import static java.lang.System.in;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BinaryEncoderNGTest
{
	@Test
	public void testSingleArrayMixedTypes() throws IOException
	{
		Bundle out = new Bundle()
			.putDate("date", new Date())
			.putUUID("uuid", UUID.randomUUID())
			.putCalendar("calendar", new GregorianCalendar())
			.putBundle("bundle", new Bundle().putString("hello", "world"));

		Bundle in = new Bundle().unmarshal(out.marshal());

		assertEquals(out, in);
	}
}
