package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import static org.terifan.bundle.FieldType.COLLECTION_TYPES;
import static org.terifan.bundle.FieldType.VALUE_TYPES;


class JSONDecoder
{
	private static SimpleDateFormat mDateFormatter;

	private final static HashMap<Character,Integer> VALUE_TYPE_MAP = new HashMap<>();
	private final static HashMap<Character,Integer> COLLECTION_TYPE_MAP = new HashMap<>();

	static
	{
		for (String s : VALUE_TYPES)
		{
			VALUE_TYPE_MAP.put(s.charAt(0), VALUE_TYPE_MAP.size());
		}
		for (String s : COLLECTION_TYPES)
		{
			COLLECTION_TYPE_MAP.put(s.isEmpty() ? '!' : s.charAt(0), COLLECTION_TYPE_MAP.size());
		}
	}


	public Bundle unmarshal(Reader aReader, Bundle aBundle) throws IOException
	{
		PushbackReader reader = new PushbackReader(aReader);

		if (reader.read() != '{')
		{
			throw new IOException("Expected a start curly bracket in bundle start.");
		}

		return readBundleImpl(reader, aBundle);
	}


	private Bundle readBundleImpl(PushbackReader aReader, Bundle aBundle) throws IOException
	{
		for (;;)
		{
			int c = readChar(aReader);

			if (c == '}')
			{
				break;
			}

			if (aBundle.isEmpty())
			{
				aReader.unread(c);
			}
			else if (c != ',')
			{
				throw new IOException("Expected comma sign between elements in bundle: found ascii " + c);
			}

			String key = readString(aReader, readChar(aReader));

			if (!key.contains("!"))
			{
				throw new IllegalStateException();
			}

			int fieldType = decodeKey(key);

			key = key.substring(key.indexOf("!") + 1);

			char d = readChar(aReader);

			if (d != ':' && d != '=')
			{
				throw new IOException("Expected colon sign after key: key=" + key);
			}

			Object value;

			switch (FieldType.collectionType(fieldType))
			{
				case FieldType.ARRAY:
				case FieldType.ARRAYLIST:
					value = readArray(aReader, fieldType);
					break;
				case FieldType.MATRIX:
					value = readMatrix(aReader, fieldType);
					break;
				default:
					value = readValue(aReader, fieldType);
					break;
			}

			aBundle.put(key, value, fieldType);
		}

		return aBundle;
	}


	private String readString(PushbackReader aReader, char aTerminator) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		for (;;)
		{
			int c = aReader.read();

			if (c == aTerminator)
			{
				return sb.toString();
			}
			if (c == '\\')
			{
				c = aReader.read();
			}

			sb.append((char)c);
		}
	}


	private Object readMatrix(PushbackReader aReader, int aFieldType) throws IOException
	{
		ArrayList list = new ArrayList();

		for (;;)
		{
			int c = readChar(aReader);

			if (c == ']')
			{
				break;
			}

			list.add(readArray(aReader, aFieldType));
		}

		Object array = Array.newInstance(FieldType.getPrimitiveType(aFieldType), list.size(), 0);

		for (int i = 0; i < list.size(); i++)
		{
			Array.set(array, i, list.get(i));
		}

		return array;
	}


	private Object readArray(PushbackReader aReader, int aFieldType) throws IOException
	{
		int c = readChar(aReader);

		if (c == 'n')
		{
			readNull(aReader);
			return null;
		}
		else if (c != '[')
		{
			throw new IllegalStateException();
		}

		ArrayList list = new ArrayList();

		for (;;)
		{
			c = readChar(aReader);

			if (c == ']')
			{
				break;
			}
			if (list.isEmpty())
			{
				aReader.unread(c);
			}
			else if (c != ',')
			{
				throw new IOException("Expected comma sign between elements in array: found ascii " + c);
			}

			list.add(readValue(aReader, aFieldType));
		}

		if (FieldType.collectionType(aFieldType) == FieldType.ARRAYLIST)
		{
			return list;
		}

		Object array = Array.newInstance(FieldType.getPrimitiveType(aFieldType), list.size());

		for (int i = 0; i < list.size(); i++)
		{
			Array.set(array, i, list.get(i));
		}

		return array;
	}


	private Object readValue(PushbackReader aReader, int aFieldType) throws IOException
	{
		if (FieldType.valueType(aFieldType) == FieldType.BUNDLE)
		{
			int c = readChar(aReader);

			if (c == 'n')
			{
				readNull(aReader);
				return null;
			}
			if (c != '{')
			{
				throw new IllegalStateException(""+c);
			}

			return readBundleImpl(aReader, new Bundle());
		}

		StringBuilder sb = new StringBuilder();

		char t = '\0';

		switch (FieldType.valueType(aFieldType))
		{
			case FieldType.STRING:
			case FieldType.DATE:
			case FieldType.OBJECT:
				t = readChar(aReader);
				if (t == 'n')
				{
					readNull(aReader);
					return null;
				}
				break;
		}

		for (;;)
		{
			int c = aReader.read();

			if (c == t || t == '\0' && (c == '}' || c == ']' || c == ',' || c == '=' || c == ':'))
			{
				if (c != t)
				{
					aReader.unread(c);
				}
				break;
			}
			if (c == '\\')
			{
				c = aReader.read();
			}

			sb.append((char)c);
		}

		String value = sb.toString().trim();

		if (value.equals("null"))
		{
			return null;
		}

		switch (FieldType.valueType(aFieldType))
		{
			case FieldType.BOOLEAN:
				return Boolean.parseBoolean(value);
			case FieldType.BYTE:
				return Byte.parseByte(value);
			case FieldType.SHORT:
				return Short.parseShort(value);
			case FieldType.CHAR:
				return (char)Integer.parseInt(value);
			case FieldType.INT:
				return Integer.parseInt(value);
			case FieldType.LONG:
				return Long.parseLong(value);
			case FieldType.FLOAT:
				return Float.parseFloat(value);
			case FieldType.DOUBLE:
				return Double.parseDouble(value);
			case FieldType.DATE:
				if (mDateFormatter == null)
				{
					mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				}
				try
				{
					return mDateFormatter.parse(value);
				}
				catch (ParseException e)
				{
					throw new IOException(e);
				}
			case FieldType.STRING:
				return value;
			case FieldType.OBJECT:
				byte[] buf = Base64.getDecoder().decode(value);
				try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf)))
				{
					return ois.readObject();
				}
				catch (ClassNotFoundException e)
				{
					throw new IOException(e);
				}
		}

		throw new IllegalStateException();
	}


	private void readNull(PushbackReader aReader) throws IOException
	{
		if (Character.toLowerCase(aReader.read()) != 'u' || Character.toLowerCase(aReader.read()) != 'l' || Character.toLowerCase(aReader.read()) != 'l')
		{
			throw new IllegalArgumentException();
		}
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


	private int decodeKey(String aKey)
	{
		return FieldType.encode(COLLECTION_TYPE_MAP.get(aKey.charAt(1)) << 4, VALUE_TYPE_MAP.get(aKey.charAt(0)) + 1);
	}
}