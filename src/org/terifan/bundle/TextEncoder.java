package org.terifan.bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TextEncoder
{
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
				if (key.contains("\"") || key.contains("'"))
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

				if (value == null)
				{
					mAppendable.append("\"").append(key).append("\": ");
					mAppendable.append("null");
				}
				else
				{
					Class<? extends Object> cls = value.getClass();
					boolean isList = List.class.isAssignableFrom(cls);

					if (cls.isArray() || isList)
					{
						mAppendable.append("\"").append(key).append("\": ");
						if (isList)
						{
							value = ((List)value).toArray();
							mAppendable.append("<");
						}
						else
						{
							mAppendable.append("[");
						}
						int len = Array.getLength(value);
						if (len > 0)
						{
							boolean simpleArray = isSimple(len, value);

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
								writeValue(Array.get(value, i));
							}
							if (!mFlat && !simpleArray)
							{
								mIndent--;
								mAppendable.append("\n");
								indent();
							}
						}
						mAppendable.append(isList ? ">" : "]");
					}
					else
					{
						mAppendable.append("\"").append(key).append("\": ");
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


	private boolean isSimple(int aLen, Object aValue) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
	{
		boolean simpleArray = true;
		for (int i = 0; i < aLen; i++)
		{
			Object v = Array.get(aValue, i);
			if (v != null && (v instanceof Bundle || v.getClass().isArray() || List.class.isAssignableFrom(v.getClass())))
			{
				simpleArray = false;
				break;
			}
		}
		return simpleArray;
	}


	private boolean isSimple(Bundle aBundle)
	{
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
		else if (aValue instanceof Integer)
		{
			mAppendable.append(aValue.toString());
		}
		else if (aValue instanceof Double)
		{
			String v = aValue.toString();
			if (!v.contains("."))
			{
				v += ".0";
			}
			mAppendable.append(v);
		}
		else if (aValue instanceof Long)
		{
			mAppendable.append(aValue + "L");
		}
		else if (aValue instanceof Float)
		{
			mAppendable.append(aValue + "f");
		}
		else if (aValue instanceof Character)
		{
			mAppendable.append((int)(Character)aValue + "c");
		}
		else if (aValue instanceof Short)
		{
			mAppendable.append(aValue + "s");
		}
		else if (aValue instanceof Byte)
		{
			mAppendable.append(aValue + "b");
		}
		else if (aValue instanceof Boolean)
		{
			mAppendable.append(aValue.toString());
		}
		else if (aValue instanceof Bundle)
		{
			writeBundle((Bundle)aValue);
		}
		else if (aValue instanceof Date)
		{
			if (mDateFormatter == null)
			{
				mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			}
			mAppendable.append("#").append(mDateFormatter.format(aValue)).append("#");
		}
		else
		{
			throw new IllegalArgumentException("Bad type: " + aValue.getClass());
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