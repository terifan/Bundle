package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import org.terifan.bundle.BinaryDecoder.PathEvaluation;
import org.terifan.bundle.JSONEncoder.Printer;


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


	public Bundle(ByteArrayInputStream aValue) throws IOException
	{
		this(aValue, new PathEvaluation());
	}


	public Bundle(ByteArrayInputStream aValue, PathEvaluation aPathEvaluation) throws IOException
	{
		this();

		new BinaryDecoder().unmarshal(aValue, aPathEvaluation, this);
	}


	public Bundle(byte[] aValue) throws IOException
	{
		this(new ByteArrayInputStream(aValue));
	}


	public Bundle(byte[] aValue, PathEvaluation aPathEvaluation) throws IOException
	{
		this(new ByteArrayInputStream(aValue), aPathEvaluation);
	}


	public Bundle(String aBundle) throws IOException
	{
		this();

		new JSONDecoder(new StringReader(aBundle)).unmarshal(this);
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


	private Object castArray(String aKey, Class aType)
	{
		Array array = (Array)get(aKey);
		Object elements = java.lang.reflect.Array.newInstance(aType, array.size());
		for (int i = 0; i < array.size(); i++)
		{
			java.lang.reflect.Array.set(elements, i, array.get(i));
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


	public byte[] marshal() throws IOException
	{
		return new BinaryEncoder().marshal(this);
	}


	@Override
	public void writeExternal(ObjectOutput aOut) throws IOException
	{
		byte[] data = marshal();
		aOut.writeInt(data.length);
		aOut.write(data);
	}


	@Override
	public void readExternal(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		int size = aIn.readInt();
		byte[] buf = new byte[size];
		aIn.read(buf);

		new BinaryDecoder().unmarshal(new ByteArrayInputStream(buf), new PathEvaluation(), this);
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
		return toJSON(new StringBuilder(), true).toString();
	}


	/**
	 * Return this Bundle as a JSON.
	 *
	 * @param aAppendable
	 *   bundle JSON is written to this Appendable
	 * @param aCompact
	 *   if false the JSON produced will be formatted
	 * @return
	 *   return this Bundle as a JSON
	 */
	public <T extends Appendable> T toJSON(T aAppendable, boolean aCompact)
	{
		new JSONEncoder().marshalBundle(new Printer(aAppendable, aCompact), this);

		return aAppendable;
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
}
