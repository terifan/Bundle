package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ConvertXml 
{
	public static void unmarshal(InputStream aInputStream, Bundle aBundle) throws IOException
	{
		try
		{
			list(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(aInputStream), aBundle);
		}
		catch (ParserConfigurationException | SAXException e)
		{
			throw new IOException(e);
		}
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