package org.terifan.bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class JSONEncoder
{
	private final static int SIMPLE_OBJECT_MAX_ELEMENTS = 5;

	private SimpleDateFormat mDateFormatter;
	private Appendable mAppendable;
	private boolean mFlat;
	private int mIndent;


	public void marshal(Bundle aBundle, File aOutput) throws IOException
	{
		marshal(aBundle, aOutput, false);
	}


	public void marshal(Bundle aBundle, File aOutput, boolean aFlat) throws IOException
	{
		try (FileWriter fw = new FileWriter(aOutput))
		{
			marshal(aBundle, fw, aFlat);
		}
	}


	public String marshal(Bundle aBundle) throws IOException
	{
		return marshal(aBundle, false);
	}


	/**
	 * Returns the provided Bundle as a JSON string.
	 *
	 * @return
	 *   the Bundle as a JSON string
	 */
	public String marshal(Bundle aBundle, boolean aFlat) throws IOException
	{
		return marshal(aBundle, new StringBuilder(1<<17), aFlat).toString();
	}


	/**
	 * Returns the provided Bundle as a JSON string.
	 *
	 * @return
	 *   the Bundle as a JSON string
	 */
	public Appendable marshal(Bundle aBundle, Appendable aAppendable) throws IOException
	{
		return marshal(aBundle, aAppendable, false);
	}


	public Appendable marshal(Bundle aBundle, Appendable aAppendable, boolean aFlat) throws IOException
	{
		mAppendable = aAppendable;
		mFlat = aFlat;
		mIndent = 0;

		writeBundle(aBundle);

		return aAppendable;
	}


	private void writeBundle(Bundle aBundle) throws IOException
	{
		mAppendable.append("{");

		if (!aBundle.isEmpty())
		{
			boolean simple = isSimple(aBundle);
			boolean first = true;

			if (!mFlat && !simple)
			{
				mAppendable.append("\n");
				mIndent++;
			}

			for (String key : aBundle.keySet())
			{
				if (key.contains("\"") || key.contains("'") || key.contains("\n") || key.contains("\r") || key.contains("\t"))
				{
					throw new IOException("Name contains illegal character: " + key);
				}

				Object value = aBundle.get(key);

				if (!first)
				{
					if (mFlat || simple)
					{
						mAppendable.append(", ");
					}
					else
					{
						mAppendable.append(",\n");
						indent();
					}
				}
				else if (!simple)
				{
					indent();
				}
				first = false;

				String skey = aBundle.getType(key) + "!" + key;
				mAppendable.append("\"").append(skey).append("\": ");

				if (value == null)
				{
					mAppendable.append("null");
				}
				else
				{
					if (List.class.isAssignableFrom(value.getClass()))
					{
						value = ((List)value).toArray();
					}

					if (value.getClass().isArray())
					{
						writeArray(value);
					}
					else
					{
						writeValue(value);
					}
				}
			}

			if (!mFlat && !simple)
			{
				mIndent--;
				mAppendable.append("\n");
				indent();
			}
		}
		mAppendable.append("}");
	}


	private void writeArray(Object aValue) throws IOException
	{
		mAppendable.append("[");

		int len = Array.getLength(aValue);

		if (len > 0)
		{
			boolean simpleArray = isSimple(len, aValue);
			if (!mFlat && !simpleArray)
			{
				mAppendable.append("\n");
				mIndent++;
			}
			for (int i = 0; i < len; i++)
			{
				if (i > 0)
				{
					if (mFlat || simpleArray)
					{
						mAppendable.append(", ");
					}
					else
					{
						mAppendable.append(",\n");
					}
				}
				if (!simpleArray)
				{
					indent();
				}

				Object v = Array.get(aValue, i);
				if (v != null && v.getClass().isArray())
				{
					writeArray(v);
				}
				else
				{
					writeValue(v);
				}
			}

			if (!mFlat && !simpleArray)
			{
				mIndent--;
				mAppendable.append("\n");
				indent();
			}
		}

		mAppendable.append("]");
	}


	private boolean isSimple(int aLen, Object aValue) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
	{
		boolean simpleArray = aLen < SIMPLE_OBJECT_MAX_ELEMENTS;
		if (simpleArray)
		{
			for (int i = 0; i < aLen; i++)
			{
				Object v = Array.get(aValue, i);
				if (v != null && (v instanceof Bundle || v.getClass().isArray() || List.class.isAssignableFrom(v.getClass())))
				{
					simpleArray = false;
					break;
				}
			}
		}
		return simpleArray;
	}


	private boolean isSimple(Bundle aBundle)
	{
		boolean simple = aBundle.size() < SIMPLE_OBJECT_MAX_ELEMENTS;
		if (simple)
		{
			for (String key : aBundle.keySet())
			{
				Object value = aBundle.get(key);
				int fieldType = aBundle.getType(key);

				int collectionType = FieldType.collectionType(fieldType);
				int valueType = FieldType.valueType(fieldType);

				if (value != null && (valueType == FieldType.BUNDLE || collectionType != FieldType.VALUE))
				{
					simple = false;
					break;
				}
			}
		}
		return simple;
	}


	private void writeValue(Object aValue) throws IOException
	{
		if (aValue == null)
		{
			mAppendable.append("null");
			return;
		}

		if (aValue instanceof String)
		{
			mAppendable.append("\"").append(escapeString(aValue.toString())).append("\"");
		}
		else if (aValue instanceof Bundle)
		{
			writeBundle((Bundle)aValue);
		}
		else if (aValue instanceof Character)
		{
			mAppendable.append("" + (int)(Character)aValue);
		}
		else if (aValue instanceof Date)
		{
			if (mDateFormatter == null)
			{
				mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			}
			mAppendable.append("\"").append(mDateFormatter.format(aValue)).append("\"");
		}
		else
		{
			mAppendable.append(aValue.toString());
		}
	}


	private String escapeString(String s)
	{
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\b", "\\\b").replace("\f", "\\\f").replace("\n", "\\\n").replace("\r", "\\\r").replace("\t", "\\\t");
	}


	private void indent() throws IOException
	{
		for (int i = 0; i < mIndent; i++)
		{
			mAppendable.append("\t");
		}
	}
}