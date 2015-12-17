package org.terifan.bundle;

import java.util.Date;


class FieldType
{
	public final static int VALUE = 0x10;
	public final static int ARRAY = 0x20;
	public final static int ARRAYLIST = 0x30;
	public final static int MATRIX = 0x40;

	public final static int BOOLEAN = 0x01;
	public final static int BYTE = 0x02;
	public final static int SHORT = 0x03;
	public final static int CHAR = 0x04;
	public final static int INT = 0x05;
	public final static int LONG = 0x06;
	public final static int FLOAT = 0x07;
	public final static int DOUBLE = 0x08;
	public final static int STRING = 0x09;
	public final static int BUNDLE = 0x0a;
	public final static int DATE = 0x0b;

	private final static Class[] TYPES =
	{
		Boolean.TYPE,
		Byte.TYPE,
		Short.TYPE,
		Character.TYPE,
		Integer.TYPE,
		Long.TYPE,
		Float.TYPE,
		Double.TYPE,
		String.class,
		Bundle.class,
		Date.class
	};


	private FieldType()
	{
	}


	static Class getPrimitiveType(int aFieldType)
	{
		return TYPES[valueType(aFieldType) - 1];
	}


	static int collectionType(int aFieldType)
	{
		return aFieldType & 0xf0;
	}


	static int valueType(int aFieldType)
	{
		return aFieldType & 0x0f;
	}


	static int encode(int aCollectionType, int aValueType)
	{
		assert (aCollectionType & ~0xf0) == 0 : aCollectionType;
		assert (aValueType & ~0x0f) == 0 : aValueType;

		return aCollectionType + aValueType;
	}


//	static int classify(Class aClass)
//	{
//		int collectionType;
//
//		if (aClass.isArray() && aClass.getComponentType().isArray())
//		{
//			collectionType = MATRIX;
//		}
//		else if (aClass.isArray())
//		{
//			collectionType = ARRAY;
//		}
//		else if (List.class.isAssignableFrom(aClass))
//		{
//			collectionType = ARRAYLIST;
//		}
//		else
//		{
//			collectionType = VALUE;
//		}
//
//		while (aClass.isArray())
//		{
//			aClass = aClass.getComponentType();
//		}
//
//		for (ValueType valueType : ValueType.values())
//		{
//			if (valueType.mComponentType.isAssignableFrom(aClass) || valueType.mPrimitiveType.isAssignableFrom(aClass))
//			{
//				return encode(collectionType, valueType.mType);
//			}
//		}
//
//		throw new IllegalArgumentException("Unsupported type: " + collectionType + " " + aClass.getCanonicalName());
//	}
//
//
//	private enum ValueType
//	{
//		BOOLEAN(Boolean.class, Boolean.TYPE),
//		BYTE(Byte.class, Byte.TYPE),
//		SHORT(Short.class, Short.TYPE),
//		CHAR(Character.class, Character.TYPE),
//		INT(Integer.class, Integer.TYPE),
//		LONG(Long.class, Long.TYPE),
//		FLOAT(Float.class, Float.TYPE),
//		DOUBLE(Double.class, Double.TYPE),
//		STRING(String.class, String.class),
//		BUNDLE(Bundle.class, Bundle.class),
//		DATE(Date.class, Date.class);
//
//		private final Class mComponentType;
//		private final Class mPrimitiveType;
//		private final int mType;
//
//		private ValueType(Class aComponentType, Class aPrimitiveType)
//		{
//			mComponentType = aComponentType;
//			mPrimitiveType = aPrimitiveType;
//			mType = ordinal() + 1;
//		}
//	}
}