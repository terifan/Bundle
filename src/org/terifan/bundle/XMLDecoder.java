package org.terifan.bundle;

import java.io.InputStream;
import java.util.HashMap;
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
	public void importXML(InputStream aInputStream, Container aContainer, boolean aCreateOptionalArrays)
	{
		try (InputStream in = aInputStream)
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			Document doc = documentBuilder.parse(in);

			importXML(doc, (Bundle)aContainer, aCreateOptionalArrays);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
	}


	private void importXML(Node aNode, Bundle aBundle, boolean aCreateOptionalArrays)
	{
		NodeList nodeList = aNode.getChildNodes();

		HashMap<String,Array> elements = new HashMap<>();
		HashMap<String,Integer> counts = null;

		if (!aCreateOptionalArrays)
		{
			counts = new HashMap<>();

			for (int i = 0; i < nodeList.getLength(); i++)
			{
				String name = nodeList.item(i).getNodeName();
				counts.put(name, counts.getOrDefault(name, 0) + 1);
			}
		}

		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);

			if (node instanceof Element)
			{
				Bundle bundle = new Bundle();

				if (aCreateOptionalArrays || counts.get(node.getNodeName()) > 1)
				{
					Array array = elements.computeIfAbsent(node.getNodeName(), e->{Array arr = new Array(); aBundle.putArray(e, arr); return arr;});
					array.add(bundle);
				}
				else
				{
					aBundle.set(node.getNodeName(), bundle);
				}

				NamedNodeMap attributes = ((Element)node).getAttributes();
				for (int j = 0; j < attributes.getLength(); j++)
				{
					bundle.set("@" + attributes.item(j).getNodeName(), attributes.item(j).getTextContent());
				}

				importXML(node, bundle, aCreateOptionalArrays);
			}
			else if (node instanceof Text)
			{
				if (!node.getNodeValue().trim().isEmpty())
				{
					aBundle.set("#content", node.getNodeValue().trim());
				}
			}
		}
	}
}
