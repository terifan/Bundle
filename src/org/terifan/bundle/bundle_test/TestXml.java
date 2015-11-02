package org.terifan.bundle.bundle_test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.terifan.bundle.BinaryEncoder;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.ConvertXml;
import org.terifan.bundle.TextDecoder;
import org.terifan.bundle.TextEncoder;
import org.xml.sax.SAXException;


public class TestXml
{
	public static void main(String ... args)
	{
		try
		{
			int delta = 0;

			delta += test("tiny.xml", 133);
			delta += test("lxir.xml", 466);
			delta += test("params.xml", 725);
			delta += test("fo.xml", 2238);
			delta += test("ctts.xml", 1143);
			delta += test("lynx.xml", 1250);
			delta += test("oms1.xml", 24798);
			delta += test("oms2.xml", 19302);
			delta += test("edoc.xml", 119825);
			delta += test("capimil1.xml", 10915);
			delta += test("capimil2.xml", 13225);

			Log.out.println("---------------------------------------------------------------------------------------------");
			Log.out.println(delta);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static int test(String aFilename, int aExpectedSize) throws SAXException, IOException, ParserConfigurationException
	{
		byte[] xmlData;
		try (InputStream in = TestXml.class.getResourceAsStream(aFilename))
		{
			xmlData = fetch(in);
		}

		Bundle bundle = new Bundle();

		if (aFilename.endsWith(".bundle"))
		{
			bundle = new TextDecoder().unmarshal(new ByteArrayInputStream(xmlData));
		}
		else
		{
			ConvertXml.unmarshal(new ByteArrayInputStream(xmlData), bundle);
		}

//		Log.out.println(new TextEncoder().marshal(bundle));

		BinaryEncoder binaryEncoder = new BinaryEncoder();
		byte[] binData = binaryEncoder.marshal(bundle);
		String txtData = new TextEncoder().marshal(bundle, true);

//		if (aFilename.equals("tiny.xml"))
//		{
//			Log.hexDump(binData);
//		}

		byte[] zipBin = zip(binData);
		byte[] zipTxt = zip(txtData.getBytes("utf-8"));
		byte[] zipXml = zip(xmlData);

		Log.out.printf("%12s, source: %6d (%5d), txt: %6d (%5d), bin: %6d (%5d) / %6d %6d / %6d %s\n", aFilename, xmlData.length, zipXml.length, txtData.length(), zipTxt.length, binData.length, zipBin.length, aExpectedSize, binData.length-aExpectedSize, binData.length-zipTxt.length, binaryEncoder.getStatistics());

		return binData.length-aExpectedSize;
	}


	private static byte[] fetch(InputStream aInput) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		for (int len; (len = aInput.read(buffer)) > 0;)
		{
			output.write(buffer, 0, len);
		}
		return output.toByteArray();
	}


	private static byte[] zip(byte[] aData) throws IOException
	{
		ByteArrayOutputStream zip = new ByteArrayOutputStream();
		try (DeflaterOutputStream dos = new DeflaterOutputStream(zip, new Deflater(Deflater.BEST_COMPRESSION)))
		{
			dos.write(aData);
		}
		return zip.toByteArray();
	}
}
