package org.terifan.bundle;

import java.awt.Color;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class ImporterNGTest
{
	@Test
	public void testImporter() throws IOException
	{
		Bundle in = new Bundle("{\"r\":64,\"g\":128,\"b\":255}");

		Importer<Color> importer = b->new Color(b.getInt("r"), b.getInt("g"), b.getInt("b"));

		Color color = in.newInstance(Color.class, importer);

		assertEquals(color.getRed(), (int)in.getInt("r"));
		assertEquals(color.getGreen(), (int)in.getInt("g"));
		assertEquals(color.getBlue(), (int)in.getInt("b"));
	}
}
