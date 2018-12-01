package org.terifan.bundle;

import java.awt.Color;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class ExporterNGTest
{
	@Test
	public void testExporter() throws IOException
	{
		Color color = new Color(64,128,255);

		Exporter<Color> fn = (b,c)->b.putNumber("r", c.getRed()).putNumber("g",c.getGreen()).putNumber("b",c.getBlue());

		Bundle in = Bundle.of(color, fn);

		assertEquals(color.getRed(), (int)in.getInt("r"));
		assertEquals(color.getGreen(), (int)in.getInt("g"));
		assertEquals(color.getBlue(), (int)in.getInt("b"));
	}
}
