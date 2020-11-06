package org.terifan.bundle;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


class JSONTextWriter
{
	private Appendable mAppendable;
	private boolean mNewLine;
	private boolean mCompact;
	private boolean mFirst;
	private int mIndent;


	public JSONTextWriter(Appendable aAppendable, boolean aCompact)
	{
		mAppendable = aAppendable;
		mNewLine = false;
		mCompact = aCompact;
		mFirst = true;
	}


	public JSONTextWriter indent(int aDelta)
	{
		mIndent += aDelta;
		return this;
	}


	public JSONTextWriter print(Object aText) throws IOException
	{
		return print(aText, true);
	}


	public JSONTextWriter print(Object aText, boolean aIndent) throws IOException
	{
		String text = formatString(aText);

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


	public JSONTextWriter println(Object aText)
	{
		return println(aText, true);
	}


	public JSONTextWriter println(Object aText, boolean aIndent)
	{
		String text = formatString(aText);

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


	public JSONTextWriter println()
	{
		mNewLine = true;
		return this;
	}


	public boolean isFirst()
	{
		return mFirst;
	}


	private String formatString(Object aText)
	{
		if (aText == null)
		{
			return "null";
		}

		if (aText instanceof Double || aText instanceof Float)
		{
			String text = aText.toString().replace(" ", "");

			int i0 = text.indexOf(',');
			if (i0 != -1)
			{
				int i1 = text.indexOf('.');
				if (i1 != -1)
				{
					if (i0 < i1)
					{
						text = text.replace(",", ""); // handles: 10,000.7
					}
					else
					{
						text = text.replace(".", "").replace(',', '.'); // handles: 10.000,7
					}
				}
				else
				{
					text = text.replace(',', '.'); // handles: 10000.7
				}
			}

			if (text.endsWith(".0"))
			{
				text = text.substring(0, text.length() - 2);
			}

			return text;
		}

		return aText.toString();
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


	private String stripTrailing(String aText)
	{
		while (Character.isWhitespace(aText.charAt(aText.length() - 1)))
		{
			aText = aText.substring(0, aText.length() - 1);
		}
		return aText;
	}
}
