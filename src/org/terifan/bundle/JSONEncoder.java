package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;


class JSONEncoder
{
	private final static int SIMPLE_OBJECT_MAX_ELEMENTS = 5;

	final static String[] COLLECTION_TYPES = {"","a","l","m"};
	final static String[] VALUE_TYPES = {"z","b","a","c","i","l","f","d","s","m","t","o"};

	private SimpleDateFormat mDateFormatter;
	private Appendable mAppendable;
	private boolean mFlat;
	private int mIndent;


	public void marshal(Bundle aBundle, Appendable aAppendable, boolean aFlat) throws IOException
	{
		mAppendable = aAppendable;
		mFlat = aFlat;
		mIndent = 0;

		writeBundle(aBundle);
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
				checkKey(key);

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

				int fieldType = aBundle.getType(key);
				mAppendable.append("\"").append(encodeKey(fieldType, key)).append("\": ");

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
						writeArray(value, fieldType);
					}
					else
					{
						writeValue(value, fieldType);
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


	private static String encodeKey(int aFieldType, String aKey)
	{
		return VALUE_TYPES[FieldType.valueType(aFieldType) - 1] + COLLECTION_TYPES[FieldType.collectionType(aFieldType) >> 4] + "!" + aKey;
	}


	private void checkKey(String aKey) throws IOException
	{
		if (aKey.contains("\"") || aKey.contains("'") || aKey.contains("\n") || aKey.contains("\r") || aKey.contains("\t"))
		{
			throw new IOException("Name contains illegal character: " + aKey);
		}
	}


	private void writeArray(Object aValue, int aFieldType) throws IOException
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
					writeArray(v, aFieldType);
				}
				else
				{
					writeValue(v, aFieldType);
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


	private void writeValue(Object aValue, int aFieldType) throws IOException
	{
		if (aValue == null)
		{
			mAppendable.append("null");
			return;
		}

		switch (FieldType.valueType(aFieldType))
		{
			case FieldType.CHAR:
				mAppendable.append("" + (int)(Character)aValue);
				break;
			case FieldType.DATE:
				if (mDateFormatter == null)
				{
					mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				}
				mAppendable.append("\"").append(mDateFormatter.format(aValue)).append("\"");
				break;
			case FieldType.STRING:
				mAppendable.append("\"").append(escapeString(aValue.toString())).append("\"");
				break;
			case FieldType.BUNDLE:
				writeBundle((Bundle)aValue);
				break;
			case FieldType.OBJECT:
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (ObjectOutputStream oos = new ObjectOutputStream(baos))
				{
					oos.writeObject(aValue);
				}
				mAppendable.append("\"").append(Base64.getEncoder().encodeToString(baos.toByteArray())).append("\"");
				break;
			default:
				mAppendable.append(aValue.toString());
				break;
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