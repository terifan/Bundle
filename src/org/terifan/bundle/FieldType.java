package org.terifan.bundle;

import java.util.Date;
import java.util.List;


enum FieldType
{
	BOOLEAN(Boolean.class, Boolean.TYPE),
	BYTE(Byte.class, Byte.TYPE),
	SHORT(Short.class, Short.TYPE),
	CHAR(Character.class, Character.TYPE),
	INT(Integer.class, Integer.TYPE),
	LONG(Long.class, Long.TYPE),
	FLOAT(Float.class, Float.TYPE),
	DOUBLE(Double.class, Double.TYPE),
	STRING(String.class, String.class),
	BUNDLE(Bundle.class, Bundle.class),
	DATE(Date.class, Date.class),
	EMPTY(null,null),
	NULL(null,null);

	private final Class mComponentType;
	private final Class mPrimitiveType;


	private FieldType(Class aComponentType, Class aPrimitiveType)
	{
		mComponentType = aComponentType;
		mPrimitiveType = aPrimitiveType;
	}


	public Class getPrimitiveType()
	{
		return mPrimitiveType;
	}


	public Class getComponentType()
	{
		return mComponentType;
	}


	static FieldType classify(Object aObject)
	{
		if (aObject == null)
		{
			return NULL;
		}

		Class cls = aObject.getClass();

		if (List.class.isAssignableFrom(cls))
		{
			cls = null;
			for (Object o : (List)aObject)
			{
				if (o != null)
				{
					cls = o.getClass();
					break;
				}
			}
			if (cls == null)
			{
				return EMPTY;
			}
		}
		if (cls.isArray())
		{
			cls = cls.getComponentType();
			if (cls.isArray())
			{
				cls = cls.getComponentType();
			}
		}
		for (FieldType fieldType : values())
		{
			if (fieldType.mComponentType != null && (fieldType.mComponentType.isAssignableFrom(cls) || fieldType.mPrimitiveType.isAssignableFrom(cls)))
			{
				return fieldType;
			}
		}

		throw new IllegalArgumentException("Unsupported type: " + cls);
	}


	public String getJavaName()
	{
		if (this == EMPTY)
		{
			return name();
		}
		String name = mComponentType.getSimpleName();
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
}
