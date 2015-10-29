package org.terifan.bundle.bundle_test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.terifan.bundle.BinaryEncoder;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.TextEncoder;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class TestXml
{
	public static void main(String ... args)
	{
		try
		{
			int delta = 0;

			delta += test("tiny.xml", 133);
			delta += test("params.xml", 725);
			delta += test("fo.xml", 2238);
			delta += test("ctts.xml", 1143);
			delta += test("lynx.xml", 1250);
			delta += test("oms1.xml", 24798);
			delta += test("oms2.xml", 19302);
			delta += test("edoc.xml", 119825);

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

		list(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlData)), bundle);

//		Log.out.println(new TextEncoder().marshal(bundle));

		byte[] binData = new BinaryEncoder().marshal(bundle);
		String txtData = new TextEncoder().marshal(bundle, true);

		if (aFilename.equals("tiny.xml"))
		{
			Log.hexDump(binData);
		}

		byte[] zipBin = zip(binData);
		byte[] zipTxt = zip(txtData.getBytes("utf-8"));
		byte[] zipXml = zip(xmlData);

		Log.out.printf("%10s, source: %6d (%5d), txt: %6d (%5d), bin: %6d (%5d) / %6d %5d\n", aFilename, xmlData.length, zipXml.length, txtData.length(), zipTxt.length, binData.length, zipBin.length, aExpectedSize, binData.length-aExpectedSize);

		return binData.length-aExpectedSize;
	}


	private static void list(Node aNode, Bundle aBundle)
	{
		NodeList list = aNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				NamedNodeMap attributes = ((Element)node).getAttributes();
				if (attributes.getLength() > 0)
				{
					for (int j = 0; j < attributes.getLength(); j++)
					{
						aBundle.put("@" + attributes.item(j).getNodeName(), parseValue(attributes.item(j).getNodeValue()));
					}
				}

				NodeList children = node.getChildNodes();

				if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE)
				{
					String name = node.getNodeName();
					Object value = parseValue(children.item(0).getNodeValue());

					if (aBundle.containsKey(name))
					{
						Object old = aBundle.get(name);
						if (old instanceof ArrayList)
						{
							ArrayList tmp = (ArrayList)old;
							if (!tmp.get(0).getClass().equals(value.getClass()))
							{
								throw new IllegalArgumentException("New element type conflicts with type of list");
							}
							tmp.add(value);
						}
						else if (!old.getClass().equals(value.getClass()))
						{
							throw new IllegalArgumentException("New element type conflicts with type of list");
						}
						else
						{
							ArrayList tmp = new ArrayList();
							tmp.add(old);
							tmp.add(value);
							aBundle.put(name, tmp);
						}
					}
					else
					{
						aBundle.put(name, value);
					}
				}
				else if (children.getLength() > 0)
				{
					Bundle bundle = new Bundle();
					list(node, bundle);

					String name = node.getNodeName();

					if (aBundle.containsKey(name))
					{
						Object a = aBundle.get(name);
						if (a instanceof ArrayList)
						{
							((ArrayList)a).add(bundle);
						}
						else
						{
							ArrayList tmp = new ArrayList();
							tmp.add(a);
							tmp.add(bundle);
							aBundle.put(name, tmp);
						}
					}
					else
					{
						aBundle.put(name, bundle);
					}
				}
			}
		}
	}


	private static Object parseValue(String aInput)
	{
		Object output = aInput;

		output = parseDate(output, "yyyy-MM-dd'T'HH:mm:ss.SSS");
		output = parseDate(output, "yyyy-MM-dd'T'HH:mm:ss");
		output = parseDate(output, "yyyy-MM-dd HH:mm:ss.SSS");
		output = parseDate(output, "yyyy-MM-dd HH:mm:ss");
		output = parseDate(output, "yyyy-MM-dd");

		if (output == aInput)
		{
			try
			{
				output = Double.parseDouble(aInput);
				output = Long.parseLong(aInput);
				output = Integer.parseInt(aInput);
			}
			catch (Exception e)
			{
			}
		}

		if ((output instanceof String) && output.toString().length() > 20 && output.toString().matches("[A-Za-z0-9+/]*"))
		{
			try
			{
				output = Base64.getDecoder().decode(output.toString());
			}
			catch (Exception e)
			{
			}
		}

		return output;
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
		try (DeflaterOutputStream dos = new DeflaterOutputStream(zip))
		{
			dos.write(aData);
		}
		return zip.toByteArray();
	}


	private static Object parseDate(Object aInput, String aFormat)
	{
		if (aInput instanceof String)
		{
			try
			{
				return new SimpleDateFormat(aFormat).parse((String)aInput);
			}
			catch (Exception e)
			{
			}
		}
		return aInput;
	}
}
