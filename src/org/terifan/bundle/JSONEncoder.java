package org.terifan.bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.List;


public class JSONEncoder
{
	private SimpleDateFormat mDateFormatter;
	private int mIndent;
	private Appendable mAppendable;


	public void marshal(Bundle aBundle, File aOutput) throws IOException
	{
		try (FileWriter fw = new FileWriter(aOutput))
		{
			marshal(aBundle, fw);
		}
	}


	/**
	 * Returns the provided Bundle as a JSON string.
	 *
	 * @return
	 *   the Bundle as a JSON string
	 */
	public String marshal(Bundle aBundle) throws IOException
	{
		return marshal(aBundle, new StringBuilder(1<<17)).toString();
	}


	/**
	 * Returns the provided Bundle as a JSON string.
	 *
	 * @return
	 *   the Bundle as a JSON string
	 */
	public Appendable marshal(Bundle aBundle, Appendable aAppendable) throws IOException
	{
		mAppendable = aAppendable;
		mIndent = 0;

		writeBundle(aBundle);

		return aAppendable;
	}


	private void writeBundle(Bundle aBundle) throws IOException
	{
		mAppendable.append("{");

		if (!aBundle.isEmpty())
		{
			boolean first = true;
			boolean simple = true;

			for (String key : aBundle.keySet())
			{
				Object value = aBundle.get(key);
				FieldType fieldType = FieldType.valueOf(value);
				if (value != null && (fieldType == FieldType.BUNDLE || value.getClass().isArray() || List.class.isAssignableFrom(value.getClass())))
				{
					simple = false;
					break;
				}
			}

			if (!simple)
			{
				mAppendable.append("\n");
				mIndent++;
			}

			for (String key : aBundle.keySet())
			{
				if (key.contains("\""))
				{
					throw new IOException("Name contains illegal character: " + key);
				}

				Object value = aBundle.get(key);
				FieldType fieldType = FieldType.valueOf(value);

				if (!first)
				{
					if (simple)
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

				mAppendable.append("\""+key+"\": ");

				if (value == null)
				{
					mAppendable.append("null");
				}
				else
				{
					Class<? extends Object> cls = value.getClass();

					if (cls.isArray() || List.class.isAssignableFrom(cls))
					{
						if (List.class.isAssignableFrom(cls))
						{
							value = ((List)value).toArray();
						}
						mAppendable.append("[");
						int len = Array.getLength(value);
						if (len > 0)
						{
							boolean simpleArray = true;
							for (int i = 0; i < len; i++)
							{
								Object v = Array.get(value, i);
								fieldType = FieldType.valueOf(v);
								if (v != null && (fieldType == FieldType.BUNDLE || v.getClass().isArray() || List.class.isAssignableFrom(v.getClass())))
								{
									simpleArray = false;
									break;
								}
							}

							if (!simpleArray)
							{
								mAppendable.append("\n");
								mIndent++;
							}
							for (int i = 0; i < len; i++)
							{
								if (i > 0)
								{
									if (simpleArray)
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
								writeValue(Array.get(value, i), fieldType);
							}
							if (!simpleArray)
							{
								mIndent--;
								mAppendable.append("\n");
								indent();
							}
						}
						mAppendable.append("]");
					}
					else
					{
						writeValue(value, fieldType);
					}
				}
			}

			if (!simple)
			{
				mIndent--;
				mAppendable.append("\n");
				indent();
			}
		}
		mAppendable.append("}");
	}


	private void writeValue(Object aValue, FieldType aFieldType) throws IOException
	{
		if (aValue == null)
		{
			mAppendable.append("null");
			return;
		}

		switch (aFieldType)
		{
			case BUNDLE:
				writeBundle((Bundle)aValue);
				break;
			case STRING:
				mAppendable.append("\"" + escapeString(aValue.toString()) + "\"");
				break;
			case DATE:
				if (mDateFormatter == null)
				{
					mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				}
				mAppendable.append("\"" + mDateFormatter.format(aValue) + "\"");
				break;
			case CHAR:
				mAppendable.append("\"" + (int)(Character)aValue + "\"");
				break;
			default:
				mAppendable.append(aValue.toString());
				break;
		}
	}


	private String escapeString(String s)
	{
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("/", "\\/").replace("\b", "\\\b").replace("\f", "\\\f").replace("\n", "\\\n").replace("\r", "\\\r").replace("\t", "\\\t");
	}


	private void indent() throws IOException
	{
		for (int i = 0; i < mIndent; i++)
		{
			mAppendable.append("\t");
		}
	}
}
