package org.terifan.bundle;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;


class JSONDecoder
{
	private PushbackReader mReader;


	public JSONDecoder(Reader aReader)
	{
		mReader = new PushbackReader(aReader, 1);
	}


	public Container unmarshal(Container aContainer) throws IOException
	{
		switch (mReader.read())
		{
			case '{':
				return readBundle((Bundle)aContainer);
			case '[':
				return readArray((Array)aContainer);
			default:
				throw new IllegalArgumentException("First character must be either \"[\" or \"{\".");
		}
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

			if (c != '\"' && c != '\'')
			{
				throw new IOException("Expected starting quote character of key.");
			}

			int terminator = c;

			String key = readString(terminator);

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
					value = readArray(new Array());
					break;
				case '{':
					value = readBundle(new Bundle());
					break;
				case '\"':
					value = readString('\"');
					break;
				case '\'':
					value = readString('\'');
					break;
				default:
					mReader.unread(c);
					value = readValue();
					break;
			}

			aBundle.set(key, value);
		}

		return aBundle;
	}


	private Container readArray(Array aArray) throws IOException
	{
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

			if (aArray.size() > 0)
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
					value = readArray(new Array());
					break;
				case '{':
					value = readBundle(new Bundle());
					break;
				case '\"':
					value = readString('\"');
					break;
				case '\'':
					value = readString('\'');
					break;
				default:
					mReader.unread(c);
					value = readValue();
					break;
			}

			aArray.add(value);
		}

		return aArray;
	}


	private String readString(int aTerminator) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		for (;;)
		{
			int c = readByte();

			if (c == aTerminator)
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
}
