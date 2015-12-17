package org.terifan.bundle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.terifan.bundle.bundle_test.Log;


public class TextDecoder
{
	private static SimpleDateFormat mDateFormatter;


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aURL
	 *   the path of a serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(URL aURL) throws IOException
	{
		return unmarshal(aURL, new Bundle());
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aURL
	 *   the path of a serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(URL aURL, Bundle aBundle) throws IOException
	{
		return unmarshal(aURL.openConnection().getInputStream(), aBundle);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aString
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(String aString) throws IOException
	{
		return readBundle(new PushbackReader(new StringReader(aString)), new Bundle());
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aString
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(String aString, Bundle aBundle) throws IOException
	{
		return readBundle(new PushbackReader(new StringReader(aString)), aBundle);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aReader
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(Reader aReader) throws IOException
	{
		return readBundle(new PushbackReader(aReader), new Bundle());
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aReader
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(Reader aReader, Bundle aBundle) throws IOException
	{
		return readBundle(new PushbackReader(aReader), aBundle);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aFile
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(File aFile) throws IOException
	{
		return readBundle(new PushbackReader(new FileReader(aFile)), new Bundle());
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aFile
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(File aFile, Bundle aBundle) throws IOException
	{
		return readBundle(new PushbackReader(new FileReader(aFile)), aBundle);
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aInputStream
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(InputStream aInputStream) throws IOException
	{
		return readBundle(new PushbackReader(new InputStreamReader(aInputStream)), new Bundle());
	}


	/**
	 * Reads the provided string and returns a Bundle.
	 *
	 * @param aInputStream
	 *   the serialized Bundle
	 * @return
	 *   the read Bundle
	 */
	public Bundle unmarshal(InputStream aInputStream, Bundle aBundle) throws IOException
	{
		return readBundle(new PushbackReader(new InputStreamReader(aInputStream)), aBundle);
	}


	private Bundle readBundle(PushbackReader aReader, Bundle aBundle) throws IOException
	{
		if (aReader.read() != '{')
		{
			throw new IOException("Expected a start curly bracket in bundle start.");
		}

		return readBundleImpl(aReader, aBundle);
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

			int fieldType = Integer.parseInt(key.substring(0, key.indexOf("!")));
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
			readChar(aReader);
			readChar(aReader);
			readChar(aReader);
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
			int c = aReader.read();

			if (c == 'n')
			{
				aReader.read();
				aReader.read();
				aReader.read();
				return null;
			}
			if (c != '{')
			{
				throw new IllegalStateException();
			}

			return readBundleImpl(aReader, new Bundle());
		}

		StringBuilder sb = new StringBuilder();

		for (;;)
		{
			int c = aReader.read();

			if (c == '}' || c == ']' || c == ',' || c == '=' || c == ':')
			{
				aReader.unread(c);
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
				return value.substring(1, value.length() - 1);
		}

		throw new IllegalStateException();
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


	public static void main(String ... args)
	{
		try
		{
			Log.out.println(new TextEncoder().marshal(new TextDecoder().unmarshal("{'25!a':'x', '37!b':[1,2,3], '26!c':{'21!d':1}, '71!d':[[1,2],[3,4]], '74!e':[[{'21!f':1}]]}")));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}