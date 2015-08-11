package org.terifan.bundle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;


public class JSONDecoder
{
	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aString
	 *   the JSON string
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(String aString) throws IOException
	{
		return readBundle(new PushbackReader(new StringReader(aString)), false);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aString
	 *   the JSON string
	 * @param aAllStrings
	 *   instead of decoding values as either boolean, long or double all values will be Strings.
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(String aString, boolean aAllStrings) throws IOException
	{
		return readBundle(new PushbackReader(new StringReader(aString)), aAllStrings);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aReader
	 *   the JSON string
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(Reader aReader) throws IOException
	{
		return readBundle(new PushbackReader(aReader), false);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aFile
	 *   the JSON string
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(File aFile) throws IOException
	{
		return readBundle(new PushbackReader(new FileReader(aFile)), false);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aInputStream
	 *   the JSON string
	 * @param aAllStrings
	 *   instead of decoding values as either boolean, long or double all values will be Strings.
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(InputStream aInputStream, boolean aAllStrings) throws IOException
	{
		return readBundle(new PushbackReader(new InputStreamReader(aInputStream)), aAllStrings);
	}


	private Bundle readBundle(PushbackReader aReader, boolean aAllStrings) throws IOException
	{
		Bundle bundle = new Bundle();

		if (aReader.read() != '{')
		{
			throw new IOException("Expected a start curly bracket in bundle start.");
		}

		for (int c; (c = readChar(aReader)) != '}';)
		{
			if (bundle.isEmpty())
			{
				aReader.unread(c);
			}
			else if (c != ',')
			{
				throw new IOException("Expected comma sign between elements in bundle: found ascii " + c);
			}

			Object key = readValue(aReader, true, aAllStrings);

			if (!(key instanceof String))
			{
				throw new IOException("Key must be string.");
			}

			char d = readChar(aReader);
			if (d != ':' && d != '=')
			{
				throw new IOException("Exptected colon sign after key: key=" + key);
			}

			Object o = readValue(aReader, false, aAllStrings);

			setValue(bundle, key.toString(), o);
		}

		return bundle;
	}


	private Object readValue(PushbackReader aReader, boolean aKeyField, boolean aAllStrings) throws IOException
	{
		char c = readChar(aReader);

		if (c == '{')
		{
			aReader.unread(c);
			return readBundle(aReader, aAllStrings);
		}
		if (c == '[')
		{
			return readArray(aReader, aAllStrings);
		}
		if (c == '\"' || c == '\'')
		{
			StringBuilder sb = new StringBuilder();
			for (int d; (d = aReader.read()) != c && d != -1;)
			{
				if (d == '\\')
				{
					d = aReader.read();
				}
				sb.append((char)d);
			}
			return sb.toString();
		}

		aReader.unread(c);
		StringBuilder sb = new StringBuilder();
		for (;;)
		{
			c = (char)aReader.read();
			if (c == '\\')
			{
				c = (char)aReader.read();
			}
			if (c == '}' || c == ']' || c == ',' || c == ':' || c == '=')
			{
				aReader.unread(c);
				break;
			}
			sb.append(c);
		}

		if (aKeyField)
		{
			return sb.toString().trim();
		}

		return processValue(sb.toString().trim(), aAllStrings);
	}


	private Object readArray(PushbackReader aReader, boolean aAllStrings) throws IOException
	{
		ArrayList list = new ArrayList();
		boolean mixed = false;
		Class type = null;

		for (int c; (c = readChar(aReader)) != ']';)
		{
			if (list.isEmpty())
			{
				aReader.unread(c);
			}
			else if (c != ',')
			{
				throw new IOException("Expected comma between elements in array.");
			}

			Object value = readValue(aReader, false, aAllStrings);

			if (value != null)
			{
				Class t = value.getClass();
				if (type == null)
				{
					type = t;
				}
				else if (t != type)
				{
					mixed = true;
					if (t == String.class || ((t == Double.class || t == Long.class) && type == Boolean.class) || ((type == Double.class || type == Long.class) && t == Boolean.class))
					{
						type = String.class;
					}
					else if ((t == Long.class && type == Double.class) || (type == Long.class && t == Double.class))
					{
						type = Double.class;
					}
				}
			}

			list.add(value);
		}

		if (mixed)
		{
			for (int i = 0; i < list.size(); i++)
			{
				Object v = list.get(i);

				if (v != null)
				{
					if (type == String.class)
					{
						list.set(i, v.toString());
					}
					else if (type == Double.class && v instanceof Long)
					{
						list.set(i, (double)(Long)v);
					}
				}
			}
		}

		return list;
	}


	private char readChar(PushbackReader aReader) throws IOException
	{
		for (;;)
		{
			int c = aReader.read();
			if (c == -1)
			{
				throw new IOException();
			}
			if (!Character.isWhitespace((char)c))
			{
				return (char)c;
			}
		}
	}


	private Object processValue(String aString, boolean aAllStrings)
	{
		if (aString.equals("null"))
		{
			return null;
		}

		if (aAllStrings)
		{
			return aString;
		}

		if (aString.equals("true"))
		{
			return Boolean.TRUE;
		}
		if (aString.equals("false"))
		{
			return Boolean.FALSE;
		}
		if (aString.contains("."))
		{
			return new Double(aString);
		}
		if (aString.matches("^[0-9]+$"))
		{
			return new Long(aString);
		}

		return aString;
	}


	private void setValue(Bundle bundle, String key, Object o) throws IOException
	{
		if (o instanceof String || o == null)
		{
			bundle.put(key, o);
		}
		else if (o instanceof Bundle)
		{
			bundle.put(key, o);
		}
		else if (o instanceof ArrayList)
		{
			ArrayList list = (ArrayList)o;
			Class componentType = getComponentType(list);
			if (list.isEmpty() || componentType == null)
			{
				bundle.put(key, list);
			}
			else if (componentType == String.class)
			{
				bundle.put(key, list);
			}
			else if (componentType == Bundle.class)
			{
				bundle.put(key, list);
			}
			else if (componentType == Boolean.class)
			{
				bundle.put(key, list);
			}
			else if (componentType == Double.class)
			{
				bundle.put(key, list);
			}
			else if (componentType == Long.class)
			{
				bundle.put(key, list);
			}
			else
			{
				throw new IOException();
			}
		}
		else if (o instanceof Boolean)
		{
			bundle.put(key, o);
		}
		else if (o instanceof Double)
		{
			bundle.put(key, o);
		}
		else if (o instanceof Long)
		{
			bundle.put(key, o);
		}
		else
		{
			throw new IOException(o.toString());
		}
	}


	private Class getComponentType(ArrayList list) throws IOException
	{
		Class type = null;
		boolean toDouble = false;
		for (int i = 0; i < list.size(); i++)
		{
			Object value = list.get(i);
			if (value != null)
			{
				Class other = value.getClass();
				if (type == null)
				{
					type = other;
				}
				if (other != type)
				{
					if (type == Long.class && other == Double.class || other == Long.class && type == Double.class)
					{
						toDouble = true;
					}
					else
					{
						throw new IOException("Array contains mix types: expected=" + type + ", found=" + other);
					}
				}
			}
		}

		if (toDouble)
		{
			for (int i = 0; i < list.size(); i++)
			{
				Object value = list.get(i);
				if (value instanceof Long)
				{
					list.set(i, (double)(Long)value);
				}
			}
		}

		return type;
	}
}
