package org.terifan.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;


/**
 * A Bundle is typed Map that can be serialized to JSON and binary format.
 *
 * Note: the hashCode and equals methods are order independent even though the Bundle maintains elements in the inserted order.
 */
public class Bundle extends Container<String,Bundle> implements Serializable, Externalizable
{
	private static final long serialVersionUID = 1L;

	private LinkedHashMap<String, Object> mValues;


	public Bundle()
	{
		mValues = new LinkedHashMap<>();
	}


	public Bundle(Bundlable aValue)
	{
		this();

		aValue.writeExternal(this);
	}


	@Override
	public Object get(String aKey)
	{
		return mValues.get(aKey);
	}


	@Override
	Bundle put(String aKey, Object aValue)
	{
		mValues.put(aKey, aValue);
		return this;
	}


	public Object[] toArray(String aKey)
	{
		Array array = getArray(aKey);
		Object[] values = new Object[array.size()];
		for (int i = 0; i < array.size(); i++)
		{
			values[i] = array.get(i);
		}
		return values;
	}


	public Byte[] getByteArray(String aKey)
	{
		return (Byte[])castArray(aKey, Byte.class);
	}


	public Short[] getShortArray(String aKey)
	{
		return (Short[])castArray(aKey, Short.class);
	}


	public Integer[] getIntArray(String aKey)
	{
		return (Integer[])castArray(aKey, Integer.class);
	}


	public Long[] getLongArray(String aKey)
	{
		return (Long[])castArray(aKey, Long.class);
	}


	public Float[] getFloatArray(String aKey)
	{
		return (Float[])castArray(aKey, Float.class);
	}


	public Double[] getDoubleArray(String aKey)
	{
		return (Double[])castArray(aKey, Double.class);
	}


	public Bundle[] getBundleArray(String aKey)
	{
		return (Bundle[])castArray(aKey, Bundle.class);
	}


	private Object castArray(String aKey, Class aType)
	{
		Array array = (Array)get(aKey);

		if (array == null)
		{
			return null;
		}

		Object elements = java.lang.reflect.Array.newInstance(aType, array.size());
		for (int i = 0; i < array.size(); i++)
		{
			Object v = array.get(i);

			if (v instanceof Number && v.getClass() != aType)
			{
				if (aType == Integer.class)
				{
					v = ((Number)v).intValue();
				}
				else if (aType == Long.class)
				{
					v = ((Number)v).longValue();
				}
				else if (aType == Short.class)
				{
					v = ((Number)v).shortValue();
				}
				else if (aType == Byte.class)
				{
					v = ((Number)v).byteValue();
				}
				else if (aType == Float.class)
				{
					v = ((Number)v).floatValue();
				}
				else if (aType == Double.class)
				{
					v = ((Number)v).doubleValue();
				}
			}

			java.lang.reflect.Array.set(elements, i, v);
		}
		return elements;
	}


	public <T extends Bundlable> ArrayList<T> getObjectArray(Class<T> aValue, String aKey)
	{
		try
		{
			ArrayList<T> list = new ArrayList<>();

			Constructor<T> declaredConstructor = aValue.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);

			for (Object value : getArray(aKey))
			{
				if (value instanceof Bundle)
				{
					T instance = declaredConstructor.newInstance();
					instance.readExternal((Bundle)value);
					list.add(instance);
				}
			}

			return list;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public <T extends Bundlable> T asObject(Class<T> aValue)
	{
		try
		{
			Constructor<T> declaredConstructor = aValue.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);

			T instance = declaredConstructor.newInstance();
			instance.readExternal(this);

			return instance;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	@Override
	public Bundle remove(String aKey)
	{
		mValues.remove(aKey);
		return this;
	}


	@Override
	public Bundle clear()
	{
		mValues.clear();
		return this;
	}


	@Override
	public int size()
	{
		return mValues.size();
	}


	public Set<String> keySet()
	{
		return mValues.keySet();
	}


	public Set<Entry<String, Object>> entrySet()
	{
		return mValues.entrySet();
	}


	public boolean containsKey(String aKey)
	{
		return mValues.containsKey(aKey);
	}


	/**
	 * Return this Bundle as a compacted JSON.
	 *
	 * @return
	 *   return this Bundle as a compacted JSON
	 */
	@Override
	public String toString()
	{
		return marshalJSON(new StringBuilder(), true).toString();
	}


	@Override
	MurmurHash32 hashCode(MurmurHash32 aHash)
	{
		for (Entry<String,Object> entry : mValues.entrySet())
		{
			aHash.update(entry.getKey());
			super.hashCode(aHash, entry.getValue());
		}

		return aHash;
	}


	@Override
	public boolean equals(Object aOther)
	{
		if (aOther instanceof Bundle)
		{
//			return mValues.equals(((Bundle)aOther).mValues);

			// TODO: fix

			try
			{
				return Arrays.equals(marshal(), ((Bundle)aOther).marshal());
			}
			catch (IOException e)
			{
				throw new IllegalArgumentException(e);
			}
		}

		return false;
	}


	/**
	 * Create a Bundle from object provided assuming it implements the Bundlable interface.
	 *
	 * @param aBundlable
	 *   a Bundlable object
	 * @return
	 *   a Bundle
	 */
	public static Bundle of(Object aBundlable)
	{
		Bundle bundle = new Bundle();
		((Bundlable)aBundlable).writeExternal(bundle);
		return bundle;
	}
}
