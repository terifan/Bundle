package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class Bundle implements Cloneable, Externalizable, Iterable<String>
{
	private final static long serialVersionUID = 1L;

	private final Map<String, Object> mValues;
	private final Map<String, Integer> mTypes;


	/**
	 * Create a new empty Bundle.
	 */
	public Bundle()
	{
		mValues = new TreeMap<>();
		mTypes = new HashMap<>();
	}


	/**
	 * Copy the provided Bundle into a new instance.
	 */
	public Bundle(Bundle aOther)
	{
		this();
		putAll(aOther);
	}


	/**
	 * Removes all elements from the mapping of this Bundle.
	 */
	public Bundle clear()
	{
		mValues.clear();
		return this;
	}


	/**
	 * Clones the current Bundle.
	 */
	@Override
	public Bundle clone()
	{
		try
		{
			return (Bundle)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new IllegalStateException(e);
		}
	}


	/**
	 * Returns true if the given key is contained in the mapping of this Bundle.
	 */
	public boolean containsKey(String aKey)
	{
		return mValues.containsKey(aKey);
	}


	/**
	 * Returns an 8 bit value representing the object type as high 4 bits and value type as low 4 bits.
	 */
	int getType(String aKey)
	{
		return mTypes.get(aKey);
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the given key exists.
	 */
	public Object get(String aKey)
	{
		return mValues.get(aKey);
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Bundle getBundle(String aKey)
	{
		return getBundle(aKey, null);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public Bundle getBundle(String aKey, Bundle aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return (Bundle)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Bundle.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Bundle[] getBundleArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (Bundle[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Bundle[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Bundle[][] getBundleMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (Bundle[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Bundle[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Bundle> getBundleArrayList(String aKey)
	{
		return getBundleArrayList(aKey, (ArrayList<Bundle>)null);
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Bundle> getBundleArrayList(String aKey, ArrayList<Bundle> aDefault)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefault;
		}
		try
		{
			return (ArrayList<Bundle>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	public ArrayList<Bundle> getBundleArrayList(String aKey, BundleProcessor aProcessor)
	{
		ArrayList<Bundle> list = getBundleArrayList(aKey, new ArrayList<>());
		for (Bundle item : list)
		{
			aProcessor.process(item);
		}
		return list;
	}


	public Bundlable getBundlable(String aKey, Bundlable aInstance) throws IOException
	{
		Bundle bundle = getBundle(aKey);

		if (bundle != null)
		{
			aInstance.readExternal(bundle);
		}

		return aInstance;
	}


	public <T extends Bundlable> T getBundlable(String aKey, Class<T> aType) throws IOException
	{
		try
		{
			Bundle bundle = getBundle(aKey);

			if (bundle == null)
			{
				return null;
			}

			T instance = aType.newInstance();

			instance.readExternal(bundle);

			return (T)instance;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new IOException(e);
		}
	}


	public <T extends Bundlable> T[] getBundlableArray(String aKey, Class<T> aType) throws IOException
	{
		try
		{
			Bundle[] bundles = getBundleArray(aKey);

			if (bundles == null)
			{
				return null;
			}

			Object items = Array.newInstance(aType, bundles.length);

			for (int i = 0; i < bundles.length; i++)
			{
				T instance = aType.newInstance();

				instance.readExternal(bundles[i]);

				Array.set(items, i, (T)instance);
			}

			return (T[])items;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new IOException(e);
		}
	}


	public <T extends Bundlable> T[][] getBundlableMatrix(String aKey, Class<T> aType) throws IOException
	{
		try
		{
			Bundle[][] bundles = getBundleMatrix(aKey);

			if (bundles == null)
			{
				return null;
			}

			Object items = Array.newInstance(aType, bundles.length);

			for (int i = 0; i < bundles.length; i++)
			{
				Object items2 = Array.newInstance(aType, bundles[i].length);

				for (int j = 0; j < bundles[i].length; j++)
				{
					T instance = aType.newInstance();

					instance.readExternal(bundles[i][j]);

					Array.set(items2, j, (T)instance);
				}

				Array.set(items, i, items2);
			}

			return (T[][])items;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new IOException(e);
		}
	}


	public <T extends Bundlable> T[] getBundlableArray(String aKey, Class<T> aType, BundlableObjectFactory<T> aFactory) throws IOException
	{
		Bundle[] bundles = getBundleArray(aKey);

		if (bundles == null)
		{
			return null;
		}

		Object items = Array.newInstance(aType, bundles.length);

		for (int i = 0; i < bundles.length; i++)
		{
			T instance = aFactory.newInstance();

			instance.readExternal(bundles[i]);

			Array.set(items, i, (T)instance);
		}

		return (T[])items;
	}


	public <T extends Bundlable> T[][] getBundlableMatrix(String aKey, Class<T> aType, BundlableObjectFactory<T> aFactory) throws IOException
	{
		Bundle[][] bundles = getBundleMatrix(aKey);

		if (bundles == null)
		{
			return null;
		}

		Object items = Array.newInstance(aType, bundles.length);

		for (int i = 0; i < bundles.length; i++)
		{
			Object items2 = Array.newInstance(aType, bundles[i].length);

			for (int j = 0; j < bundles.length; j++)
			{
				T instance = aFactory.newInstance();

				instance.readExternal(bundles[i][j]);

				Array.set(items2, j, (T)instance);
			}

			Array.set(items, i, items2);
		}

		return (T[][])items;
	}


	public <T extends Bundlable> ArrayList<T> getBundlableArrayList(String aKey, Class<T> aType) throws IOException
	{
		try
		{
			ArrayList<Bundle> bundles = getBundleArrayList(aKey);

			ArrayList<T> items = new ArrayList<>();

			if (bundles == null)
			{
				return items;
			}

			for (Bundle bundle : bundles)
			{
				T instance = aType.newInstance();

				instance.readExternal(bundle);

				items.add((T)instance);
			}

			return items;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new IOException(e);
		}
	}


	public <T extends Bundlable> ArrayList<T> getBundlableArrayList(String aKey, BundlableObjectFactory<T> aFactory) throws IOException
	{
		ArrayList<Bundle> bundles = getBundleArrayList(aKey);

		ArrayList<T> items = new ArrayList<>();

		if (bundles == null)
		{
			return items;
		}

		for (Bundle bundle : bundles)
		{
			T instance = aFactory.newInstance();

			instance.readExternal(bundle);

			items.add((T)instance);
		}

		return items;
	}


	/**
	 * Returns the value associated with the given key, or false if no mapping of the desired type exists for the given key.
	 */
	public boolean getBoolean(String aKey)
	{
		return getBoolean(aKey, false);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public boolean getBoolean(String aKey, boolean aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			if (o instanceof Boolean)
			{
				return (Boolean)o;
			}
			return ((Number)o).longValue() != 0;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Boolean.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public boolean[] getBooleanArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (boolean[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, boolean[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public boolean[][] getBooleanMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (boolean[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, boolean[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Boolean> getBooleanArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Boolean>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or 0 if no mapping of the desired type exists for the given key.
	 */
	public byte getByte(String aKey)
	{
		return getByte(aKey, (byte)0);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public byte getByte(String aKey, byte aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return ((Number)o).byteValue();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Byte.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public byte[] getByteArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (byte[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, byte[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public byte[][] getByteMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (byte[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, byte[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Byte> getByteArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Byte>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or 0 if no mapping of the desired type exists for the given key.
	 */
//	public char getChar(String aKey)
//	{
//		return getChar(aKey, (char)0);
//	}
//
//
//	/**
//	 * Returns the value associated with the given key, or 0 if no mapping of the desired type exists for the given key.
//	 */
//	public char getChar(String aKey, char aDefaultValue)
//	{
//		Object o = mValues.get(aKey);
//		if (o == null)
//		{
//			return aDefaultValue;
//		}
//		try
//		{
//			return (char)((Number)o).shortValue();
//		}
//		catch (ClassCastException e)
//		{
//			return typeWarning(aKey, o, Character.class, aDefaultValue, e);
//		}
//	}
//
//
//	/**
//	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
//	 * is explicitly associated with the key.
//	 */
//	public char[] getCharArray(String aKey)
//	{
//		Object o = mValues.get(aKey);
//		try
//		{
//			return (char[])o;
//		}
//		catch (ClassCastException e)
//		{
//			return typeWarning(aKey, o, char[].class, null, e);
//		}
//	}
//
//
//	/**
//	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
//	 * is explicitly associated with the key.
//	 */
//	public char[][] getCharMatrix(String aKey)
//	{
//		Object o = mValues.get(aKey);
//		try
//		{
//			return (char[][])o;
//		}
//		catch (ClassCastException e)
//		{
//			return typeWarning(aKey, o, char[][].class, null, e);
//		}
//	}
//
//
//	/**
//	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
//	 * is explicitly associated with the key.
//	 */
//	public ArrayList<Character> getCharArrayList(String aKey)
//	{
//		Object o = mValues.get(aKey);
//		try
//		{
//			return (ArrayList<Character>)o;
//		}
//		catch (ClassCastException e)
//		{
//			return typeWarning(aKey, o, ArrayList.class, null, e);
//		}
//	}


	/**
	 * Returns the value associated with the given key, or 0.0 if no mapping of the desired type exists for the given key.
	 */
	public double getDouble(String aKey)
	{
		return getDouble(aKey, 0.0);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public double getDouble(String aKey, double aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return ((Number)o).doubleValue();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Double.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public double[] getDoubleArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (double[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, double[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public double[][] getDoubleMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (double[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, double[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Double> getDoubleArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Double>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or 0.0f if no mapping of the desired type exists for the given key.
	 */
	public float getFloat(String aKey)
	{
		return getFloat(aKey, 0f);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public float getFloat(String aKey, float aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return ((Number)o).floatValue();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Float.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public float[] getFloatArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (float[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, float[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public float[][] getFloatMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (float[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, float[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Float> getFloatArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Float>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public int getInt(String aKey, int aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return ((Number)o).intValue();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Integer.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or 0 if no mapping of the desired type exists for the given key.
	 */
	public int getInt(String aKey)
	{
		return getInt(aKey, 0);
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public int[] getIntArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (int[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, int[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public int[][] getIntMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (int[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, int[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Integer> getIntArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Integer>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or 0 if no mapping of the desired type exists for the given key.
	 */
	public long getLong(String aKey)
	{
		return getLong(aKey, 0L);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public long getLong(String aKey, long aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return ((Number)o).longValue();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Long.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public long[] getLongArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (long[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, long[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public long[][] getLongMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (long[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, long[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Long> getLongArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Long>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or 0 if no mapping of the desired type exists for the given key.
	 */
	public short getShort(String aKey)
	{
		return getShort(aKey, (short)0);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public short getShort(String aKey, short aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return ((Number)o).shortValue();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Short.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public short[] getShortArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (short[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, short[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public short[][] getShortMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (short[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, short[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Short> getShortArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Short>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public String getString(String aKey)
	{
		return getString(aKey, null);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public String getString(String aKey, String aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return o.toString();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, String.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public String[] getStringArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (String[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, String[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public String[][] getStringMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (String[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, String[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<String> getStringArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<String>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Serializable getSerializable(String aKey)
	{
		return getSerializable(aKey, null);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public Serializable getSerializable(String aKey, Serializable aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return (Serializable)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Serializable.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Serializable[] getSerializableArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (Serializable[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Serializable[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Serializable[][] getSerializableMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (Serializable[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Serializable[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Serializable> getSerializableArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Serializable>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Date getDate(String aKey)
	{
		return getDate(aKey, null);
	}


	/**
	 * Returns the value associated with the given key, or aDefaultValue if no mapping of the desired type exists for the given key.
	 */
	public Date getDate(String aKey, Date aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			if (o instanceof Date)
			{
				if (o instanceof Long)
				{
					return new Date((Long)o);
				}
				if (o instanceof String)
				{
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse((String)o);
				}
			}
			return (Date)o;
		}
		catch (ClassCastException | ParseException e)
		{
			return typeWarning(aKey, o, Date.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Date[] getDateArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (Date[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Date[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public Date[][] getDateMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (Date[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Date[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Date> getDateArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Date>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


	/**
	 * Returns true if the mapping of this Bundle is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return mValues.isEmpty();
	}


	/**
	 * Returns true if the mapping of this key is null, false otherwise.
	 */
	public boolean isNull(String aKey)
	{
		return mValues.get(aKey) == null;
	}


	/**
	 * Returns a Set containing the Strings used as keys in this Bundle.
	 */
	public Set<String> keySet()
	{
		return mValues.keySet();
	}


	/**
	 * Inserts all mappings from the given Bundle into this Bundle.
	 */
	public Bundle putAll(Bundle aOther)
	{
		for (String key : aOther)
		{
			mValues.put(key, aOther.mValues.get(key));
			mTypes.put(key, aOther.mTypes.get(key));
		}
		return this;
	}


	/**
	 * Inserts a Bundle value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBundle(String aKey, Bundle aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.BUNDLE);
		return this;
	}


	public Bundle putBundleArray(String aKey, Bundle... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.BUNDLE);
		return this;
	}


	public Bundle putBundleMatrix(String aKey, Bundle[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.BUNDLE);
		return this;
	}


	public Bundle putBundleArrayList(String aKey, ArrayList<Bundle> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.BUNDLE);
		return this;
	}


	public Bundle putBundlable(String aKey, Bundlable aValue) throws IOException
	{
		Bundle bundle;
		if (aValue == null)
		{
			bundle = null;
		}
		else
		{
			bundle = new Bundle();
			aValue.writeExternal(bundle);
		}
		put(aKey, bundle, FieldType.VALUE, FieldType.BUNDLE);
		return this;
	}


	public Bundle putBundlableArray(String aKey, Bundlable... aValues) throws IOException
	{
		Bundle[] bundles;

		if (aValues == null)
		{
			bundles = null;
		}
		else
		{
			bundles = new Bundle[aValues.length];
			for (int i = 0; i < aValues.length; i++)
			{
				bundles[i] = new Bundle();
				aValues[i].writeExternal(bundles[i]);
			}
		}
		put(aKey, bundles, FieldType.ARRAY, FieldType.BUNDLE);
		return this;
	}


	public Bundle putBundlableMatrix(String aKey, Bundlable[][] aValues) throws IOException
	{
		Bundle[][] bundles;

		if (aValues == null)
		{
			bundles = null;
		}
		else
		{
			bundles = new Bundle[aValues.length][];
			for (int i = 0; i < aValues.length; i++)
			{
				if (aValues[i] != null)
				{
					bundles[i] = new Bundle[aValues[i].length];
					for (int j = 0; j < aValues[i].length; j++)
					{
						bundles[i][j] = new Bundle();
						aValues[i][j].writeExternal(bundles[i][j]);
					}
				}
			}
		}
		put(aKey, bundles, FieldType.MATRIX, FieldType.BUNDLE);
		return this;
	}


	public Bundle putBundlableArrayList(String aKey, ArrayList<? extends Bundlable> aValues) throws IOException
	{
		ArrayList<Bundle> bundles;

		if (aValues == null)
		{
			bundles = null;
		}
		else
		{
			bundles = new ArrayList<>(aValues.size());
			for (Bundlable value : aValues)
			{
				Bundle bundle = new Bundle();
				value.writeExternal(bundle);
				bundles.add(bundle);
			}
		}
		put(aKey, bundles, FieldType.ARRAYLIST, FieldType.BUNDLE);
		return this;
	}


	/**
	 * Inserts a Boolean value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBoolean(String aKey, boolean aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.BOOLEAN);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanArray(String aKey, boolean... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.BOOLEAN);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanMatrix(String aKey, boolean[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.BOOLEAN);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanArrayList(String aKey, ArrayList<Boolean> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.BOOLEAN);
		return this;
	}


	/**
	 * Inserts a byte value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByte(String aKey, int aValue)
	{
		return putByte(aKey, (byte)aValue);
	}


	/**
	 * Inserts a byte value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByte(String aKey, byte aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.BYTE);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteArray(String aKey, byte... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.BYTE);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteMatrix(String aKey, byte[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.BYTE);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteArrayList(String aKey, ArrayList<Byte> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.BYTE);
		return this;
	}


	/**
	 * Inserts a char value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putChar(String aKey, char aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.CHAR);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharArray(String aKey, char... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.CHAR);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharMatrix(String aKey, char[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.CHAR);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharArrayList(String aKey, ArrayList<Character> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.CHAR);
		return this;
	}


	/**
	 * Inserts a double value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDouble(String aKey, double aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.DOUBLE);
		return this;
	}


	/**
	 * Inserts a double array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleArray(String aKey, double... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.DOUBLE);
		return this;
	}


	/**
	 * Inserts a double array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleMatrix(String aKey, double[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.DOUBLE);
		return this;
	}


	/**
	 * Inserts a double list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleArrayList(String aKey, ArrayList<Double> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.DOUBLE);
		return this;
	}


	/**
	 * Inserts a float value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloat(String aKey, float aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.FLOAT);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatArray(String aKey, float... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.FLOAT);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatMatrix(String aKey, float[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.FLOAT);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatArrayList(String aKey, ArrayList<Float> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.FLOAT);
		return this;
	}


	/**
	 * Inserts an int value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putInt(String aKey, int aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.INT);
		return this;
	}


	/**
	 * Inserts an int array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntArray(String aKey, int... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.INT);
		return this;
	}


	/**
	 * Inserts an int array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntMatrix(String aKey, int[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.INT);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntArrayList(String aKey, ArrayList<Integer> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.INT);
		return this;
	}


	/**
	 * Inserts a long value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLong(String aKey, long aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.LONG);
		return this;
	}


	/**
	 * Inserts a long array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongArray(String aKey, long... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.LONG);
		return this;
	}


	/**
	 * Inserts a long array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongMatrix(String aKey, long[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.LONG);
		return this;
	}


	/**
	 * Inserts a long list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongArrayList(String aKey, ArrayList<Long> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.LONG);
		return this;
	}


	/**
	 * Inserts a short value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShort(String aKey, int aValue)
	{
		return putShort(aKey, (short)aValue);
	}


	/**
	 * Inserts a short value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShort(String aKey, short aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.SHORT);
		return this;
	}


	/**
	 * Inserts a short array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArray(String aKey, short... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.SHORT);
		return this;
	}


	/**
	 * Inserts a short array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortMatrix(String aKey, short[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.SHORT);
		return this;
	}


	/**
	 * Inserts a short list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArrayList(String aKey, ArrayList<Short> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.SHORT);
		return this;
	}


	/**
	 * Inserts a String value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putString(String aKey, String aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.STRING);
		return this;
	}


	/**
	 * Inserts a String array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringArray(String aKey, String... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.STRING);
		return this;
	}


	/**
	 * Inserts a String array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringMatrix(String aKey, String[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.STRING);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringArrayList(String aKey, ArrayList<String> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.STRING);
		return this;
	}


	/**
	 * Inserts a Serializable value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putSerializable(String aKey, Serializable aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.SERIALIZABLE);
		return this;
	}


	/**
	 * Inserts a Serializable array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putSerializableArray(String aKey, Serializable... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.SERIALIZABLE);
		return this;
	}


	/**
	 * Inserts a Serializable array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putSerializableMatrix(String aKey, Serializable[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.SERIALIZABLE);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putSerializableArrayList(String aKey, ArrayList<Serializable> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.SERIALIZABLE);
		return this;
	}


	/**
	 * Inserts a Date value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDate(String aKey, Date aValue)
	{
		put(aKey, aValue, FieldType.VALUE, FieldType.DATE);
		return this;
	}


	/**
	 * Inserts a Date array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateArray(String aKey, Date... aValue)
	{
		put(aKey, aValue, FieldType.ARRAY, FieldType.DATE);
		return this;
	}


	/**
	 * Inserts a Date array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateMatrix(String aKey, Date[][] aValue)
	{
		put(aKey, aValue, FieldType.MATRIX, FieldType.DATE);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateArrayList(String aKey, ArrayList<Date> aValue)
	{
		put(aKey, aValue, FieldType.ARRAYLIST, FieldType.DATE);
		return this;
	}


	/**
	 * Removes any entry with the given key from the mapping of this Bundle.
	 */
	public Bundle remove(String aKey)
	{
		mValues.remove(aKey);
		return this;
	}


	/**
	 * Returns the number of mappings contained in this Bundle.
	 */
	public int size()
	{
		return mValues.size();
	}


	/**
	 * Returns a description of this bundle.
	 */
	@Override
	public String toString()
	{
		try
		{
			return marshalPSON(true);
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}


	/**
	 * Returns true if the provided object is a Bundle with the same keys and values as this.
	 */
	@Override
	public boolean equals(Object aOther)
	{
		if (aOther == this)
		{
			return true;
		}
		if (aOther instanceof Bundle)
		{
			Bundle o = (Bundle)aOther;

			for (String key : keySet())
			{
				Object tv = mValues.get(key);
				Object ov = o.mValues.get(key);

				if (tv != ov)
				{
//					if (tv == null && List.class.isAssignableFrom(ov.getClass()) && ((List)ov).isEmpty())
//					{
//						continue;
//					}
//					if (ov == null && List.class.isAssignableFrom(tv.getClass()) && ((List)tv).isEmpty())
//					{
//						continue;
//					}
					if (tv == null || ov == null)
					{
						return false;
					}

					if (tv.getClass().isArray())
					{
						int tl = Array.getLength(tv);
						int ol = Array.getLength(ov);

						if (tl != ol)
						{
							return false;
						}
						for (int i = 0; i < tl; i++)
						{
							Object tav = Array.get(tv, i);
							Object oav = Array.get(ov, i);
							if (tav != oav && (tav == null || !tav.equals(oav)))
							{
								return false;
							}
						}
					}
					else if (!tv.equals(ov))
					{
						return false;
					}
				}
			}

			return true;
		}

		return false;
	}


	/**
	 * Returns a hashcode of this Bundle.
	 */
	@Override
	public int hashCode()
	{
		return mValues.hashCode();
	}


	@Override
	public Iterator<String> iterator()
	{
		return keySet().iterator();
	}


	private void put(String aKey, Object aValue, int aCollectionType, int aValueType)
	{
		put(aKey, aValue, FieldType.encode(aCollectionType, aValueType));
	}


	void put(String aKey, Object aValue, int aFieldType)
	{
		if (aKey == null)
		{
			throw new IllegalArgumentException("Provided key is null.");
		}

		mValues.put(aKey, aValue);
		mTypes.put(aKey, aFieldType);
	}


	/**
	 * Override this method to handle type casting errors.
	 *
	 * @return
	 *    default implementation return the default value provided
	 */
	protected <E> E typeWarning(String aKey, Object aValue, Class<E> aExpectedType, E aDefaultValue, Exception aException)
	{
		System.err.printf("Attempt to cast generated internal exception: Key '%s' expected %s but value was a %s. The default value '%s' was returned.\n", aKey, aExpectedType.getSimpleName(), aValue == null ? null : aValue.getClass().getSimpleName(), aDefaultValue);

		return aDefaultValue;
	}


	@Override
	public void readExternal(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		unmarshal(new InputStream()
		{
			@Override
			public int read() throws IOException
			{
				return aIn.read();
			}
			@Override
			public int read(byte[] aBuffer, int aOffset, int aLength) throws IOException
			{
				return aIn.read(aBuffer, aOffset, aLength);
			}
		});
	}


	@Override
	public void writeExternal(ObjectOutput aOut) throws IOException
	{
		marshal(new OutputStream()
		{
			@Override
			public void write(int aByte) throws IOException
			{
				aOut.write(aByte);
			}
			@Override
			public void write(byte[] aBuffer, int aOffset, int aLength) throws IOException
			{
				aOut.write(aBuffer, aOffset, aLength);
			}
		});
	}


	public byte[] marshal() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
		new BinaryEncoder().marshal(this, baos);
		return baos.toByteArray();
	}


	public Bundle marshal(OutputStream aOutputStream) throws IOException
	{
		new BinaryEncoder().marshal(this, aOutputStream);
		return this;
	}


	public Bundle marshal(ByteBuffer aByteBuffer) throws IOException
	{
		new BinaryEncoder().marshal(this, new ByteBufferOutputStream(aByteBuffer));
		return this;
	}


	public String marshalPSON() throws IOException
	{
		return marshalPSON(false);
	}


	public String marshalPSON(boolean aFlat) throws IOException
	{
		StringBuilder sb = new StringBuilder(4096);
		new PSONEncoder().marshal(this, sb, aFlat);
		return sb.toString();
	}


	public Bundle marshalPSON(OutputStream aOutputStream) throws IOException
	{
		return marshalPSON(aOutputStream, false);
	}


	public Bundle marshalPSON(OutputStream aOutputStream, boolean aFlat) throws IOException
	{
		PrintWriter out = new PrintWriter(aOutputStream);
		new PSONEncoder().marshal(this, out, aFlat);
		out.flush();
		return this;
	}


	public Bundle unmarshal(byte[] aData) throws IOException
	{
		new BinaryDecoder().unmarshal(this, new ByteArrayInputStream(aData));
		return this;
	}


	public Bundle unmarshal(InputStream aInputStream) throws IOException
	{
		new BinaryDecoder().unmarshal(this, aInputStream);
		return this;
	}


	public Bundle unmarshal(ByteBuffer aByteBuffer) throws IOException
	{
		new BinaryDecoder().unmarshal(this, new ByteBufferInputStream(aByteBuffer));
		return this;
	}


	public Bundle unmarshalPSON(String aPSON) throws IOException
	{
		new PSONDecoder().unmarshal(new StringReader(aPSON), this);
		return this;
	}


	public Bundle unmarshalPSON(InputStream aInputStream) throws IOException
	{
		new PSONDecoder().unmarshal(new InputStreamReader(aInputStream), this);
		return this;
	}


	public void visit(BundleVisitor aVisitor)
	{
		visit(Integer.MAX_VALUE, false, null, aVisitor);
	}


	/**
	 * Recursively visit the values of this Bundle.
	 *
	 * @param aMaxDepth
	 *   the max depth, level 0 include only the immediate children of this Bundle
	 * @param aVisitValues
	 *   true if each value in array, arraylists and matricies should invoke the process method of the visitor.
	 * @param aAbortCondition
	 *   null or an AtomicBoolean, recursion will stop when this is true
	 * @param aVisitor
	 *   the visitor callback
	 */
	public void visit(int aMaxDepth, boolean aVisitValues, AtomicBoolean aAbortCondition, BundleVisitor aVisitor)
	{
		if (aMaxDepth < 0 || aAbortCondition != null && aAbortCondition.get())
		{
			return;
		}

		aMaxDepth--;

		for (Entry<String,Object> entry : mValues.entrySet())
		{
			String key = entry.getKey();
			Integer fieldType = mTypes.get(key);
			Object value = entry.getValue();
			int collectionTypeOf = FieldType.collectionTypeOf(fieldType);

			if (FieldType.valueTypeOf(fieldType) == FieldType.BUNDLE)
			{
				if (aVisitValues)
				{
					aVisitor.process(this, key, value);
				}

				switch (collectionTypeOf)
				{
					case FieldType.ARRAY:
						int len = Array.getLength(value);
						for (int i = 0; i < len; i++)
						{
							Bundle childBundle = (Bundle)Array.get(value, i);
							visitChildBundle(aVisitor, entry, childBundle, aMaxDepth, aAbortCondition);
						}
						break;
					case FieldType.ARRAYLIST:
						for (Bundle childBundle : (ArrayList<Bundle>)value)
						{
							visitChildBundle(aVisitor, entry, childBundle, aMaxDepth, aAbortCondition);
						}
						break;
					case FieldType.MATRIX:
						int rows = Array.getLength(value);
						for (int i = 0; i < rows; i++)
						{
							Object v = Array.get(value, i);
							int cols = Array.getLength(v);
							for (int j = 0; j < cols; j++)
							{
								Bundle childBundle = (Bundle)Array.get(v, j);
								visitChildBundle(aVisitor, entry, childBundle, aMaxDepth, aAbortCondition);
							}
						}
						break;
					default:
						Bundle childBundle = (Bundle)entry.getValue();
						visitChildBundle(aVisitor, entry, childBundle, aMaxDepth, aAbortCondition);
						break;
				}
			}
			else
			{
				if (!aVisitValues)
				{
					aVisitor.process(this, key, value);
				}
				else
				{
					switch (collectionTypeOf)
					{
						case FieldType.ARRAY:
							int len = Array.getLength(value);
							for (int i = 0; i < len; i++)
							{
								Object newValue = aVisitor.process(this, key, Array.get(value, i));
								Array.set(value, i, newValue);
							}
							break;
						case FieldType.ARRAYLIST:
						{
							ArrayList list = (ArrayList)value;
							for (int i = 0; i < list.size(); i++)
							{
								Object newValue = aVisitor.process(this, key, list.get(i));
								list.set(i, newValue);
							}
							break;
						}
						case FieldType.MATRIX:
							int rows = Array.getLength(value);
							for (int i = 0; i < rows; i++)
							{
								Object v = Array.get(value, i);
								int cols = Array.getLength(value);
								for (int j = 0; j < cols; j++)
								{
									Object newValue = aVisitor.process(this, key, Array.get(v, j));
									Array.set(v, j, newValue);
								}
							}
							break;
						default:
							Object newValue = aVisitor.process(this, key, value);
							put(key, newValue, FieldType.classify(newValue));
							break;
					}
				}
			}
		}
	}


	protected void visitChildBundle(BundleVisitor aVisitor, Entry<String, Object> aEntry, Bundle aChildBundle, int aMaxDepth, AtomicBoolean aAbortCondition)
	{
		aVisitor.entering(this, aEntry.getKey(), aChildBundle);

		aChildBundle.visit(aMaxDepth, false, aAbortCondition, aVisitor);

		aVisitor.leaving(this, aEntry.getKey(), aChildBundle);
	}
}