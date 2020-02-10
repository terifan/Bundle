package org.terifan.bundle;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class ContainerNGTest
{
	@Test
	public void testGetDefault() throws IOException
	{
		Bundle in = new Bundle().putString("one", "ONE");

		assertEquals(in.get("one", "MISSING"), "ONE");
		assertEquals(in.get("two", "MISSING"), "MISSING");
	}
}
