package org.terifan.bundle2;

import java.util.HashMap;
import java.util.Map;
import org.terifan.bundle2.BundleX.BooleanArray;
import org.terifan.bundle2.BundleX.BundleArray;
import org.terifan.bundle2.BundleX.NumberArray;
import org.terifan.bundle2.BundleX.StringArray;


class BundleConstants
{
	final static int VERSION = 0;

	final static int BOOLEAN = 0;
	final static int BYTE = 1;
	final static int SHORT = 2;
	final static int INT = 3;
	final static int LONG = 4;
	final static int FLOAT = 5;
	final static int DOUBLE = 6;
	final static int STRING = 7;
	final static int BUNDLE = 8;
	final static int BOOLEANARRAY = 9;
	final static int NUMBERARRAY = 10;
	final static int STRINGARRAY = 11;
	final static int BUNDLEARRAY = 12;

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
			put(BundleX.class, BUNDLE);
			put(BooleanArray.class, BOOLEANARRAY);
			put(NumberArray.class, NUMBERARRAY);
			put(StringArray.class, STRINGARRAY);
			put(BundleArray.class, BUNDLEARRAY);
		}
	};
}
