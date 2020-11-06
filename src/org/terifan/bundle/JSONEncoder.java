package org.terifan.bundle;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;


class JSONEncoder
{
	public JSONEncoder()
	{
	}


	public void marshal(Printer aPrinter, Container aContainer) throws IOException
	{
		if (aContainer instanceof Bundle)
		{
			marshalBundle(aPrinter, (Bundle)aContainer, true);
		}
		else
		{
			marshalArray(aPrinter, (Array)aContainer);
		}
	}


	private void marshalBundle(Printer aPrinter, Bundle aBundle) throws IOException
	{
		marshalBundle(aPrinter, aBundle, true);
	}


	private void marshalBundle(Printer aPrinter, Bundle aBundle, boolean aNewLineOnClose) throws IOException
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

		if (!hasBundle && !aPrinter.isFirst())
		{
			aPrinter.println();
		}

		aPrinter.println("{").indent(1);

		for (Entry<String, Object> entry : aBundle.entrySet())
		{
			aPrinter.print("\"" + escapeString(entry.getKey()) + "\": ");

			marshal(aPrinter, entry.getValue());

			if (hasBundle && --size > 0)
			{
				aPrinter.println(aNewLineOnClose ? "," : ", ", false);
			}
			else if (!hasBundle && --size > 0)
			{
				aPrinter.print(", ", false);
			}
		}

		if (aNewLineOnClose)
		{
			aPrinter.println().indent(-1).println("}");
		}
		else
		{
			aPrinter.println().indent(-1).print("}");
		}
	}


	void marshalArray(Printer aPrinter, Array aArray) throws IOException
	{
		int size = aArray.size();

		if (size == 0)
		{
			aPrinter.println("[]");
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
			aPrinter.print("[").indent(aArray.size() > 1 ? 1 : 0);
		}
		else if (shortArray)
		{
			aPrinter.print("[");
		}
		else
		{
			aPrinter.println("[").indent(1);
		}

		for (Object value : aArray)
		{
			if (first)
			{
				marshalBundle(aPrinter, (Bundle)value, false);

				if (--size > 0)
				{
					aPrinter.println(", ");
				}
			}
			else
			{
				marshal(aPrinter, value);

				if (--size > 0)
				{
					aPrinter.print(", ", false);
				}
			}

			first = false;
		}

		if (special)
		{
			aPrinter.indent(aArray.size() > 1 ? -1 : 0).println("]");
		}
		else if (shortArray)
		{
			aPrinter.println("]");
		}
		else
		{
			aPrinter.println().indent(-1).println("]");
		}
	}


	private void marshal(Printer aPrinter, Object aValue) throws IOException
	{
		if (aValue instanceof Bundle)
		{
			marshalBundle(aPrinter, (Bundle)aValue);
		}
		else if (aValue instanceof Array)
		{
			marshalArray(aPrinter, (Array)aValue);
		}
		else
		{
			marshalValue(aPrinter, aValue);
		}
	}


	void marshalValue(Printer aPrinter, Object aValue) throws IOException
	{
		if (aValue instanceof Date)
		{
			aValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date)aValue);
		}
		else if (aValue instanceof byte[])
		{
			aValue = Base64.getEncoder().encodeToString((byte[])aValue);
		}
		else if (aValue instanceof UUID)
		{
			aValue = aValue.toString();
		}
		else if (aValue instanceof Calendar)
		{
			Calendar calendar = (Calendar)aValue;
			int offset = calendar.getTimeZone().getRawOffset() / 60_000;
			aValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(calendar.getTimeInMillis())) + (offset < 0 ? "-" : "+") + String.format("%02d:%02d", offset / 60, offset % 60);
		}

		if (aValue instanceof String)
		{
			aPrinter.print("\"" + escapeString(aValue.toString()) + "\"");
		}
		else
		{
			aPrinter.print(aValue);
		}
	}


	private String escapeString(String aString)
	{
		return aString.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
	}


	static class Printer
	{
		private Appendable mAppendable;
		private boolean mNewLine;
		private boolean mCompact;
		private boolean mFirst;
		private int mIndent;


		public Printer(Appendable aAppendable, boolean aCompact)
		{
			mAppendable = aAppendable;
			mNewLine = false;
			mCompact = aCompact;
			mFirst = true;
		}


		Printer indent(int aDelta)
		{
			mIndent += aDelta;
			return this;
		}


		Printer print(Object aText) throws IOException
		{
			return print(aText, true);
		}


		Printer print(Object aText, boolean aIndent) throws IOException
		{
			String text = aText == null ? "null" : aText.toString();

			if ((aText instanceof Double || aText instanceof Float) && text.endsWith(".0"))
			{
				text = text.substring(0, text.length() - 2);
			}

			if (mCompact && text.endsWith(" "))
			{
				text = stripTrailing(text);
				if (text.isEmpty())
				{
					return this;
				}
			}

			if (aIndent)
			{
				printIndent();
			}

			mAppendable.append(text);
			mFirst = false;
			return this;
		}


		Printer println(Object aText)
		{
			return println(aText, true);
		}


		Printer println(Object aText, boolean aIndent)
		{
			String text = aText == null ? "null" : aText.toString();
			if (mCompact && text.endsWith(" "))
			{
				text = stripTrailing(text);
				if (text.isEmpty())
				{
					return this;
				}
			}
			if (aIndent)
			{
				printIndent();
			}
			try
			{
				mAppendable.append(text);
			}
			catch (IOException e)
			{
				throw new IllegalStateException(e);
			}
			mNewLine = true;
			return this;
		}


		Printer println()
		{
			mNewLine = true;
			return this;
		}


		boolean isFirst()
		{
			return mFirst;
		}


		private void printIndent()
		{
			if (mNewLine && !mCompact)
			{
				try
				{
					mAppendable.append("\n");
					for (int i = 0; i < mIndent; i++)
					{
						mAppendable.append("\t");
					}
					mNewLine = false;
				}
				catch (IOException e)
				{
					throw new IllegalStateException(e);
				}
			}
		}
	}


	private static String stripTrailing(String aText)
	{
		while (Character.isWhitespace(aText.charAt(aText.length() - 1)))
		{
			aText = aText.substring(0, aText.length() - 1);
		}
		return aText;
	}
}
