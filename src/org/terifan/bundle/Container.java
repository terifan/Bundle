package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;


public abstract class Container<K,R> implements Serializable, Externalizable
{
	private final static long serialVersionUID = 1L;


	Container()
	{
	}


	public abstract Object get(K aKey);


	public Object get(K aKey, Object aDefaultValue)
	{
		Object value = (Object)get(aKey);
		if (value == null)
		{
			return aDefaultValue;
		}
		return value;
	}


	abstract R put(K aKey, Object aValue);


	public Byte getByte(K aKey)
	{
		return (Byte)get(aKey);
	}


	public Byte getByte(K aKey, Byte aDefaultValue)
	{
		Byte value = (Byte)get(aKey);
		if (value == null)
		{
			return aDefaultValue;
		}
		return value;
	}


	public Short getShort(K aKey)
	{
		return (Short)get(aKey);
	}


	public Short getShort(K aKey, Short aDefaultValue)
	{
		Short value = (Short)get(aKey);
		if (value == null)
		{
			return aDefaultValue;
		}
		return value;
	}


	public Integer getInt(K aKey)
	{
		return getInt(aKey, null);
	}


	public Integer getInt(K aKey, Integer aDefaultValue)
	{
		Object v = get(aKey);
		if (v == null)
		{
			return aDefaultValue;
		}
		if (v instanceof Long)
		{
			return (int)(long)(Long)v;
		}
		return (Integer)v;
	}


	public Long getLong(K aKey)
	{
		return getLong(aKey, null);
	}


	public Long getLong(K aKey, Long aDefaultValue)
	{
		Long value = (Long)get(aKey);
		if (value != null)
		{
			return aDefaultValue;
		}
		return value;
	}


	public Float getFloat(K aKey)
	{
		return (Float)get(aKey);
	}


	public Float getFloat(K aKey, Float aDefaultValue)
	{
		Float value = (Float)get(aKey);
		if (value == null)
		{
			return aDefaultValue;
		}
		return value;
	}


	public Double getDouble(K aKey)
	{
		return (Double)get(aKey);
	}


	public Double getDouble(K aKey, Double aDefaultValue)
	{
		Double value = (Double)get(aKey);
		if (value == null)
		{
			return aDefaultValue;
		}
		return value;
	}


	public String getString(K aKey)
	{
		return (String)get(aKey);
	}


	public String getString(K aKey, String aDefaultValue)
	{
		String value = (String)get(aKey);
		if (value == null)
		{
			return aDefaultValue;
		}
		return value;
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


	public Boolean getBoolean(K aKey, boolean aDefault)
	{
		return get(aKey) == null ? aDefault : (Boolean)get(aKey);
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


	public R putObject(K aKey, Object aValue, Converter aConverter)
	{
		aValue = aConverter.convert(aValue);

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


	@Override
	public int hashCode()
	{
		return (int)hashCode(new MurmurHash32(0)).finish();
	}


	void hashCode(MurmurHash32 aHash, Object aValue)
	{
		if (aValue instanceof Container)
		{
			((Container)aValue).hashCode(aHash);
		}
		else if (aValue instanceof CharSequence)
		{
			aHash.update((CharSequence)aValue);
		}
		else if (aValue instanceof byte[])
		{
			aHash.update((byte[])aValue);
		}
		else if (aValue instanceof int[])
		{
			aHash.update((int[])aValue);
		}
		else
		{
			aHash.update(Objects.hashCode(aValue));
		}
	}


	public abstract R remove(K aKey);


	public abstract int size();


	public abstract R clear();


	abstract MurmurHash32 hashCode(MurmurHash32 aHash);


	public String marshalJSON(boolean aCompact)
	{
		StringBuilder builder = new StringBuilder();
		marshalJSON(builder, aCompact);
		return builder.toString();
	}


	/**
	 * Return this Bundle as a JSON.
	 *
	 * @param aJSONOutput
	 *   bundle JSON is written to this Appendable
	 * @param aCompact
	 *   if false the JSON produced will be formatted
	 * @return
	 *   return this Bundle as a JSON
	 */
	public <T extends Appendable> T marshalJSON(T aJSONOutput, boolean aCompact)
	{
		try
		{
			new JSONEncoder().marshal(new JSONEncoder.Printer(aJSONOutput, aCompact), this);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(e);
		}

		return aJSONOutput;
	}


	public R unmarshalJSON(String aJSONData)
	{
		return unmarshalJSON(new StringReader(aJSONData));
	}


	public R unmarshalJSON(Reader aJSONData)
	{
		try
		{
			return (R)new JSONDecoder(aJSONData).unmarshal(this);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public byte[] marshal() throws IOException
	{
		return new BinaryEncoder().marshal(this);
	}


	public R unmarshal(byte[] aBinaryData)
	{
		return unmarshal(new ByteArrayInputStream(aBinaryData), new PathEvaluation());
	}


	public R unmarshal(byte[] aBinaryData, PathEvaluation aPathEvaluation)
	{
		return unmarshal(new ByteArrayInputStream(aBinaryData), aPathEvaluation);
	}


	public R unmarshal(InputStream aBinaryData)
	{
		return unmarshal(aBinaryData, new PathEvaluation());
	}


	public R unmarshal(InputStream aBinaryData, PathEvaluation aPathEvaluation)
	{
		try
		{
			return (R)new BinaryDecoder().unmarshal(aBinaryData, aPathEvaluation, this);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public R unmarshalXML(InputStream aXMLData, boolean aCreateOptionalArrays)
	{
		new XMLDecoder().importXML(aXMLData, this, aCreateOptionalArrays);
		return (R)this;
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
}