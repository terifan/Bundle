package org.terifan.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Bundle<T extends Bundle> implements Cloneable, Externalizable, Iterable<String>
{
	private final static long serialVersionUID = 1L;

	private final Map<String, Object> mValues;
	private final Map<String, FieldType2> mTypes;


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
	public Object clone() throws CloneNotSupportedException
	{
		try
		{
			Bundle b = (Bundle)super.clone();
			b.putAll(this);
			return b;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
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
	 * Returns the value type associated with the key.
	 */
	public FieldType2 getType(String aKey)
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
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Bundle>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
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

			if (bundles == null)
			{
				return null;
			}

			ArrayList<T> items = new ArrayList<>();

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

		if (bundles == null)
		{
			return null;
		}

		ArrayList<T> items = new ArrayList<>();

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
	public char getChar(String aKey)
	{
		return getChar(aKey, (char)0);
	}


	/**
	 * Returns the value associated with the given key, or 0 if no mapping of the desired type exists for the given key.
	 */
	public char getChar(String aKey, char aDefaultValue)
	{
		Object o = mValues.get(aKey);
		if (o == null)
		{
			return aDefaultValue;
		}
		try
		{
			return (char)((Number)o).shortValue();
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, Character.class, aDefaultValue, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public char[] getCharArray(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (char[])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, char[].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public char[][] getCharMatrix(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (char[][])o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, char[][].class, null, e);
		}
	}


	/**
	 * Returns the value associated with the given key, or null if no mapping of the desired type exists for the given key or a null value
	 * is explicitly associated with the key.
	 */
	public ArrayList<Character> getCharArrayList(String aKey)
	{
		Object o = mValues.get(aKey);
		try
		{
			return (ArrayList<Character>)o;
		}
		catch (ClassCastException e)
		{
			return typeWarning(aKey, o, ArrayList.class, null, e);
		}
	}


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
	public Bundle putAll(Bundle<T> aOther)
	{
		for (String key : aOther)
		{
			put(key, aOther.mValues.get(key), aOther.mTypes.get(key));
		}
		return this;
	}


	/**
	 * Inserts a Bundle value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBundle(String aKey, Bundle aValue)
	{
		put(aKey, aValue, FieldType2.BUNDLE);
		return this;
	}


	public Bundle putBundleArray(String aKey, Bundle... aValue)
	{
		put(aKey, aValue, FieldType2.BUNDLE_ARRAY);
		return this;
	}


	public Bundle putBundleMatrix(String aKey, Bundle[][] aValue)
	{
		put(aKey, aValue, FieldType2.BUNDLE_MATRIX);
		return this;
	}


	public Bundle putBundleArrayList(String aKey, ArrayList<Bundle> aValue)
	{
		put(aKey, aValue, FieldType2.BUNDLE_ARRAYLIST);
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
		put(aKey, bundle, FieldType2.BUNDLE);
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
		put(aKey, bundles, FieldType2.BUNDLE_ARRAY);
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
		put(aKey, bundles, FieldType2.BUNDLE_MATRIX);
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
		put(aKey, bundles, FieldType2.BUNDLE_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts a Boolean value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBoolean(String aKey, boolean aValue)
	{
		put(aKey, aValue, FieldType2.BOOLEAN);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanArray(String aKey, boolean... aValue)
	{
		put(aKey, aValue, FieldType2.BOOLEAN_ARRAY);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanMatrix(String aKey, boolean[][] aValue)
	{
		put(aKey, aValue, FieldType2.BOOLEAN_MATRIX);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanArrayList(String aKey, ArrayList<Boolean> aValue)
	{
		put(aKey, aValue, FieldType2.BOOLEAN_ARRAYLIST);
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
		put(aKey, aValue, FieldType2.BYTE);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteArray(String aKey, byte... aValue)
	{
		put(aKey, aValue, FieldType2.BYTE_ARRAY);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteMatrix(String aKey, byte[][] aValue)
	{
		put(aKey, aValue, FieldType2.BYTE_MATRIX);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteArrayList(String aKey, ArrayList<Byte> aValue)
	{
		put(aKey, aValue, FieldType2.BYTE_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts a char value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putChar(String aKey, char aValue)
	{
		put(aKey, aValue, FieldType2.CHAR);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharArray(String aKey, char... aValue)
	{
		put(aKey, aValue, FieldType2.CHAR_ARRAY);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharMatrix(String aKey, char[][] aValue)
	{
		put(aKey, aValue, FieldType2.CHAR_MATRIX);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharArrayList(String aKey, ArrayList<Character> aValue)
	{
		put(aKey, aValue, FieldType2.CHAR_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts a double value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public T putDouble(String aKey, double aValue)
	{
		put(aKey, aValue, FieldType2.DOUBLE);
		return (T)this;
	}


	/**
	 * Inserts a double array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleArray(String aKey, double... aValue)
	{
		put(aKey, aValue, FieldType2.DOUBLE_ARRAY);
		return this;
	}


	/**
	 * Inserts a double array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleMatrix(String aKey, double[][] aValue)
	{
		put(aKey, aValue, FieldType2.DOUBLE_MATRIX);
		return this;
	}


	/**
	 * Inserts a double list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleArrayList(String aKey, ArrayList<Double> aValue)
	{
		put(aKey, aValue, FieldType2.DOUBLE_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts a float value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloat(String aKey, float aValue)
	{
		put(aKey, aValue, FieldType2.FLOAT);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatArray(String aKey, float... aValue)
	{
		put(aKey, aValue, FieldType2.FLOAT_ARRAY);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatMatrix(String aKey, float[][] aValue)
	{
		put(aKey, aValue, FieldType2.FLOAT_MATRIX);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatArrayList(String aKey, ArrayList<Float> aValue)
	{
		put(aKey, aValue, FieldType2.FLOAT_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts an int value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putInt(String aKey, int aValue)
	{
		put(aKey, aValue, FieldType2.INT);
		return this;
	}


	/**
	 * Inserts an int array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntArray(String aKey, int... aValue)
	{
		put(aKey, aValue, FieldType2.INT_ARRAY);
		return this;
	}


	/**
	 * Inserts an int array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntMatrix(String aKey, int[][] aValue)
	{
		put(aKey, aValue, FieldType2.INT_MATRIX);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntArrayList(String aKey, ArrayList<Integer> aValue)
	{
		put(aKey, aValue, FieldType2.INT_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts a long value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLong(String aKey, long aValue)
	{
		put(aKey, aValue, FieldType2.LONG);
		return this;
	}


	/**
	 * Inserts a long array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongArray(String aKey, long... aValue)
	{
		put(aKey, aValue, FieldType2.LONG_ARRAY);
		return this;
	}


	/**
	 * Inserts a long array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongMatrix(String aKey, long[][] aValue)
	{
		put(aKey, aValue, FieldType2.LONG_MATRIX);
		return this;
	}


	/**
	 * Inserts a long list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongArrayList(String aKey, ArrayList<Long> aValue)
	{
		put(aKey, aValue, FieldType2.LONG_ARRAYLIST);
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
		put(aKey, aValue, FieldType2.SHORT);
		return this;
	}


	/**
	 * Inserts a short array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArray(String aKey, short... aValue)
	{
		put(aKey, aValue, FieldType2.SHORT_ARRAY);
		return this;
	}


	/**
	 * Inserts a short array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArray(String aKey, short[][] aValue)
	{
		put(aKey, aValue, FieldType2.SHORT_ARRAY);
		return this;
	}


	/**
	 * Inserts a short list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArrayList(String aKey, ArrayList<Short> aValue)
	{
		put(aKey, aValue, FieldType2.SHORT_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts a String value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public T putString(String aKey, String aValue)
	{
		put(aKey, aValue, FieldType2.STRING);
		return (T)this;
	}


	/**
	 * Inserts a String array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringArray(String aKey, String... aValue)
	{
		put(aKey, aValue, FieldType2.STRING_ARRAY);
		return this;
	}


	/**
	 * Inserts a String array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringMatrix(String aKey, String[][] aValue)
	{
		put(aKey, aValue, FieldType2.STRING_MATRIX);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringArrayList(String aKey, ArrayList<String> aValue)
	{
		put(aKey, aValue, FieldType2.STRING_ARRAYLIST);
		return this;
	}


	/**
	 * Inserts a Date value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDate(String aKey, Date aValue)
	{
		put(aKey, aValue, FieldType2.DATE);
		return this;
	}


	/**
	 * Inserts a Date array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateArray(String aKey, Date... aValue)
	{
		put(aKey, aValue, FieldType2.DATE_ARRAY);
		return this;
	}


	/**
	 * Inserts a Date array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateMatrix(String aKey, Date[][] aValue)
	{
		put(aKey, aValue, FieldType2.DATE_MATRIX);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateArrayList(String aKey, ArrayList<Date> aValue)
	{
		put(aKey, aValue, FieldType2.DATE_ARRAYLIST);
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
		return mValues.toString();
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


	@Deprecated
	void put(String aKey, Object aValue)
	{
		FieldType2 type = null;

		if (aValue == null) type = FieldType2.STRING;
		else if (aValue instanceof Boolean) type = FieldType2.BOOLEAN;
		else if (aValue instanceof Byte) type = FieldType2.BYTE;
		else if (aValue instanceof Short) type = FieldType2.SHORT;
		else if (aValue instanceof Character) type = FieldType2.CHAR;
		else if (aValue instanceof Integer) type = FieldType2.INT;
		else if (aValue instanceof Long) type = FieldType2.LONG;
		else if (aValue instanceof Float) type = FieldType2.FLOAT;
		else if (aValue instanceof Double) type = FieldType2.DOUBLE;
		else if (aValue instanceof String) type = FieldType2.STRING;
		else if (aValue instanceof Date) type = FieldType2.DATE;
		else if (aValue instanceof Bundle) type = FieldType2.BUNDLE;
		else if (aValue.getClass().isArray() && aValue.getClass().getComponentType().isArray())
		{
			Object v = aValue.getClass().getComponentType().getComponentType();
			if (v == Boolean.TYPE) type = FieldType2.BOOLEAN_MATRIX;
			else if (v == Byte.TYPE) type = FieldType2.BYTE_MATRIX;
			else if (v == Short.TYPE) type = FieldType2.SHORT_MATRIX;
			else if (v == Character.TYPE) type = FieldType2.CHAR_MATRIX;
			else if (v == Integer.TYPE) type = FieldType2.INT_MATRIX;
			else if (v == Long.TYPE) type = FieldType2.LONG_MATRIX;
			else if (v == Float.TYPE) type = FieldType2.FLOAT_MATRIX;
			else if (v == Double.TYPE) type = FieldType2.DOUBLE_MATRIX;
			else if (v == String.class) type = FieldType2.STRING_MATRIX;
			else if (v == Date.class) type = FieldType2.DATE_MATRIX;
			else if (v == Bundle.class) type = FieldType2.BUNDLE_MATRIX;
			else throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}
		else if (aValue.getClass().isArray())
		{
			Object v = aValue.getClass().getComponentType();
			if (v == Boolean.TYPE) type = FieldType2.BOOLEAN_ARRAY;
			else if (v == Byte.TYPE) type = FieldType2.BYTE_ARRAY;
			else if (v == Short.TYPE) type = FieldType2.SHORT_ARRAY;
			else if (v == Character.TYPE) type = FieldType2.CHAR_ARRAY;
			else if (v == Integer.TYPE) type = FieldType2.INT_ARRAY;
			else if (v == Long.TYPE) type = FieldType2.LONG_ARRAY;
			else if (v == Float.TYPE) type = FieldType2.FLOAT_ARRAY;
			else if (v == Double.TYPE) type = FieldType2.DOUBLE_ARRAY;
			else if (v == String.class) type = FieldType2.STRING_ARRAY;
			else if (v == Date.class) type = FieldType2.DATE_ARRAY;
			else if (v == Bundle.class) type = FieldType2.BUNDLE_ARRAY;
			else throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}
		else if (aValue instanceof ArrayList)
		{
			ArrayList list = (ArrayList)aValue;
			Object v = list.get(0);
			Class t = null;
			if (v instanceof Boolean) {type = FieldType2.BOOLEAN_ARRAYLIST; t = Boolean.class;}
			else if (v instanceof Byte) {type = FieldType2.BYTE_ARRAYLIST; t = Byte.class;}
			else if (v instanceof Short) {type = FieldType2.SHORT_ARRAYLIST; t = Short.class;}
			else if (v instanceof Character) {type = FieldType2.CHAR_ARRAYLIST; t = Character.class;}
			else if (v instanceof Integer) {type = FieldType2.INT_ARRAYLIST; t = Integer.class;}
			else if (v instanceof Long) {type = FieldType2.LONG_ARRAYLIST; t = Long.class;}
			else if (v instanceof Float) {type = FieldType2.FLOAT_ARRAYLIST; t = Float.class;}
			else if (v instanceof Double) {type = FieldType2.DOUBLE_ARRAYLIST; t = Double.class;}
			else if (v instanceof String) {type = FieldType2.STRING_ARRAYLIST; t = String.class;}
			else if (v instanceof Date) {type = FieldType2.DATE_ARRAYLIST; t = Date.class;}
			else if (v instanceof Bundle) {type = FieldType2.BUNDLE_ARRAYLIST; t = Bundle.class;}
			else throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());

			for (Object item : list)
			{
				if (item != null && item.getClass() != t)
				{
					throw new IllegalArgumentException("Unsupported type in ArrayList: " + aValue.getClass());
				}
			}
		}
		else throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());

		put(aKey, aValue, type);
	}


	void put(String aKey, Object aValue, FieldType2 aType)
	{
		if (aKey == null)
		{
			throw new IllegalArgumentException("Provided key is null.");
		}

		mValues.put(aKey, aValue);
		mTypes.put(aKey, aType);
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
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		byte[] buf = new byte[in.readInt()];
		in.read(buf);
		new BinaryDecoder().unmarshal(this, buf);
	}


	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		byte[] buf = new BinaryEncoder().marshal(this);
		out.writeInt(buf.length);
		out.write(buf);
	}


//	public static Bundle createBundle(Map<String,?> aMap, ConvertValue aConvertValue)
//	{
//		return createBundle(aMap, null, aConvertValue);
//	}
//
//
//	public static Bundle createBundle(Map<?,?> aMap, ConvertValue aConvertKey, ConvertValue aConvertValue)
//	{
//		Bundle bundle = new Bundle();
//
//		for (Entry entry : aMap.entrySet())
//		{
//			Object key = entry.getKey();
//			Object value = entry.getValue();
//
//			if (aConvertKey != null)
//			{
//				key = aConvertKey.convert(key);
//			}
//			if (aConvertValue != null)
//			{
//				value = aConvertValue.convert(value);
//			}
//
//			if (key != null)
//			{
//				if (!(key instanceof String))
//				{
//					throw new IllegalStateException("A key was not converted to String: " + entry.getKey());
//				}
//
//				bundle.put((String)key, value);
//			}
//		}
//
//		return bundle;
//	}
//
//
//	public interface ConvertValue
//	{
//		/**
//		 * Return the value as a standard Java type or other type supported by the Bundle implementation.
//		 *
//		 * @param aValue
//		 *   a java object.
//		 * @return
//		 *   a standard Java type or other type supported by the Bundle implementation.
//		 */
//		Object convert(Object aValue);
//	}
}