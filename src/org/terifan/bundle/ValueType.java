package org.terifan.bundle;

import java.util.Date;


// Maximum 16 values
enum ValueType
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
	DATE(Date.class, Date.class);

	private final Class mComponentType;
	private final Class mPrimitiveType;


	private ValueType(Class aComponentType, Class aPrimitiveType)
	{
		mComponentType = aComponentType;
		mPrimitiveType = aPrimitiveType;
	}


	Class getPrimitiveType()
	{
		return mPrimitiveType;
	}


	static ValueType classify(Class aClass)
	{
		for (ValueType fieldType : values())
		{
			if (fieldType.mComponentType.isAssignableFrom(aClass) || fieldType.mPrimitiveType.isAssignableFrom(aClass))
			{
				return fieldType;
			}
		}

		throw new IllegalArgumentException("Unsupported type: " + aClass);
	}
}