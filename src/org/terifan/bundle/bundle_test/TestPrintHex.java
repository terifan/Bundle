package org.terifan.bundle.bundle_test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.terifan.bundle.BinaryEncoder;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.ConvertXml;


public class TestPrintHex
{
	public static void main(String ... args)
	{
		try
		{
			byte[] xml;
			try (InputStream in = TestPrintHex.class.getResourceAsStream("lxir.xml"))
			{
				xml = TestXml.fetch(in);
			}

			Bundle bundle = new Bundle();
			ConvertXml.unmarshal(new ByteArrayInputStream(xml), bundle);

			BinaryEncoder binaryEncoder = new BinaryEncoder();
			Log.hexDump(binaryEncoder.marshal(bundle));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
