package org.terifan.bundle;

import java.util.ArrayList;


enum ObjectType
{
	VALUE,
	ARRAY,
	ARRAYLIST,
	MATRIX;


	static ObjectType classify(Object aValue)
	{
		Class<? extends Object> cls = aValue.getClass();

		if (cls.isArray() && cls.getComponentType().isArray())
		{
			return MATRIX;
		}
		else if (cls.isArray())
		{
			return ARRAY;
		}
		else if (aValue instanceof ArrayList)
		{
			return ARRAYLIST;
		}
		else
		{
			return VALUE;
		}
	}
}