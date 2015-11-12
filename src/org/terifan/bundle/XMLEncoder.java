package org.terifan.bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XMLEncoder<T extends Bundle>
{
	private final static String NS = "http://www.w3.org/2001/XMLSchema-instance";


	public void marshal(Bundle aBundle, String aRootElement, File aOutput) throws IOException
	{
		try (FileWriter fw = new FileWriter(aOutput))
		{
			marshal(aBundle, aRootElement, fw);
		}
	}


	public void marshal(Bundle aBundle, String aRootElement, Writer aOutput) throws IOException
	{
		try
		{
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(marshal(aBundle, aRootElement)), new StreamResult(aOutput));
		}
		catch (TransformerException | TransformerFactoryConfigurationError e)
		{
			throw new IOException(e);
		}
	}


	/**
	 * Returns this Bundle as an XML.
	 *
	 * @return the XML root Element
	 */
	public Document marshal(Bundle aBundle, String aRootElement) throws IOException
	{
		try
		{
			if (aRootElement == null)
			{
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//				Element el = document.createElement(aBundle.keySet().iterator().next());
//				document.appendChild(el);
//				el.setAttribute("xmlns:xsi", NS);
				writeBundle(aBundle, document);
				return document;
			}
			else
			{
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element el = document.createElement(aRootElement);
				document.appendChild(el);
				el.setAttribute("xmlns:xsi", NS);
				writeBundle(aBundle, el);
				return document;
			}
		}
		catch (ParserConfigurationException e)
		{
			throw new IOException(e);
		}
	}


	public String marshalToString(Bundle aBundle, String aRootElement) throws IOException
	{
		try
		{
			StringWriter sw = new StringWriter();
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(marshal(aBundle, aRootElement)), new StreamResult(sw));
			return sw.toString();
		}
		catch (TransformerFactoryConfigurationError | TransformerException e)
		{
			throw new IOException(e);
		}
	}


	/**
	 * Returns this Bundle as an XML.
	 *
	 * @return the provided Element
	 */
	public Element marshal(Bundle aBundle, Element aParentNode) throws IOException
	{
		writeBundle(aBundle, aParentNode);
		return aParentNode;
	}


	private void writeBundle(Bundle<T> aBundle, Node aElement) throws IOException
	{
		Document ownerDocument = aElement.getOwnerDocument() == null ? (Document)aElement : aElement.getOwnerDocument();

		for (String key : aBundle.keySet())
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.classify(value);

			if (fieldType == null)
			{
				continue;
			}

			Class<? extends Object> cls = value.getClass();

			if (cls.isArray() || List.class.isAssignableFrom(cls))
			{
				if (List.class.isAssignableFrom(cls))
				{
					value = ((List)value).toArray();
				}
//				String typeName = fieldType.getJavaName().toLowerCase();
				String typeName = fieldType.name().toLowerCase();
				Element el = ownerDocument.createElement(key);
				el.setAttribute("type", cls.isArray() ? "array" : "arraylist");
				el.setAttribute("component-type", typeName);
				aElement.appendChild(el);
				for (int i = 0, len = Array.getLength(value); i < len; i++)
				{
					Object o = Array.get(value, i);
					Element el2 = (Element)el.appendChild(ownerDocument.createElement("item"));
					if (o == null)
					{
						el2.setAttributeNS(NS, "xsi:nil", "true");
					}
					else if (fieldType == FieldType.BUNDLE)
					{
						writeBundle((Bundle)o, el2);
					}
					else if (fieldType == FieldType.STRING)
					{
						el2.appendChild(ownerDocument.createCDATASection(o.toString()));
					}
					else if (fieldType == FieldType.CHAR)
					{
						el2.appendChild(ownerDocument.createTextNode("" + (int)(Character)o));
					}
					else if (fieldType == FieldType.DATE)
					{
						el2.appendChild(ownerDocument.createTextNode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z (Z)").format(o)));
					}
					else
					{
						el2.appendChild(ownerDocument.createTextNode(o.toString()));
					}
				}
			}
			else
			{
				Element el = (Element)aElement.appendChild(ownerDocument.createElement(key));
				el.setAttribute("type", fieldType.name().toLowerCase());
				if (fieldType == FieldType.BUNDLE)
				{
					writeBundle((Bundle)value, el);
				}
				else if (fieldType == FieldType.STRING)
				{
					el.appendChild(ownerDocument.createCDATASection(value.toString()));
				}
				else if (fieldType == FieldType.CHAR)
				{
					el.appendChild(ownerDocument.createTextNode("" + (int)(Character)value));
				}
				else if (fieldType == FieldType.DATE)
				{
					el.appendChild(ownerDocument.createTextNode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z (Z)").format(value)));
				}
				else
				{
					el.appendChild(ownerDocument.createTextNode(value.toString()));
				}
			}
		}
	}
}
