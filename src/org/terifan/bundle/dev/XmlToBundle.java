package org.terifan.bundle.dev;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilderFactory;
import org.terifan.bundle.old.Bundle;
import org.terifan.bundle.old.BundleVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XmlToBundle
{
	public static void main(String ... args)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setCoalescing(true);
			Document doc = factory.newDocumentBuilder().parse(XmlToBundle.class.getResourceAsStream("test.xml"));

			Bundle bundle = new Bundle();

			visit(bundle, (Element)doc.getFirstChild());

			TreeMap<String,TreeSet<String>> types = new TreeMap<>();

			bundle.visit(100, true, null, new BundleVisitor()
			{
				ArrayDeque<String> path = new ArrayDeque<>();
				ArrayDeque<Bundle> struct = new ArrayDeque<>();

				@Override
				public void entering(Bundle aParentBundle, String aKey, Bundle aChildBundle, int aIndex)
				{
					path.addLast(aKey);
					struct.addLast(aChildBundle);
				}
				@Override
				public void leaving(Bundle aParentBundle, String aKey, Bundle aChildBundle, int aIndex)
				{
					path.removeLast();
					struct.removeLast();
				}
				@Override
				public Object process(Bundle aBundle, String aKey, Object aValue)
				{
					TreeSet<String> set = types.computeIfAbsent(path.toString()+" "+aKey, e->new TreeSet<>());
					if (aValue instanceof String)
					{
						set.add(classify((String)aValue));
					}
					else
					{
						set.add("mix");
					}

					return aValue;
				}
			});

			for (Entry<String,TreeSet<String>> entry : types.entrySet())
			{
				TreeSet<String> set = entry.getValue();

				if (!set.contains("mix"))
				{
					if (set.contains("int") && set.contains("float"))
					{
						set.remove("int");
					}
					if (set.contains("int") && set.contains("long"))
					{
						set.remove("int");
					}
					if (set.contains("int") && set.contains("float"))
					{
						set.remove("int");
					}
//					if (set.contains("date") && set.contains("string"))
//					{
//						set.remove("date");
//					}
//					if (set.contains("time") && set.contains("string"))
//					{
//						set.remove("time");
//					}
//					if (set.contains("datetime") && set.contains("string"))
//					{
//						set.remove("datetime");
//					}
				}

				if (set.size() > 1)
				{
					set.clear();
					set.add("mix");
				}
			}

			bundle.visit(100, true, null, new BundleVisitor()
			{
				ArrayDeque<String> path = new ArrayDeque<>();
				ArrayDeque<Bundle> struct = new ArrayDeque<>();

				@Override
				public void entering(Bundle aParentBundle, String aKey, Bundle aChildBundle, int aIndex)
				{
					path.addLast(aKey);
					struct.addLast(aChildBundle);
				}
				@Override
				public void leaving(Bundle aParentBundle, String aKey, Bundle aChildBundle, int aIndex)
				{
					path.removeLast();
					struct.removeLast();
				}
				@Override
				public Object process(Bundle aBundle, String aKey, Object aValue)
				{
					TreeSet<String> type = types.get(path.toString()+" "+aKey);

					if (type.contains("int"))
					{
						return Integer.parseInt(aValue.toString());
					}
					if (type.contains("long"))
					{
						return Long.parseLong(aValue.toString());
					}
					if (type.contains("float"))
					{
						return Double.parseDouble(aValue.toString());
					}

					return aValue;
				}
			});

//			for (Entry<String,TreeSet<String>> entry : types.entrySet())
//			{
//				System.out.println(entry);
//			}

			bundle.marshalPSON(System.out, false);
			System.out.println();
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static void visit(Bundle aParentBundle, Element aElement)
	{
		Node child = aElement.getFirstChild();
		boolean hasChildNodes = false;

		while (child != null)
		{
			if (child instanceof Element)
			{
				hasChildNodes = true;
				break;
			}

			child = child.getNextSibling();
		}

		if (hasChildNodes)
		{
			Bundle newBundle = new Bundle();
			Object value = aParentBundle.get(aElement.getNodeName());

			if (value instanceof ArrayList)
			{
				ArrayList<Bundle> list = (ArrayList<Bundle>)value;
				list.add(newBundle);
			}
			else if (value instanceof Bundle)
			{
				ArrayList<Bundle> list = new ArrayList<>();
				list.add((Bundle)value);
				list.add(newBundle);
				aParentBundle.putBundleArrayList(aElement.getNodeName(), list);
			}
			else
			{
				aParentBundle.putBundle(aElement.getNodeName(), newBundle);
			}

			child = aElement.getFirstChild();

			while (child != null)
			{
				if (child instanceof Element)
				{
					visit(newBundle, (Element)child);
				}

				child = child.getNextSibling();
			}
		}
		else
		{
			child = aElement.getFirstChild();

			while (child != null)
			{
				Object value = aParentBundle.get(aElement.getNodeName());

				if (value instanceof ArrayList)
				{
					ArrayList<String> list = (ArrayList<String>)value;
					list.add(child.getTextContent().trim());
				}
				else if (value instanceof String)
				{
					ArrayList<String> list = new ArrayList<>();
					list.add((String)value);
					list.add(child.getTextContent().trim());
					aParentBundle.putStringArrayList(aElement.getNodeName(), list);
				}
				else
				{
					aParentBundle.putString(aElement.getNodeName(), child.getTextContent().trim());
				}

				child = child.getNextSibling();
			}
		}
	}


	private static String classify(String aValue)
	{
		if ("true".equalsIgnoreCase(aValue) || "false".equalsIgnoreCase(aValue))
		{
			return "boolean";
		}
		if (aValue.matches("[0-9]{1,15}"))
		{
			try
			{
				Integer.parseInt(aValue);
				return "int";
			}
			catch (Exception e)
			{
			}
			try
			{
				Long.parseLong(aValue);
				return "long";
			}
			catch (Exception e)
			{
			}
			return "string";
		}
		if (aValue.length() > 1 && aValue.matches("[0-9]{1,15}\\.") && aValue.indexOf(".") == aValue.lastIndexOf("."))
		{
			return "float";
		}
		if (aValue.matches("[0-9]{4}-[0-1]{1}[0-9]{1}-[0-3]{1}[0-9]{1} [0-2]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}"))
		{
			return "datetime";
		}
		if (aValue.matches("[0-9]{4}-[0-1]{1}[0-9]{1}-[0-3]{1}[0-9]{1}"))
		{
			return "date";
		}
		if (aValue.matches("[0-2]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}\\:[0-5]{1}[0-9]{1}"))
		{
			return "time";
		}
		return "string";
	}
}
