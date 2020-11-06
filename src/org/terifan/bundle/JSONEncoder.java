package org.terifan.bundle;

import java.io.IOException;
import java.util.Base64;
import java.util.Map.Entry;


class JSONEncoder
{
	private JSONTextWriter mWriter;


	public void marshal(JSONTextWriter aPrinter, Container aContainer) throws IOException
	{
		mWriter = aPrinter;

		if (aContainer instanceof Bundle)
		{
			marshalBundle((Bundle)aContainer, true);
		}
		else
		{
			marshalArray((Array)aContainer);
		}
	}


	private void marshalBundle(Bundle aBundle) throws IOException
	{
		marshalBundle(aBundle, true);
	}


	private void marshalBundle(Bundle aBundle, boolean aNewLineOnClose) throws IOException
	{
		int size = aBundle.size();

		boolean hasBundle = aBundle.size() > 5;

		for (Object entry : aBundle.values())
		{
			if (entry instanceof Bundle)
			{
				hasBundle = true;
				break;
			}
		}

		if (!hasBundle && !mWriter.isFirst())
		{
			mWriter.println();
		}

		mWriter.println("{").indent(1);

		for (Entry<String, Object> entry : aBundle.entrySet())
		{
			mWriter.print("\"" + escapeString(entry.getKey()) + "\": ");

			marshal(entry.getValue());

			if (hasBundle && --size > 0)
			{
				mWriter.println(aNewLineOnClose ? "," : ", ", false);
			}
			else if (!hasBundle && --size > 0)
			{
				mWriter.print(", ", false);
			}
		}

		if (aNewLineOnClose)
		{
			mWriter.println().indent(-1).println("}");
		}
		else
		{
			mWriter.println().indent(-1).print("}");
		}
	}


	void marshalArray(Array aArray) throws IOException
	{
		int size = aArray.size();

		if (size == 0)
		{
			mWriter.println("[]");
			return;
		}

		boolean special = aArray.get(0) instanceof Bundle;
		boolean first = special;
		boolean shortArray = !special && aArray.size() < 10;

		for (int i = 0; shortArray && i < aArray.size(); i++)
		{
			shortArray = !(aArray.get(i) instanceof Array) && !(aArray.get(i) instanceof Bundle) && !(aArray.get(i) instanceof String);
		}

		if (special)
		{
			mWriter.print("[").indent(aArray.size() > 1 ? 1 : 0);
		}
		else if (shortArray)
		{
			mWriter.print("[");
		}
		else
		{
			mWriter.println("[").indent(1);
		}

		for (Object value : aArray)
		{
			if (first)
			{
				marshalBundle((Bundle)value, false);

				if (--size > 0)
				{
					mWriter.println(", ");
				}
			}
			else
			{
				marshal(value);

				if (--size > 0)
				{
					mWriter.print(", ", false);
				}
			}

			first = false;
		}

		if (special)
		{
			mWriter.indent(aArray.size() > 1 ? -1 : 0).println("]");
		}
		else if (shortArray)
		{
			mWriter.println("]");
		}
		else
		{
			mWriter.println().indent(-1).println("]");
		}
	}


	private void marshal(Object aValue) throws IOException
	{
		if (aValue instanceof Bundle)
		{
			marshalBundle((Bundle)aValue);
		}
		else if (aValue instanceof Array)
		{
			marshalArray((Array)aValue);
		}
		else
		{
			marshalValue(aValue);
		}
	}


	void marshalValue(Object aValue) throws IOException
	{
		if (aValue instanceof byte[])
		{
			aValue = Base64.getEncoder().encodeToString((byte[])aValue);
		}

		if (aValue instanceof String)
		{
			mWriter.print("\"" + escapeString(aValue.toString()) + "\"");
		}
		else
		{
			mWriter.print(aValue);
		}
	}


	private String escapeString(String aString)
	{
		return aString.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}
}
