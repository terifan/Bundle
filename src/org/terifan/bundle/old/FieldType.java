package org.terifan.bundle.old;

import java.io.Serializable;
import java.util.Date;


class FieldType
{
	public final static int VALUE = 0x00;
	public final static int ARRAY = 0x10;
	public final static int ARRAYLIST = 0x20;
	public final static int MATRIX = 0x30;

	// must not be zero
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
	public final static int SERIALIZABLE = 0x0c;

	// prefix codes used by pson encoder
	final static String[] COLLECTION_TYPES = {"","a","q","m"};
	final static String[] VALUE_TYPES = {"z","x","y","c","i","l","f","d","s","b","t","o"};

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
		Date.class,
		Serializable.class
	};


	static int classify(Object aValue)
	{
		if (aValue instanceof Boolean)
		{
			return encode(VALUE, BOOLEAN);
		}
		if (aValue instanceof Byte)
		{
			return encode(VALUE, BYTE);
		}
		if (aValue instanceof Short)
		{
			return encode(VALUE, SHORT);
		}
		if (aValue instanceof Character)
		{
			return encode(VALUE, CHAR);
		}
		if (aValue instanceof Integer)
		{
			return encode(VALUE, INT);
		}
		if (aValue instanceof Long)
		{
			return encode(VALUE, LONG);
		}
		if (aValue instanceof Float)
		{
			return encode(VALUE, FLOAT);
		}
		if (aValue instanceof Double)
		{
			return encode(VALUE, DOUBLE);
		}
		if (aValue instanceof String)
		{
			return encode(VALUE, STRING);
		}
		if (aValue instanceof Bundle)
		{
			return encode(VALUE, BUNDLE);
		}
		if (aValue instanceof Date)
		{
			return encode(VALUE, DATE);
		}
		if (aValue instanceof Serializable)
		{
			return encode(VALUE, SERIALIZABLE);
		}

		throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
	}


	private FieldType()
	{
	}


	static Class classTypeOf(int aFieldType)
	{
		return TYPES[valueTypeOf(aFieldType) - 1];
	}


	static int collectionTypeOf(int aFieldType)
	{
		return aFieldType & 0xf0;
	}


	static int valueTypeOf(int aFieldType)
	{
		return aFieldType & 0x0f;
	}


	static int encode(int aCollectionType, int aValueType)
	{
		assert (aCollectionType & ~0xf0) == 0 : aCollectionType;
		assert (aValueType & ~0x0f) == 0 && (aValueType & 0x0f) != 0 : aValueType;

		return aCollectionType + aValueType;
	}


	static String toString(int aCode)
	{
		String type = classTypeOf(aCode).getSimpleName();

		switch (collectionTypeOf(aCode))
		{
			case VALUE:
				return type;
			case ARRAY:
				return type + "[]";
			case MATRIX:
				return type + "[][]";
			case ARRAYLIST:
				return "List<" + type + ">";
			default:
				throw new IllegalArgumentException();
		}
	}
}