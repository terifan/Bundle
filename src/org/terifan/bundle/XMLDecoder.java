package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XMLDecoder
{
	private final static String NS = "http://www.w3.org/2001/XMLSchema-instance";


	public Bundle unmarshal(InputStream aInputStream, String aBundleNodePath) throws IOException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document doc = factory.newDocumentBuilder().parse((InputStream)aInputStream);
			Node node = (Node)XPathFactory.newInstance().newXPath().compile(aBundleNodePath).evaluate(doc, XPathConstants.NODE);

			return unmarshal(node);
		}
		catch (ParserConfigurationException | XPathExpressionException | SAXException e)
		{
			throw new IOException(e);
		}
	}


	public Bundle unmarshal(String aXml, String aBundleNodePath) throws IOException
	{
		return unmarshal(new StringReader(aXml), aBundleNodePath);
	}


	public Bundle unmarshal(Reader aReader, String aBundleNodePath) throws IOException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document doc = factory.newDocumentBuilder().parse(new InputSource(aReader));
			Node node = (Node)XPathFactory.newInstance().newXPath().compile(aBundleNodePath).evaluate(doc, XPathConstants.NODE);

			return unmarshal(node);
		}
		catch (ParserConfigurationException | XPathExpressionException | SAXException e)
		{
			throw new IOException(e);
		}
	}


	public Bundle unmarshal(Node aBundleNode) throws IOException
	{
//		Element el = (Element)aBundleNode;
//
//		if (!el.getAttribute("type").equals("bundle"))
//		{
//			throw new IOException("Bundle node must have type 'bundle'.");
//		}

		return readBundle(aBundleNode);
	}


	private Bundle readBundle(Node aBundleNode) throws IOException
	{
		Bundle bundle = new Bundle();

		Element el = (Element)aBundleNode;

		NodeList list = el.getChildNodes();

		for (int i = 0; i < list.getLength(); i++)
		{
			Node o = list.item(i);

			if (!(o instanceof Element))
			{
				continue;
			}

			Element el2 = (Element)o;

			String type = el2.getAttribute("type");

			try
			{
				switch (type)
				{
					case "array":
					{
						String compType = el2.getAttribute("component-type");
						FieldType fieldType = FieldType.valueOf(compType.toUpperCase());
						if ("true".equals(el2.getAttributeNS(NS, "nil")))
						{
							bundle.put(el2.getNodeName(), null);
						}
						else
						{
							NodeList list2 = el2.getChildNodes();

							int itemCount = list2.getLength();
//							int itemCount = 0;
//							for (int j = 0; j < list2.getLength(); j++)
//							{
//								if ((list2.item(j) instanceof Element) && list2.item(j).getNodeName().equals("item"))
//								{
//									itemCount++;
//								}
//							}

							Object array = Array.newInstance(fieldType.getPrimitiveType(), itemCount);

							for (int j = 0; j < list2.getLength(); j++)
							{
								Element el3 = (Element)list2.item(j);
								if (el3 != null && el3.getNodeName().equals("item"))
								{
									if ("true".equals(el3.getAttributeNS(NS, "nil")))
									{
										Array.set(array, j, null);
									}
									else if (compType.equals("bundle"))
									{
										Array.set(array, j, readBundle(el3));
									}
									else
									{
										Array.set(array, j, readValue(el3, compType));
									}
								}
							}

							bundle.put(el2.getNodeName(), array);
						}
						break;
					}
					case "arraylist":
					{
						String compType = el2.getAttribute("component-type");
						NodeList list2 = el2.getChildNodes();
						ArrayList array = new ArrayList();

						for (int j = 0; j < list2.getLength(); j++)
						{
							Element el3 = (Element)list2.item(j);
							if (el3 != null && el3.getNodeName().equals("item"))
							{
								if ("true".equals(el3.getAttributeNS(NS, "nil")))
								{
									array.add(null);
								}
								else if (compType.equals("bundle"))
								{
									array.add(readBundle(el3));
								}
								else
								{
									array.add(readValue(el3, compType));
								}
							}
						}

						bundle.put(el2.getNodeName(), array);
						break;
					}
					case "bundle":
						bundle.putBundle(el2.getNodeName(), readBundle(el2));
						break;
					default:
						bundle.put(el2.getNodeName(), readValue(el2, type));
						break;
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException("Unsupported field type: '" + type + "' in element '" + el2.getNodeName() + "'", e);
			}
		}

		return bundle;
	}


	private Object readValue(Element aElement, String type) throws IOException
	{
		String value = aElement.getFirstChild().getTextContent();

		switch (type)
		{
			case "boolean":
				return Boolean.parseBoolean(value);
			case "byte":
				return Byte.parseByte(value);
			case "short":
				return Short.parseShort(value);
			case "char":
				return (char)Integer.parseInt(value);
			case "int":
				return Integer.parseInt(value);
			case "long":
				return Long.parseLong(value);
			case "float":
				return Float.parseFloat(value);
			case "double":
				return Double.parseDouble(value);
			case "date":
				try
				{
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z (Z)").parse(value);
				}
				catch (ParseException e)
				{
					throw new IOException("Failed to decode date: " + value, e);
				}
			case "string":
				return value;
		}

		throw new IOException(type);
	}
}
