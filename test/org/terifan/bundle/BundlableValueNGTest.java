package org.terifan.bundle;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BundlableValueNGTest
{
	@Test
	public void testBundle() throws IOException
	{
		RGB foreground = new RGB(255, 128, 64);
		RGB background = new RGB(64, 128, 255);

		Bundle bundle = new Bundle();
		bundle.putBundlable("foreground", foreground);
		bundle.putBundlable("background", background);

		assertEquals(bundle.marshalJSON(true), "{\"foreground\":16744512,\"background\":4227327}");
		assertEquals(new Bundle().unmarshal(bundle.marshal()), bundle);
		assertEquals(new Bundle(bundle.marshalJSON(true)), bundle);
		assertEquals(bundle.getBundlable(RGB.class, "foreground"), foreground);
		assertEquals(bundle.getBundlable(RGB.class, "background"), background);
	}


	@Test
	public void testArray() throws IOException
	{
		RGB foreground = new RGB(255, 128, 64);
		RGB background = new RGB(64, 128, 255);

		Array array = new Array();
		array.add(foreground, background);

		assertEquals(array.marshalJSON(true), "[16744512,4227327]");
		assertEquals(array.getBundlable(RGB.class, 0), foreground);
		assertEquals(array.getBundlable(RGB.class, 1), background);
	}


	@Test
	public void testArrayOf() throws IOException
	{
		RGB foreground = new RGB(255, 128, 64);
		RGB background = new RGB(64, 128, 255);

		Array array = Array.of(foreground, background);

		assertEquals(array.marshalJSON(true), "[16744512,4227327]");
		assertEquals(array.getBundlable(RGB.class, 0), foreground);
		assertEquals(array.getBundlable(RGB.class, 1), background);
	}
}
