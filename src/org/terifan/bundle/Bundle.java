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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Stream;
import org.terifan.bundle.BinaryDecoder.PathEvaluation;
import static org.terifan.bundle.BundleConstants.TYPES;


public class Bundle implements Serializable, Externalizable
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


	public BundleArray getArray(String aKey)
	{
		return (BundleArray)mValues.get(aKey);
	}


	public Bundle putArray(String aKey, BundleArray aValue)
	{
		mValues.put(aKey, aValue);
		return this;
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


//	private class ArrayAccessor<T> implements Iterator<T>
//	{
//		private int mIndex = 0;
//		private final BundleArray mArray;
//
//
//		private ArrayAccessor(BundleArray aArray)
//		{
//			mArray = aArray;
//		}
//
//
//		@Override
//		public boolean hasNext()
//		{
//			return mIndex < mArray.size();
//		}
//
//
//		@Override
//		public T next()
//		{
//			return (T)mArray.get(mIndex++);
//		}
//	}


	public Bundle getBundle(String aKey)
	{
		return (Bundle)mValues.get(aKey);
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
		Object value = getBinary(aKey);
		
		if (value == null)
		{
			return null;
		}

		try (ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(getBinary(aKey))))
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

		return putBinary(aKey, baos.toByteArray());
	}


	public String getString(String aKey)
	{
		return (String)mValues.get(aKey);
	}


	public Bundle putString(String aKey, String aValue)
	{
		mValues.put(aKey, aValue);
		return this;
	}


	public Number getNumber(String aKey)
	{
		return (Number)mValues.get(aKey);
	}


	public Bundle putNumber(String aKey, Number aValue)
	{
		mValues.put(aKey, aValue);
		return this;
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


	public Date getDate(String aKey)
	{
		Object date = mValues.get(aKey);
		if (date == null)
		{
			return null;
		}
		if (date instanceof Date)
		{
			return (Date)date;
		}
		if (date instanceof Long)
		{
			return new Date((Long)date);
		}
		if (date instanceof String)
		{
			try
			{
				String s = (String)date;
				switch (s.length())
				{
					case 8:
						return new SimpleDateFormat("yyyy-MM-dd").parse(s);
					case 14:
						return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(s);
					case 19:
						return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(s);
					case 23:
						return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(s);
					default:
						throw new IllegalArgumentException("Date format unsupported: " + s);
				}
			}
			catch (ParseException e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		return null;
	}


	public Bundle putDate(String aKey, Date aDate)
	{
		mValues.put(aKey, aDate);
		return this;
	}
	
	
	public byte[] getBinary(String aKey)
	{
		Object value = mValues.get(aKey);
		if (value == null)
		{
			return null;
		}
		if (value instanceof byte[])
		{
			return (byte[])value;
		}
		if (value instanceof String)
		{
			return Base64.getDecoder().decode((String)value);
		}
		throw new IllegalArgumentException("Unsupported format: " + value.getClass());
	}
	
	
	public Bundle putBinary(String aKey, byte[] aBytes)
	{
		mValues.put(aKey, aBytes);
		return this;
	}
	
	
	public UUID getUUID(String aKey)
	{
		Object value = mValues.get(aKey);
		if (value == null)
		{
			return null;
		}
		if (value instanceof UUID)
		{
			return (UUID)value;
		}
		if (value instanceof String)
		{
			return UUID.fromString((String)value);
		}
		throw new IllegalArgumentException("Unsupported format: " + value.getClass());
	}
	

	public Bundle putUUID(String aKey, UUID aBytes)
	{
		mValues.put(aKey, aBytes);
		return this;
	}
	
	
	public Calendar getCalendar(String aKey)
	{
		Object value = mValues.get(aKey);
		if (value == null)
		{
			return null;
		}
		if (value instanceof Calendar)
		{
			return (Calendar)value;
		}
		if (value instanceof String)
		{
			try
			{
				String s = (String)value;
				int offset = Integer.parseInt(s.substring(Math.max(s.lastIndexOf('+'), s.lastIndexOf('-')) + 1));
				Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse((s));
				TimeZone timeZone = TimeZone.getDefault();
				timeZone.setRawOffset(offset);
				Calendar calendar = Calendar.getInstance(timeZone);
				calendar.setTimeInMillis(date.getTime());
				return calendar;
			}
			catch (ParseException e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		throw new IllegalArgumentException("Unsupported format: " + value.getClass());
	}


	public Bundle putCalendar(String aKey, Calendar aCalendar)
	{
		mValues.put(aKey, aCalendar);
		return this;
	}


	public Boolean getBoolean(String aKey)
	{
		return (Boolean)mValues.get(aKey);
	}


	public Bundle putBoolean(String aKey, Boolean aValue)
	{
		mValues.put(aKey, aValue);
		return this;
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

			if (v instanceof UUID)
			{
				v = v.toString();
			}
			if (v instanceof Calendar)
			{
				Calendar c = (Calendar)v;
				int offset = c.getTimeZone().getRawOffset() / 1000;
				v = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(((Calendar)v).getTimeInMillis())) + (offset<0?"-":"+") + String.format("%02d:%02d", offset/60/60, (offset/60)%60);
			}
			if (v instanceof String)
			{
				builder.append("\"");
			}
			if (v instanceof Date)
			{
				v = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date)v);
			}
			if (v instanceof byte[])
			{
				v = Base64.getEncoder().encodeToString((byte[])v);
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


		public BundleArray(Object... aValues)
		{
			this();
			mValues.addAll(Arrays.asList(aValues));
		}


		public int size()
		{
			return mValues.size();
		}


		public Object get(int aIndex)
		{
			return mValues.get(aIndex);
		}


		public Number getNumber(int aIndex)
		{
			return (Number)mValues.get(aIndex);
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


		public BundleArray add(BundlableType aValue)
		{
			if (aValue instanceof BundlableValue)
			{
				Object value = ((BundlableValue)aValue).writeExternal();

				checkValue(value);

				mValues.add(value);
			}
			else
			{
				Bundle bundle = new Bundle();
				((Bundlable)aValue).writeExternal(bundle);
				mValues.add(bundle);
			}
			return this;
		}


		public BundleArray add(BundlableType... aValues)
		{
			for (BundlableType v : aValues)
			{
				add(v);
			}
			return this;
		}


		public BundleArray addAll(List<BundlableType> aValues)
		{
			for (BundlableType v : aValues)
			{
				add(v);
			}
			return this;
		}
	}


	private static void checkValue(Object aValue)
	{
		if (aValue == null || TYPES.containsKey(aValue.getClass()))
		{
			return;
		}

		throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
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
