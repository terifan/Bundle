package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;


abstract class Container<K,R>
{
	Container()
	{
	}


	public abstract Object get(K aKey);


	abstract R put(K aKey, Object aValue);
	
	
	public Byte getByte(K aKey)
	{
		return (Byte)get(aKey);
	}


	public Short getShort(K aKey)
	{
		return (Short)get(aKey);
	}


	public Integer getInt(K aKey)
	{
		return (Integer)get(aKey);
	}


	public Long getLong(K aKey)
	{
		return (Long)get(aKey);
	}


	public Float getFloat(K aKey)
	{
		return (Float)get(aKey);
	}


	public Double getDouble(K aKey)
	{
		return (Double)get(aKey);
	}


	public String getString(K aKey)
	{
		return (String)get(aKey);
	}


	public R putString(K aKey, String aValue)
	{
		put(aKey, aValue);
		return (R)this;
	}


	public Number getNumber(K aKey)
	{
		return (Number)get(aKey);
	}


	public R putNumber(K aKey, Number aValue)
	{
		put(aKey, aValue);
		return (R)this;
	}
	
	
	public Calendar getCalendar(K aKey)
	{
		Object value = get(aKey);
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


	public R putCalendar(K aKey, Calendar aCalendar)
	{
		put(aKey, aCalendar);
		return (R)this;
	}


	public Boolean getBoolean(K aKey)
	{
		return (Boolean)get(aKey);
	}


	public R putBoolean(K aKey, Boolean aValue)
	{
		put(aKey, aValue);
		return (R)this;
	}


	public Array getArray(K aKey)
	{
		return (Array)get(aKey);
	}


	public R putArray(K aKey, Array aValue)
	{
		put(aKey, aValue);
		return (R)this;
	}


	public Bundle getBundle(K aKey)
	{
		return (Bundle)get(aKey);
	}


	public R putBundle(K aKey, Bundle aValue)
	{
		put(aKey, aValue);
		return (R)this;
	}


	public R putBundle(K aKey, Bundlable aValue)
	{
		Bundle bundle = new Bundle();
		aValue.writeExternal(bundle);
		put(aKey, bundle);
		return (R)this;
	}


	public <T extends BundlableValue> T getObject(Class<T> aType, K aKey)
	{
		try
		{
			Constructor<T> declaredConstructor = aType.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);

			T instance = declaredConstructor.newInstance();
			instance.readExternal(get(aKey));

			return instance;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public R putObject(K aKey, BundlableType aValue)
	{
		if (aValue instanceof BundlableValue)
		{
			put(aKey, ((BundlableValue)aValue).writeExternal());
		}
		else if (aValue instanceof Bundlable)
		{
			Bundle bundle = new Bundle();
			((Bundlable)aValue).writeExternal(bundle);
			put(aKey, bundle);
		}
		return (R)this;
	}


	public <T extends Serializable> T getSerializable(Class<T> aType, K aKey)
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


	public R putSerializable(K aKey, Serializable aValue)
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


	public Date getDate(K aKey)
	{
		Object date = get(aKey);
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


	public R putDate(K aKey, Date aDate)
	{
		put(aKey, aDate);
		return (R)this;
	}
	
	
	public byte[] getBinary(K aKey)
	{
		Object value = get(aKey);
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
	
	
	public R putBinary(K aKey, byte[] aBytes)
	{
		put(aKey, aBytes);
		return (R)this;
	}
	
	
	public UUID getUUID(K aKey)
	{
		Object value = get(aKey);
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
	

	public R putUUID(K aKey, UUID aBytes)
	{
		put(aKey, aBytes);
		return (R)this;
	}
	
	
	public abstract R remove(K aKey);

	
	public abstract int size();


	public abstract R clear();
}
