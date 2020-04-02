package org.terifan.bundle;

import java.io.InputStream;
import java.util.LinkedHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class XMLDecoder
{
	/**
	 * Import XML to a Bundle
	 *
	 * @param aInputStream
	 *   an XML string
	 * @param aCreateOptionalArrays
	 *   if true then each element will contain an array otherwise arrays will only be created when an element is repeated
	 */
	public void importXML(InputStream aInputStream, Container aContainer, boolean aAllowAttributesOnLeafs)
	{
		try (InputStream in = aInputStream)
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			Document doc = documentBuilder.parse(in);

			importBundle(doc, (Bundle)aContainer, aAllowAttributesOnLeafs);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}


	private void importBundle(Node aNode, Bundle aBundle, boolean aAllowAttributesOnLeafs)
	{
		if (aNode instanceof Element)
		{
			NamedNodeMap attributes = ((Element)aNode).getAttributes();
			for (int j = 0; j < attributes.getLength(); j++)
			{
				aBundle.set("@" + attributes.item(j).getNodeName(), attributes.item(j).getTextContent());
			}
		}

		NodeList nodeList = aNode.getChildNodes();

		LinkedHashMap<String,Integer> counts = new LinkedHashMap<>();

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			String name = node.getNodeName();
			if ((node instanceof Text || node instanceof Element) && !name.equals("#text"))
			{
				counts.put(name, counts.getOrDefault(name, 0) + 1);
			}
		}

		for (String key : counts.keySet().toArray(new String[0]))
		{
			Node[] nodes = new Node[counts.get(key)];

			for (int j = 0, k = 0; j < nodeList.getLength(); j++)
			{
				Node node = nodeList.item(j);
				if (key.equals(node.getNodeName()))
				{
					nodes[k++] = node;
				}
			}

			if (nodes.length > 1)
			{
				Array array = new Array();
				aBundle.put(key, array);
				for (Node node : nodes)
				{
					if (node instanceof Element && node.getChildNodes().getLength() > 1)
					{
						Bundle bundle = new Bundle();
						importBundle(node, bundle, aAllowAttributesOnLeafs);
						array.add(bundle);
					}
					else if (aAllowAttributesOnLeafs && hasAttributes(node))
					{
						Bundle bundle = new Bundle();
						importBundle(node, bundle, aAllowAttributesOnLeafs);
						bundle.put("#text", convertValue(node.getTextContent()));
						array.add(bundle);
					}
					else
					{
						array.add(convertValue(node.getTextContent()));
					}
				}
			}
			else
			{
				Node node = nodes[0];
				if (node instanceof Element && node.getChildNodes().getLength() > 1)
				{
					Bundle bundle = new Bundle();
					importBundle(node, bundle, aAllowAttributesOnLeafs);
					aBundle.put(key, bundle);
				}
				else if (aAllowAttributesOnLeafs && hasAttributes(node))
				{
					Bundle bundle = new Bundle();
					importBundle(node, bundle, aAllowAttributesOnLeafs);
					bundle.put("#text", convertValue(node.getTextContent()));
					aBundle.put(key, bundle);
				}
				else
				{
					aBundle.put(key, convertValue(node.getTextContent()));
				}
			}
		}
	}


	private boolean hasAttributes(Node aNode)
	{
		if (aNode instanceof Element)
		{
			NamedNodeMap attributes = ((Element)aNode).getAttributes();
			if (attributes.getLength() > 0)
			{
				return true;
			}
		}

		return false;
	}


	private Object convertValue(String aValue)
	{
		try
		{
			if (aValue == null) return null;
			if (aValue.equalsIgnoreCase("true") || aValue.equalsIgnoreCase("false"))
			{
				return Boolean.parseBoolean(aValue);
			}
			else if (aValue.matches("[0-9]*\\.[0-9]+"))
			{
				return Double.parseDouble(aValue);
			}
			else if (aValue.matches("[0-9]+"))
			{
				return Long.parseLong(aValue);
			}
		}
		catch (Exception e)
		{
//			e.printStackTrace(System.out);
		}

		return aValue;
	}
}
