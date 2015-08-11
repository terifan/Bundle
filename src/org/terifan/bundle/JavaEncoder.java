package org.terifan.bundle;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class JavaEncoder
{
	/**
	 * Returns this Bundle as Java code.
	 *
	 * @return
	 *   the Java code as a String
	 */
	public String marshal(Bundle aBundle) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		marshal(aBundle, sb);
		return sb.toString();
	}


	/**
	 * Returns this Bundle as Java code.
	 *
	 * @return
	 *   the provided Appendable
	 */
	public Appendable marshal(Bundle aBundle, Appendable aAppendable) throws IOException
	{
		aAppendable.append("Bundle thisBundle = ");
		write(aBundle, "\t", aAppendable);
		aAppendable.append(";");
		return aAppendable;
	}


	private void write(Bundle aBundle, String aIndent, Appendable aAppendable) throws IOException
	{
		aAppendable.append("new Bundle()");

		ArrayList<String> keys = new ArrayList<>(aBundle.keySet());
		Collections.sort(keys);

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.valueOf(value);

			if (value != null)
			{
				Class<? extends Object> cls = value.getClass();
				String typeName = fieldType.getJavaName();

				if (cls.isArray())
				{
					aAppendable.append("\n" + aIndent + ".put"+typeName+"Array(\"" + key + "\"");
					for (int i = 0, sz = Array.getLength(value); i < sz; i++)
					{
						if (fieldType == FieldType.BUNDLE)
						{
							aAppendable.append("\n"+aIndent+"\t, ");
							Bundle bundle = ((Bundle)Array.get(value, i));
							if (bundle == null)
							{
								aAppendable.append("null");
							}
							else
							{
								write(bundle, aIndent + "\t\t", aAppendable);
							}
						}
						else
						{
							aAppendable.append(", ");
							aAppendable.append(valueToString(fieldType, Array.get(value, i)));
						}
					}
					aAppendable.append(")");
				}
				else if (List.class.isAssignableFrom(cls))
				{
					aAppendable.append("\n" + aIndent + ".put"+typeName+"ArrayList(\"" + key + "\", ");
					aAppendable.append("new ArrayList<>(");
					int sz = ((ArrayList)value).size();
					if (sz > 0)
					{
						aAppendable.append("Arrays.asList(");
						for (int i = 0; i < sz; i++)
						{
							if (fieldType == FieldType.BUNDLE)
							{
								aAppendable.append("\n" + aIndent + "\t");
								aAppendable.append(i == 0 ? "  " : ", ");
								Bundle bundle = ((Bundle)((ArrayList)value).get(i));
								if (bundle == null)
								{
									aAppendable.append("null");
								}
								else
								{
									write(bundle, aIndent + "\t\t", aAppendable);
								}
							}
							else
							{
								aAppendable.append(i == 0 ? "" : ", ");
								aAppendable.append(valueToString(fieldType, ((ArrayList)value).get(i)));
							}
						}
						aAppendable.append(")");
					}
					aAppendable.append(")");
				}
				else
				{
					aAppendable.append("\n" + aIndent + ".put"+typeName+"(\"" + key + "\", ");
					if (fieldType == FieldType.BUNDLE)
					{
						write((Bundle)value, aIndent + "\t", aAppendable);
					}
					else
					{
						aAppendable.append(valueToString(fieldType, value));
					}
					aAppendable.append(")");
				}
			}
		}
	}


	private String valueToString(FieldType aFieldType, Object aValue)
	{
		if (aValue == null)
		{
			return "null";
		}

		switch (aFieldType)
		{
			case BOOLEAN:
				return aValue.toString();
			case BYTE:
				return "(byte)" + aValue;
			case SHORT:
				return "(short)" + aValue;
			case CHAR:
				return (Character.isJavaIdentifierPart((Character)aValue) ? "'" + (Character)aValue + "'" : "(char)" + aValue);
			case INT:
				return aValue.toString();
			case LONG:
				return aValue + "L";
			case FLOAT:
				return aValue + "f";
			case DOUBLE:
				return aValue.toString();
			case DATE:
				return "new java.util.Date(" + ((Date)aValue).getTime() + "L)";
			case STRING:
				String s = aValue.toString();
				s = s.replace("\"","\\\"");
				s = s.replace("\n","\\n");
				s = s.replace("\r","\\r");
				s = s.replace("\t","\\t");
				for (int i = 0; i < 32; i++)
				{
					s = s.replace(Character.toString((char)i), String.format("\\u%04x",i));
				}
				return "\"" + s + "\"";
			default:
				throw new InternalError();
		}
	}
}
