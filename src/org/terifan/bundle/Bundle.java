package org.terifan.bundle;

import java.io.Externalizable;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;


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


	public Bundle(String aJSON)
	{
		this();

		unmarshalJSON(new StringReader(aJSON));
	}


	@Override
	public Object get(String aKey)
	{
		return mValues.get(aKey);
	}


	@Override
	Bundle set(String aKey, Object aValue)
	{
		if (aKey == null)
		{
			throw new IllegalArgumentException("Keys cannot be null.");
		}
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


	public ArrayList<Boolean> getBooleanArray(String aKey)
	{
		return castArray(aKey, Boolean.class);
	}


	public ArrayList<Byte> getByteArray(String aKey)
	{
		return castArray(aKey, Byte.class);
	}


	public ArrayList<Short> getShortArray(String aKey)
	{
		return castArray(aKey, Short.class);
	}


	public ArrayList<Integer> getIntArray(String aKey)
	{
		return castArray(aKey, Integer.class);
	}


	public ArrayList<Long> getLongArray(String aKey)
	{
		return castArray(aKey, Long.class);
	}


	public ArrayList<Float> getFloatArray(String aKey)
	{
		return castArray(aKey, Float.class);
	}


	public ArrayList<Double> getDoubleArray(String aKey)
	{
		return castArray(aKey, Double.class);
	}


	public ArrayList<Number> getNumberArray(String aKey)
	{
		return castArray(aKey, Number.class);
	}


	public ArrayList<Bundle> getBundleArray(String aKey)
	{
		return castArray(aKey, Bundle.class);
	}


	public ArrayList<String> getStringArray(String aKey)
	{
		return castArray(aKey, String.class);
	}


	private <T> ArrayList<T> castArray(String aKey, Class<T> aType)
	{
		Array array = (Array)get(aKey);

		if (array == null)
		{
			return null;
		}

		ArrayList<T> out = new ArrayList<>();

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
				else if (aType == Double.class)
				{
					v = ((Number)v).doubleValue();
				}
				else if (aType == String.class)
				{
					v = v.toString();
				}
				else if (aType == Byte.class)
				{
					v = ((Number)v).byteValue();
				}
				else if (aType == Float.class)
				{
					v = ((Number)v).floatValue();
				}
				else if (aType == Short.class)
				{
					v = ((Number)v).shortValue();
				}
			}
			else if (v instanceof String & v.getClass() != aType)
			{
				String s = (String)v;

				if (aType == Integer.class)
				{
					v = Integer.parseInt(s);
				}
				else if (aType == Long.class)
				{
					v = Long.parseLong(s);
				}
				else if (aType == Double.class)
				{
					v = Double.parseDouble(s);
				}
				else if (aType == Byte.class)
				{
					v = Byte.parseByte(s);
				}
				else if (aType == Float.class)
				{
					v = Float.parseFloat(s);
				}
				else if (aType == Short.class)
				{
					v = Short.parseShort(s);
				}
			}

			out.add((T)v);
		}

		return out;
	}


	public <T extends Bundlable> T[] getObjectArray(Class<T> aType, String aKey)
	{
		try
		{
			Array in = getArray(aKey);

			T[] out = (T[])java.lang.reflect.Array.newInstance(aType, in.size());

			Constructor<T> declaredConstructor = aType.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);

			for (int i = 0; i < in.size(); i++)
			{
				Object value = in.get(i);

				if (value instanceof Bundle)
				{
					T instance = declaredConstructor.newInstance();
					instance.readExternal((Bundle)value);
					out[i] = instance;
				}
			}

			return out;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public <T extends Bundlable> ArrayList<T> getObjectArrayList(Class<T> aType, String aKey)
	{
		try
		{
			ArrayList<T> list = new ArrayList<>();

			Constructor<T> declaredConstructor = aType.getDeclaredConstructor();
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


	/**
	 * Creates and instance of the type provided and unmarshals it using the readExternal method of Bundlable interface.
	 *
	 * Bundle.unmarshalJSON("{\"value\":7}").asObject(MyValue.class)
	 */
	public <T extends Bundlable> T newInstance(Class<T> aType)
	{
		try
		{
			Constructor<T> declaredConstructor = aType.getDeclaredConstructor();
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


	public <T> T newInstance(Class<T> aType, Function<Bundle,T> aImporter)
	{
		return aImporter.apply(this);
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


	public Collection<Object> values()
	{
		return mValues.values();
	}


	@Override
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
			return marshalJSON(true).equals(((Bundle)aOther).marshalJSON(true));
		}

		return false;
	}


	/**
	 * Create a Bundle from object provided.
	 *
	 * @param aBundlable
	 *   a Bundlable object
	 * @return
	 *   a Bundle
	 */
	public static Bundle of(Bundlable aBundlable)
	{
		Bundle bundle = new Bundle();
		aBundlable.writeExternal(bundle);
		return bundle;
	}


	/**
	 * Appends all entries from the provided Bundle to this Bundle.
	 *
	 * @param aOther
	 *   another bundle
	 * @return
	 *   this bundle
	 */
	public Bundle append(Bundle aOther)
	{
		for (Entry<String,Object> entry : mValues.entrySet())
		{
			mValues.put(entry.getKey(), entry.getValue());
		}

		return this;
	}


	@Override
	public Map<String, Object> toMap()
	{
		return new LinkedHashMap<>(mValues);
	}
}
