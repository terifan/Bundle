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
import java.util.Date;


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

			Object key = readValue(aReader, true);

			if (!(key instanceof String))
			{
				throw new IOException("Key must be string.");
			}

			char d = readChar(aReader);
			if (d != ':' && d != '=')
			{
				throw new IOException("Exptected colon sign after key: key=" + key);
			}

			aBundle.put(key.toString(), readValue(aReader, false));
		}

		return aBundle;
	}


	private Object readValue(PushbackReader aReader, boolean aKeyField) throws IOException
	{
		char c = readChar(aReader);

		if (c == '{')
		{
			return readBundleImpl(aReader, new Bundle());
		}
		if (c == '[' || c == '<')
		{
			Object arr = readArray(aReader, c == '[');
			replaceNullMatrix(arr);
			return arr;
		}
		if (c == '\"' || c == '\'')
		{
			return readString(aReader, c);
		}
		if (c == '#')
		{
			return readDate(aReader);
		}

		aReader.unread(c);

		String sb = readValue(aReader);

		if (aKeyField)
		{
			return sb;
		}

		return processValue(sb);
	}


	private void replaceNullMatrix(Object aArr)
	{
		if (aArr != null && aArr.getClass().isArray() && aArr.getClass().getComponentType().isArray())
		{
			Class type = null;
			boolean hasNull = false;
			for (int i = 0; i < Array.getLength(aArr); i++)
			{
				Object v = Array.get(aArr, i);
				if (v == null)
				{
					hasNull = true;
				}
				else
				{
					type = v.getClass().getComponentType();
				}
			}
			if (hasNull)
			{
				for (int i = 0; i < Array.getLength(aArr); i++)
				{
					Object v = Array.get(aArr, i);
					if (v == null)
					{
						Array.set(aArr, i, Array.newInstance(type, 0));
					}
				}
			}
		}
	}


	private String readValue(PushbackReader aReader) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		for (;;)
		{
			int c = aReader.read();

			if (c == '}' || c == ']' || c == '>' || c == ',' || c == ':' || c == '=')
			{
				aReader.unread(c);
				return sb.toString().trim();
			}
			if (c == '\\')
			{
				c = aReader.read();
			}

			sb.append((char)c);
		}
	}


	private Object readString(PushbackReader aReader, char aTerminator) throws IOException
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


	private Date readDate(PushbackReader aReader) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		for (;;)
		{
			int c = aReader.read();

			if (c == '#')
			{
				break;
			}
			if (c == '\\')
			{
				c = aReader.read();
			}

			sb.append((char)c);
		}

		if (mDateFormatter == null)
		{
			mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		}
		try
		{
			return mDateFormatter.parse(sb.toString());
		}
		catch (ParseException e)
		{
			throw new IOException(e);
		}
	}


	private Object readArray(PushbackReader aReader, boolean aArray) throws IOException
	{
		char terminator = aArray ? ']' : '>';
		ArrayList list = new ArrayList();
		Class type = null;

		for (;;)
		{
			int c = readChar(aReader);

			if (c == terminator)
			{
				break;
			}

			if (list.isEmpty())
			{
				aReader.unread(c);
			}
			else if (c != ',')
			{
				throw new IOException("Expected comma between elements in array.");
			}

			Object value = readValue(aReader, false);

			if (value != null)
			{
				Class other = value.getClass();
				if (type == null)
				{
					type = other;
				}
				else if (other != type)
				{
					throw new IOException("Array contains mixed types: expected=" + type + ", found=" + other);
				}
			}

			list.add(value);
		}

		if (!aArray)
		{
			return list;
		}
		if (list.isEmpty())
		{
			return null;
		}
		
		if (type == null)
		{
			String s = "";
			for (int i = 0; i < 20; i++)
			{
				s += (char)aReader.read();
			}
			throw new IllegalArgumentException("Error near: " + s.replace("\n", " ").replace("\r", " ").replace("\t", " "));
		}
		
		Object array = Array.newInstance(getPrimitiveType(type), list.size());

		for (int i = 0; i < list.size(); i++)
		{
			Array.set(array, i, list.get(i));
		}

		return array;
	}


	private Class getPrimitiveType(Class aType)
	{
		if (aType == Boolean.class)
		{
			return Boolean.TYPE;
		}
		if (aType == Byte.class)
		{
			return Byte.TYPE;
		}
		if (aType == Short.class)
		{
			return Short.TYPE;
		}
		if (aType == Character.class)
		{
			return Character.TYPE;
		}
		if (aType == Integer.class)
		{
			return Integer.TYPE;
		}
		if (aType == Long.class)
		{
			return Long.TYPE;
		}
		if (aType == Float.class)
		{
			return Float.TYPE;
		}
		if (aType == Double.class)
		{
			return Double.TYPE;
		}
		return aType;
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


	private Object processValue(String aValue) throws IOException
	{
		if (aValue.equals("null"))
		{
			return null;
		}
		if (aValue.equals("true"))
		{
			return Boolean.TRUE;
		}
		if (aValue.equals("false"))
		{
			return Boolean.FALSE;
		}

		char suffix = Character.toLowerCase(aValue.charAt(aValue.length() - 1));

		if (suffix == 'b')
		{
			return Byte.decode(trim(aValue));
		}
		if (suffix == 'c')
		{
			return (char)Integer.parseInt(trim(aValue));
		}
		if (suffix == 's')
		{
			return Short.decode(trim(aValue));
		}
		if (suffix == 'l')
		{
			return Long.decode(trim(aValue));
		}
		if (suffix == 'f')
		{
			return Float.parseFloat(trim(aValue));
		}
		if (suffix == 'd')
		{
			return Double.parseDouble(trim(aValue));
		}
		if (aValue.contains("."))
		{
			return Double.parseDouble(aValue);
		}

		return Integer.decode(aValue);
	}


	private String trim(String aString)
	{
		return aString.substring(0, aString.length() - 1);
	}


//	public static void main(String ... args)
//	{
//		try
//		{
//			Bundle bundle = new Bundle()
//				.putBoolean("boolean", true)
//				.putByte("byte", 97)
//				.putShort("short", 1000)
//				.putChar("char", (char)97)
//				.putInt("int", 64646464)
//				.putLong("long", 6464646464646464646L)
//				.putFloat("float", 3.14f)
//				.putDouble("double", 7)
//				.putDate("date", new Date())
//				.putString("string", "string")
//				.putString("null", null)
//				.putBundle("bundle", new Bundle()
//					.putByte("a", 1)
//					.putByte("b", 2)
//				)
//				.putIntArray("ints", 1,2,3)
//				.putByteArray("bytes", (byte)1,(byte)2,(byte)3)
//				.putStringArray("strings", "a", "b", "c")
//				.putIntArrayList("intList", new ArrayList<>(Arrays.asList(1,2,3)));
//
//			String s = new TextEncoder().marshal(bundle);
//
//			Log.out.println(s);
//
//			Bundle unmarshaled = new TextDecoder().unmarshal(s);
//
//			Log.out.println(unmarshaled);
//
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			bundle.writeExternal(oos);
//			oos.close();
//
//			bundle = new Bundle();
//			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
//			ObjectInputStream ois = new ObjectInputStream(bais);
//			bundle.readExternal(ois);
//			ois.close();
//
//
//
////			baos = new ByteArrayOutputStream();
////			new BinaryEncoder().marshal(bundle, baos);
////
////			Debug.hexDump(baos.toByteArray());
////
////			bais = new ByteArrayInputStream(baos.toByteArray());
////			Bundle unbundled = new BinaryDecoder().unmarshal(bais);
////
////			Log.out.println(unbundled);
//		}
//		catch (Throwable e)
//		{
//			e.printStackTrace(System.out);
//		}
//	}
}