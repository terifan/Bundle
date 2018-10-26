package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.terifan.bundle.BinaryDecoder.PathEvaluation;


public class Bundle implements Serializable, Externalizable
{
	private static final long serialVersionUID = 1L;

	private HashMap<String, Object> mValues;


	public Bundle()
	{
		mValues = new HashMap<>();
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


//	public BundleX(String aBundle)
//	{
//		this();
//
//		decode(aBundle);
//	}


	public Object get(String aKey)
	{
		return mValues.get(aKey);
	}


	void put(String aKey, Object aValue)
	{
		mValues.put(aKey, aValue);
	}


	public Bundle putArray(String aKey, BundleArray aValue)
	{
		mValues.put(aKey, aValue);
		return this;
	}


	public BundleArray getArray(String aKey)
	{
		return (BundleArray)mValues.get(aKey);
	}


	public Object[] toArray(String aKey)
	{
		BundleArray array = getArray(aKey);
		Object[] values = new Object[array.size()];
		for (int i = 0; i < array.size(); i++)
		{
			values[i] = array.get(i);
		}
		return values;
	}


	public Byte[] getByteArray(String aKey)
	{
		return (Byte[])toArray(aKey, Byte.class);
	}


	public Short[] getShortArray(String aKey)
	{
		return (Short[])toArray(aKey, Short.class);
	}


	public Integer[] getIntArray(String aKey)
	{
		return (Integer[])toArray(aKey, Integer.class);
	}


	public Long[] getLongArray(String aKey)
	{
		return (Long[])toArray(aKey, Long.class);
	}


	public Float[] getFloatArray(String aKey)
	{
		return (Float[])toArray(aKey, Float.class);
	}


	public Double[] getDoubleArray(String aKey)
	{
		return (Double[])toArray(aKey, Double.class);
	}


	private Object toArray(String aKey, Class aType)
	{
		BundleArray array = (BundleArray)mValues.get(aKey);
		Object elements = Array.newInstance(aType, array.size());
		for (int i = 0; i < array.size(); i++)
		{
			Array.set(elements, i, array.get(i));
		}
		return elements;
	}


	private class ArrayAccessor<T> implements Iterator<T>
	{
		private int mIndex = 0;
		private final BundleArray mArray;


		private ArrayAccessor(BundleArray aArray)
		{
			mArray = aArray;
		}


		@Override
		public boolean hasNext()
		{
			return mIndex < mArray.size();
		}


		@Override
		public T next()
		{
			return (T)mArray.get(mIndex++);
		}
	}


	public Bundle putBundle(String aKey, Bundle aValue)
	{
		mValues.put(aKey, aValue);
		return this;
	}


	public Bundle putBundle(String aKey, Bundlable aValue)
	{
		Bundle bundle = new Bundle();
		aValue.writeExternal(bundle);
		mValues.put(aKey, bundle);
		return this;
	}


	public Bundle getBundle(String aKey)
	{
		return (Bundle)mValues.get(aKey);
	}


	public <T extends BundlableValue> T getObject(Class<T> aValue, String aKey)
	{
		try
		{
			Constructor<T> declaredConstructor = aValue.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);

			T instance = declaredConstructor.newInstance();
			instance.readExternal(mValues.get(aKey));

			return instance;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public Bundle putObject(String aKey, BundlableType aValue)
	{
		if (aValue instanceof BundlableValue)
		{
			mValues.put(aKey, ((BundlableValue)aValue).writeExternal());
		}
		else
		{
			Bundle bundle = new Bundle();
			((Bundlable)aValue).writeExternal(bundle);
			mValues.put(aKey, bundle);
		}
		return this;
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


	public <T extends Serializable> T getSerializable(Class<T> aValue, String aKey)
	{
		try (ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode((String)mValues.get(aKey)))))
		{
			return (T)oos.readObject();
		}
		catch (IOException | ClassNotFoundException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public Bundle putSerializable(String aKey, Serializable aValue)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			oos.writeObject(aValue);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(e);
		}

		mValues.put(aKey, Base64.getEncoder().encodeToString(baos.toByteArray()));
		return this;
	}


	public Bundle putString(String aKey, String aValue)
	{
		mValues.put(aKey, aValue);
		return this;
	}


	public String getString(String aKey)
	{
		return (String)mValues.get(aKey);
	}


	public Bundle putNumber(String aKey, Number aValue)
	{
		mValues.put(aKey, aValue);
		return this;
	}


	public Number getNumber(String aKey)
	{
		return (Number)mValues.get(aKey);
	}


	public Byte getByte(String aKey)
	{
		return (Byte)mValues.get(aKey);
	}


	public Short getShort(String aKey)
	{
		return (Short)mValues.get(aKey);
	}


	public Integer getInt(String aKey)
	{
		return (Integer)mValues.get(aKey);
	}


	public Long getLong(String aKey)
	{
		return (Long)mValues.get(aKey);
	}


	public Float getFloat(String aKey)
	{
		return (Float)mValues.get(aKey);
	}


	public Double getDouble(String aKey)
	{
		return (Double)mValues.get(aKey);
	}


	public Bundle putBoolean(String aKey, Boolean aValue)
	{
		mValues.put(aKey, aValue);
		return this;
	}


	public Boolean getBoolean(String aKey)
	{
		return (Boolean)mValues.get(aKey);
	}


	public Bundle remove(String aKey)
	{
		mValues.remove(aKey);
		return this;
	}


	public Bundle clear()
	{
		mValues.clear();
		return this;
	}


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


	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("{");

		int size = size();

		for (Entry<String, Object> entry : entrySet())
		{
			builder.append("\"");
			builder.append(entry.getKey());
			builder.append("\":");

			Object v = entry.getValue();

			if (v instanceof String)
			{
				builder.append("\"");
			}

			builder.append(v);

			if (v instanceof String)
			{
				builder.append("\"");
			}

			if (--size > 0)
			{
				builder.append(",");
			}
		}

		builder.append("}");

		return builder.toString();
	}


//	public static abstract class BundleArrayValueType<T, U> extends BundleArrayType<T>
//	{
//		private static final long serialVersionUID = 1L;
//
//
//		public U add(BundlableValueX... aObject)
//		{
//			for (BundlableValueX o : aObject)
//			{
//				mValues.add((T)o.writeExternal());
//			}
//			return (U)this;
//		}
//	}


	public static class BundleArray implements Serializable, Iterable
	{
		private static final long serialVersionUID = 1L;

		protected ArrayList mValues;


		public BundleArray()
		{
			mValues = new ArrayList<>();
		}


		public int size()
		{
			return mValues.size();
		}


		public Object get(int aIndex)
		{
			return mValues.get(aIndex);
		}


		public BundleArray add(Object aValue)
		{
			mValues.add(aValue);
			return this;
		}


		public BundleArray add(Object... aValues)
		{
			mValues.addAll(Arrays.asList(aValues));
			return this;
		}


		@Override
		public Iterator iterator()
		{
			return mValues.iterator();
		}


		public Stream stream()
		{
			return mValues.stream();
		}


		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder("[");

			int size = size();

			for (Object v : this)
			{
				if (v instanceof String)
				{
					builder.append("\"").append(v).append("\"");
				}
				else
				{
					builder.append(v);
				}

				if (--size > 0)
				{
					builder.append(",");
				}
			}

			builder.append("]");

			return builder.toString();
		}


		public BundleArray add(Bundlable aValue)
		{
			Bundle bundle = new Bundle();
			aValue.writeExternal(bundle);
			mValues.add(bundle);
			return this;
		}


		public BundleArray add(Bundlable... aValues)
		{
			for (Bundlable o : aValues)
			{
				Bundle bundle = new Bundle();
				o.writeExternal(bundle);
				mValues.add(bundle);
			}
			return this;
		}
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


	@Override
	public int hashCode()
	{
		return mValues.hashCode();
	}


	@Override
	public boolean equals(Object aOther)
	{
		if (aOther instanceof Bundle)
		{
			return mValues.equals(((Bundle)aOther).mValues);
		}

		return false;
	}
}
