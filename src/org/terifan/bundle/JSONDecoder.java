package org.terifan.bundle;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;


class JSONDecoder
{
	private PushbackReader mReader;


	public JSONDecoder(Reader aReader)
	{
		mReader = new PushbackReader(aReader, 1);
	}


	public Object unmarshal() throws IOException
	{
		switch (mReader.read())
		{
			case '{':
				return readBundle();
			case '[':
				return readArray();
			default:
				throw new IllegalArgumentException("First character must be either \"[\" or \"{\".");
		}
	}


	public void unmarshal(Bundle aBundle) throws IOException
	{
		if (mReader.read() != '{')
		{
			throw new IllegalArgumentException("First character must be \"{\".");
		}

		readBundle(aBundle);
	}


	private Bundle readBundle() throws IOException
	{
		return readBundle(new Bundle());
	}


	private Bundle readBundle(Bundle aBundle) throws IOException
	{
		for (;;)
		{
			int c = readChar();

			if (c == '}')
			{
				break;
			}
			if (aBundle.size() > 0)
			{
				if (c != ',')
				{
					throw new IOException("Expected comma between elements");
				}

				c = readChar();
			}

			if (c != '\"')
			{
				throw new IOException("Expected starting quote character of key.");
			}

			String key = readString();

			c = readChar();

			if (c != ':')
			{
				throw new IOException("Expected colon sign after key: " + key);
			}

			c = readChar();

			Object value;
			switch (c)
			{
				case '[':
					value = readArray();
					break;
				case '{':
					value = readBundle();
					break;
				case '\"':
					value = readString();
					break;
				default:
					mReader.unread(c);
					value = readValue();
					break;
			}

			aBundle.put(key, value);
		}

		return aBundle;
	}


	private Object readArray() throws IOException
	{
		Array array = new Array();

		for (;;)
		{
			int c = readChar();

			if (c == ']')
			{
				break;
			}
			if (c == ':')
			{
				throw new IOException("Found colon after element in array");
			}

			if (array.size() > 0)
			{
				if (c != ',')
				{
					throw new IOException("Expected comma between elements: found: " + (char)c);
				}

				c = readChar();
			}

			Object value;
			switch (c)
			{
				case '[':
					value = readArray();
					break;
				case '{':
					value = readBundle();
					break;
				case '\"':
					value = readString();
					break;
				default:
					mReader.unread(c);
					value = readValue();
					break;
			}

			array.add(value);
		}

		return array;
	}


	private String readString() throws IOException
	{
		StringBuilder sb = new StringBuilder();

		for (;;)
		{
			int c = readByte();

			if (c == '\"')
			{
				return sb.toString();
			}
			if (c == '\\')
			{
				c = readByte();
			}

			sb.append((char)c);
		}
	}


	private Object readValue() throws IOException
	{
		StringBuilder sb = new StringBuilder();

		for (;;)
		{
			int c = readByte();

			if (c == '}' || c == ']' || c == ',' || Character.isWhitespace(c))
			{
				mReader.unread(c);
				break;
			}
			if (c == '\\')
			{
				c = readByte();
			}

			sb.append((char)c);
		}

		String in = sb.toString().trim();
		Object out;

		if ("null".equalsIgnoreCase(in))
		{
			out = null;
		}
		else if ("true".equalsIgnoreCase(in))
		{
			out = true;
		}
		else if ("false".equalsIgnoreCase(in))
		{
			out = false;
		}
		else if (in.contains("."))
		{
			out = Double.parseDouble(in);
		}
		else
		{
			out = Long.parseLong(in);
		}

		return out;
	}


	private char readChar() throws IOException
	{
		for (;;)
		{
			int c = readByte();
			if (!Character.isWhitespace((char)c))
			{
				return (char)c;
			}
		}
	}


	private int readByte() throws IOException
	{
		int c = mReader.read();
		if (c == -1)
		{
			throw new IOException("Unexpected end of stream.");
		}
		return c;
	}


	public static void main(String... args)
	{
		try
		{
			System.out.println(new JSONDecoder(new StringReader("[]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("{}")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[1,2,3]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[1,\"a\",true,null,1.3,{},[]]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[\"1\",\"2\",\"3\"]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("{\"a\":1,\"b\":2}")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[{\"a\":1,\"b\":2}]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[1,2,[3]]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[[1,2,[3]]]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[[1,2,[{\"a\":\"b\"}]]]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[[1,2,[{\"a\":true}]]]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[[1,2,[{\"a\":null}]]]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[[1,2,[{\"a\":1.1}]]]")).unmarshal());
			System.out.println(new JSONDecoder(new StringReader("[[1,2,[{\"a\":7}]]]")).unmarshal());
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
