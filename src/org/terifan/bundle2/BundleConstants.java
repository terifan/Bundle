package org.terifan.bundle2;

import java.util.HashMap;
import java.util.Map;


class BundleConstants
{
	final static int VERSION = 0;

	final static Map<Class, Integer> TYPES = new HashMap<>()
	{
		{
			put(Boolean.class, 0);
			put(Byte.class, 1);
			put(Short.class, 2);
			put(Integer.class, 3);
			put(Long.class, 4);
			put(Float.class, 5);
			put(Double.class, 6);
			put(String.class, 7);
			put(BundleX.class, 8);
			put(BundleX.BooleanArray.class, 9);
			put(BundleX.NumberArray.class, 10);
			put(BundleX.StringArray.class, 11);
			put(BundleX.BundleArray.class, 12);
		}
	};
}
