package org.terifan.bundle;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


class BundleConstants
{
	final static int VERSION          = 0b00000000;
	final static int VERSION_MASK     = 0b00011111;
	final static int CONTAINER_MASK   = 0b01100000;
	final static int CONTAINER_BUNDLE = 0b00100000;
	final static int CONTAINER_ARRAY  = 0b01000000;

	final static int BOOLEAN = 0;
	final static int BYTE = 1;
	final static int SHORT = 2;
	final static int INT = 3;
	final static int LONG = 4;
	final static int FLOAT = 5;
	final static int DOUBLE = 6;
	final static int STRING = 7;
	final static int BUNDLE = 8;
	final static int ARRAY = 9;
	final static int DATE = 10;
	final static int BINARY = 11;
	final static int UUID = 12;
	final static int CALENDAR = 13;
	final static int BIGINTEGER = 14;
	final static int BIGDECIMAL = 15;
	final static int NULL = 16;

	final static Map<Class, Integer> TYPES = new HashMap<Class, Integer>()
	{
		{
			put(Boolean.class, BOOLEAN);
			put(Byte.class, BYTE);
			put(Short.class, SHORT);
			put(Integer.class, INT);
			put(Long.class, LONG);
			put(Float.class, FLOAT);
			put(Double.class, DOUBLE);
			put(String.class, STRING);
			put(Bundle.class, BUNDLE);
			put(Array.class, ARRAY);
			put(Date.class, DATE);
			put(byte[].class, BINARY);
			put(UUID.class, UUID);
			put(GregorianCalendar.class, CALENDAR);
			put(BigInteger.class, BIGINTEGER);
			put(BigDecimal.class, BIGDECIMAL);
			put(null, NULL);
		}
	};


	static void assertSupportedType(Object aValue)
	{
		if (aValue != null && !TYPES.containsKey(aValue.getClass()))
		{
			throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}
	}
}
