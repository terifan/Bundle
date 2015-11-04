package org.terifan.bundle.bundle_test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.terifan.bundle.BinaryEncoder;
import org.terifan.bundle.BinaryEncoderRef;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.ConvertXml;
import org.terifan.bundle.TextDecoder;
import org.terifan.bundle.TextEncoder;
import org.xml.sax.SAXException;


public class TestXml
{
	private static int[] sums = new int[12];

	private static String FORMAT = "%12s  %6d (%5d), %6d (%5d), %6d (%5d), %6d (%5d), %9d %7d %8d %9d\n";

	public static void main(String ... args)
	{
		try
		{
			Log.out.printf("%12s  %14s  %14s  %14s  %14s   %7s %8s %8s %9s\n", "", "xml", "txt", "bin", "ref", "bin-ref", "bin-zRef", "bin-zTxt", "zRef-zTxt");
			Log.out.printf("%12s  %s  %s  %s  %s   %s %s %s %s\n", "", "--------------", "--------------", "--------------", "--------------", "-------", "--------", "--------", "---------");

			test("tiny.xml");
			test("lxir.xml");
			test("params.xml");
			test("fo.xml");
			test("ctts.xml");
			test("lynx.xml");
			test("oms1.xml");
			test("oms2.xml");
			test("edoc.xml");
			test("capimil1.xml");
			test("capimil2.xml");

			Log.out.println("------------------------------------------------------------------------------------------------------------------");
			Log.out.printf(FORMAT, "", sums[0], sums[1], sums[2], sums[3], sums[4], sums[5], sums[6], sums[7], sums[8], sums[9], sums[10], sums[11]);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static void test(String aFilename) throws SAXException, IOException, ParserConfigurationException
	{
		byte[] xml;
		try (InputStream in = TestXml.class.getResourceAsStream(aFilename))
		{
			xml = fetch(in);
		}

		Bundle bundle = new Bundle();

		if (aFilename.endsWith(".bundle"))
		{
			bundle = new TextDecoder().unmarshal(new ByteArrayInputStream(xml));
		}
		else
		{
			ConvertXml.unmarshal(new ByteArrayInputStream(xml), bundle);
		}

//		Log.out.println(new TextEncoder().marshal(bundle));

		BinaryEncoder binaryEncoder = new BinaryEncoder();
		BinaryEncoderRef binaryEncoderRef = new BinaryEncoderRef();
		byte[] bin = binaryEncoder.marshal(bundle);
		byte[] ref = binaryEncoderRef.marshal(bundle);
		String txt = new TextEncoder().marshal(bundle, true);

//		if (aFilename.equals("tiny.xml"))
//		{
//			Log.hexDump(ref);
//		}

		byte[] zipBin = zip(bin);
		byte[] zipRef = zip(ref);
		byte[] zipTxt = zip(txt.getBytes("utf-8"));
		byte[] zipXml = zip(xml);

		int d1 = bin.length - ref.length;
		int d2 = bin.length - zipRef.length;
		int d3 = bin.length - zipTxt.length;
		int d4 = zipRef.length - zipTxt.length;

		Log.out.printf("%12s, %6d (%5d), %6d (%5d), %6d (%5d), %6d (%5d) / %8d %7d %8d %9d  %s\n", aFilename, xml.length, zipXml.length, txt.length(), zipTxt.length, bin.length, zipBin.length, ref.length, zipRef.length, d1, d2, d3, d4, binaryEncoder.getStatistics());

		sums[0] += xml.length;
		sums[1] += zipXml.length;
		sums[2] += txt.length();
		sums[3] += zipTxt.length;
		sums[4] += bin.length;
		sums[5] += zipBin.length;
		sums[6] += ref.length;
		sums[7] += zipRef.length;
		sums[8] += d1;
		sums[9] += d2;
		sums[10] += d3;
		sums[11] += d4;
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
