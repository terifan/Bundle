package org.terifan.bundle;

import java.util.HashMap;
import java.util.Map;
import org.terifan.bundle.Bundle.BundleArray;


class BundleConstants
{
	final static int VERSION = 1;

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

	final static Map<Class, Integer> TYPES = new HashMap<>()
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
			put(BundleArray.class, ARRAY);
		}
	};
}
