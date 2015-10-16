package org.terifan.bundle;

import java.util.Date;
import java.util.List;


enum FieldType
{
	BOOLEAN(Boolean.class, Boolean.TYPE, 5, 0b11000),
	BYTE(Byte.class, Byte.TYPE, 5, 0b11001),
	SHORT(Short.class, Short.TYPE, 5, 0b11010),
	CHAR(Character.class, Character.TYPE, 5, 0b11011),
	INT(Integer.class, Integer.TYPE, 2, 0b00),
	LONG(Long.class, Long.TYPE, 5, 0b11100),
	FLOAT(Float.class, Float.TYPE, 5, 0b11101),
	DOUBLE(Double.class, Double.TYPE, 2, 0b01),
	STRING(String.class, String.class, 2, 0b10),
	BUNDLE(Bundle.class, Bundle.class, 5, 0b11110),
	NULL(null,null, 6, 0b111110),
	EMPTY_LIST(null,null, 7, 0b1111110),
	DATE(Date.class, Date.class, 7, 0b1111111);

	private final Class mComponentType;
	private final Class mPrimitiveType;
	private final int mSymbolLength;
	private final int mSymbol;

	final static FieldType[] DECODER_ORDER =
	{
		INT, DOUBLE, STRING, // 2 bits
		BOOLEAN, BYTE, SHORT, CHAR, LONG, FLOAT, BUNDLE, // 5 bits
		NULL, // 6 bits
		EMPTY_LIST, DATE // 7 bits
	};


	private FieldType(Class aComponentType, Class aPrimitiveType, int aSymbolLength, int aSymbol)
	{
		mComponentType = aComponentType;
		mPrimitiveType = aPrimitiveType;
		mSymbolLength = aSymbolLength;
		mSymbol = aSymbol;
	}


	public int getSymbol()
	{
		return mSymbol;
	}


	public int getSymbolLength()
	{
		return mSymbolLength;
	}


	public Class getPrimitiveType()
	{
		return mPrimitiveType;
	}


	public Class getComponentType()
	{
		return mComponentType;
	}


	static FieldType valueOf(Object aObject)
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
				return EMPTY_LIST;
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
		if (this == EMPTY_LIST)
		{
			return name();
		}
		String name = mComponentType.getSimpleName();
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
}
