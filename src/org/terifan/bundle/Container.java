package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;


public abstract class Container<K, R> implements Serializable, Externalizable
{
	private final static long serialVersionUID = 1L;


	Container()
	{
	}


	public abstract <T> T get(K aKey);


	public <T> T get(K aKey, T aDefaultValue)
	{
		Object value = (Object)get(aKey);
		if (value == null)
		{
			return aDefaultValue;
		}
		return (T)value;
	}


	abstract R set(K aKey, Object aValue);


	public Boolean getBoolean(K aKey)
	{
		return getBoolean(aKey, null);
	}


	public Boolean getBoolean(K aKey, Boolean aDefaultValue)
	{
		Object v = get(aKey);
		if (v == null)
		{
			return aDefaultValue;
		}
		if (v instanceof Boolean)
		{
			return (Boolean)v;
		}
		if (v instanceof String)
		{
			return Boolean.parseBoolean((String)v);
		}
		if (v instanceof Number)
		{
			return ((Number)v).doubleValue() != 0;
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on a Boolean");
	}


	public R putBoolean(K aKey, Boolean aValue)
	{
		set(aKey, aValue);
		return (R)this;
	}


	public Byte getByte(K aKey)
	{
		return getByte(aKey, null);
	}


	public Byte getByte(K aKey, Byte aDefaultValue)
	{
		Object v = get(aKey);
		if (v == null)
		{
			return aDefaultValue;
		}
		if (v instanceof Number)
		{
			return ((Number)v).byteValue();
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on a Byte");
	}


	public Short getShort(K aKey)
	{
		return getShort(aKey, null);
	}


	public Short getShort(K aKey, Short aDefaultValue)
	{
		Object v = get(aKey);
		if (v == null)
		{
			return aDefaultValue;
		}
		if (v instanceof Number)
		{
			return ((Number)v).shortValue();
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on a Short");
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
		if (v instanceof Number)
		{
			return ((Number)v).intValue();
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on an Integer");
	}


	public Long getLong(K aKey)
	{
		return getLong(aKey, null);
	}


	public Long getLong(K aKey, Long aDefaultValue)
	{
		Object v = get(aKey);
		if (v == null)
		{
			return aDefaultValue;
		}
		if (v instanceof Number)
		{
			return ((Number)v).longValue();
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on a Long");
	}


	public Float getFloat(K aKey)
	{
		return getFloat(aKey, null);
	}


	public Float getFloat(K aKey, Float aDefaultValue)
	{
		Object v = get(aKey);
		if (v == null)
		{
			return aDefaultValue;
		}
		if (v instanceof Number)
		{
			return ((Number)v).floatValue();
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on a Float");
	}


	public Double getDouble(K aKey)
	{
		return getDouble(aKey, null);
	}


	public Double getDouble(K aKey, Double aDefaultValue)
	{
		Object v = get(aKey);
		if (v == null)
		{
			return aDefaultValue;
		}
		if (v instanceof Number)
		{
			return ((Number)v).doubleValue();
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on a Double");
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
		set(aKey, aValue);
		return (R)this;
	}


	public Number getNumber(K aKey)
	{
		return (Number)get(aKey);
	}


	public Number getNumber(K aKey, Number aDefautValue)
	{
		Object value = get(aKey);
		if (value == null)
		{
			return aDefautValue;
		}
		if (value instanceof Number)
		{
			return (Number)value;
		}
		throw new IllegalArgumentException("Value of key " + aKey + " cannot be cast on a Number");
	}


	public R putNumber(K aKey, Number aValue)
	{
		set(aKey, aValue);
		return (R)this;
	}


	public Array getArray(K aKey)
	{
		return (Array)get(aKey);
	}


	public R putArray(K aKey, Array aValue)
	{
		set(aKey, aValue);
		return (R)this;
	}


	public Bundle getBundle(K aKey)
	{
		return (Bundle)get(aKey);
	}


	public R putBundle(K aKey, Bundle aValue)
	{
		set(aKey, aValue);
		return (R)this;
	}


	/**
	 * The value referred to by the key is unmarshalled into an object of the type provided.
	 *
	 * @param aType a BundlableValue type
	 * @param aKey a key
	 * @return an instance of the BundlableValue type
	 */
	public <T extends Bundlable> T getBundlable(K aKey, Class<T> aType)
	{
		try
 		{
			Constructor<T> declaredConstructor = aType.getDeclaredConstructor();
			declaredConstructor.setAccessible(true);

			Bundlable instance = declaredConstructor.newInstance();
			instance.readExternal(new BundleInput((Container)get(aKey)));

			return (T)instance;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public <T extends Bundlable> T[] getBundlableArray(K aKey, Class<T> aType)
	{
		Array tmp = getArray(aKey);

		Object array = java.lang.reflect.Array.newInstance(aType, tmp.size());
		for (int i = 0; i < tmp.size(); i++)
		{
			java.lang.reflect.Array.set(array, i, tmp.getBundlable(i, aType));
		}

		return (T[])array;
	}


	public R putBundlable(K aKey, Bundlable aValue)
	{
		BundleOutput out = new BundleOutput();
		aValue.writeExternal(out);
		set(aKey, out.getContainer());

		return (R)this;
	}


	public <T extends Serializable> T getSerializable(K aKey, Class<T> aType)
	{
		byte[] value = getBinary(aKey);

		if (value == null || value.length == 0)
		{
			return null;
		}

		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(value)))
		{
			String typeName = dis.readUTF();
			if (!typeName.equals(aType.getCanonicalName()))
			{
				throw new IllegalArgumentException("Attempt to deserialize wrong object type: expected: " + aType + ", found: " + typeName);
			}

			byte[] buf = new byte[dis.readInt()];
			dis.readFully(buf);

			if (dis.readInt() != new MurmurHash32(buf.length).update(buf).finish())
			{
				throw new IllegalArgumentException("Serialize data checksum error");
			}

			try (ObjectInputStream oos = new ObjectInputStream(new InflaterInputStream(new ByteArrayInputStream(buf))))
			{
				return (T)oos.readObject();
			}
		}
		catch (IOException | ClassNotFoundException e)
		{
			throw new IllegalArgumentException(e);
		}
	}


	public R putSerializable(K aKey, Serializable aValue)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_SPEED))))
		{
			oos.writeObject(aValue);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(e);
		}

		byte[] buf = baos.toByteArray();
		baos.reset();

		try (DataOutputStream dos = new DataOutputStream(baos))
		{
			dos.writeUTF(aValue.getClass().getCanonicalName());
			dos.writeInt(buf.length);
			dos.write(buf);
			dos.writeInt(new MurmurHash32(buf.length).update(buf).finish());
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException(e);
		}

		return putBinary(aKey, baos.toByteArray());
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
		set(aKey, aBytes);
		return (R)this;
	}


	public R put(K aKey, Object aValue)
	{
		if (aValue instanceof Number)
		{
			putNumber(aKey, (Number)aValue);
		}
		else if (aValue instanceof String)
		{
			putString(aKey, (String)aValue);
		}
		else if (aValue instanceof Bundlable)
		{
			putBundlable(aKey, (Bundlable)aValue);
		}
		else if (aValue instanceof Boolean)
		{
			putBoolean(aKey, (Boolean)aValue);
		}
		else if (aValue instanceof byte[])
		{
			putBinary(aKey, (byte[])aValue);
		}
		else if (aValue instanceof Bundle)
		{
			putBundle(aKey, (Bundle)aValue);
		}
		else if (aValue instanceof Array)
		{
			putArray(aKey, (Array)aValue);
		}
		else if (aValue instanceof Serializable)
		{
			putSerializable(aKey, (Serializable)aValue);
		}

		return (R)this;
	}


	public boolean isNull(K aKey)
	{
		return get(aKey) == null;
	}


	public abstract boolean containsKey(K aKey);


	@Override
	public int hashCode()
	{
		return hashCode(new MurmurHash32(0)).finish();
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
	 * @param aJSONOutput bundle JSON is written to this Appendable
	 * @param aCompact if false the JSON produced will be formatted
	 * @return return this Bundle as a JSON
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
		try (Reader r = aJSONData)
		{
			return (R)new JSONDecoder(r).unmarshal(this);
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


	/**
	 * Decode an XML document into a Bundle, ignoring attributes on leaf nodes.
	 */
	public R unmarshalXML(InputStream aXMLData)
	{
		return (R)unmarshalXML(aXMLData, false);
	}


	/**
	 * Decode an XML document into a Bundle.
	 *
	 * @param aAllowAttributesOnLeafs
	 *   if true will ignore attributes on leaf nodes
	 */
	public R unmarshalXML(InputStream aXMLData, boolean aAllowAttributesOnLeafs)
	{
		new XMLDecoder().importXML(aXMLData, this, aAllowAttributesOnLeafs);
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
		aIn.readFully(buf);

		new BinaryDecoder().unmarshal(new ByteArrayInputStream(buf), new PathEvaluation(), this);
	}


	public static boolean isSupportedType(Object aValue)
	{
		Class type = aValue == null ? null : aValue.getClass();

		return type == Boolean.class
			|| type == String.class
			|| type == Byte.class
			|| type == Short.class
			|| type == Integer.class
			|| type == Long.class
			|| type == Float.class
			|| type == Double.class
			|| type == Array.class
			|| type == Bundle.class
			|| type == null;
	}


	public abstract Map<K, Object> toMap();
}
