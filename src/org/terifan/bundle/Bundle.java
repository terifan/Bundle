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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public final class Bundle implements Cloneable, Externalizable, Iterable<String>
{
	private final static long serialVersionUID = 1L;

	private final Map<String, Object> mValues = new LinkedHashMap<>();
	private boolean mStrict;


	/**
	 * Create a new empty Bundle.
	 */
	public Bundle()
	{
	}


	/**
	 * Copy the provided Bundle into a new instance.
	 */
	public Bundle(Bundle aOther)
	{
		putAll(aOther);
	}


	public Bundle setStrict(boolean aStrict)
	{
		mStrict = aStrict;
		return this;
	}


	public boolean isStrict()
	{
		return mStrict;
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
			return ((Bundle)o).setStrict(mStrict);
		}
		catch (ClassCastException e)
		{
			return (Bundle)typeWarning(aKey, o, "Bundle", aDefaultValue, e);
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
			return (Bundle[])typeWarning(aKey, o, "Bundle[]", null, e);
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
			return (Bundle[][])typeWarning(aKey, o, "Bundle[][]", null, e);
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
			return (ArrayList<Bundle>)typeWarning(aKey, o, "ArrayList<Bundle>", null, e);
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
			if (mStrict || o instanceof Boolean)
			{
				return (Boolean)o;
			}
			return ((Number)o).longValue() != 0;
		}
		catch (ClassCastException e)
		{
			return (Boolean)typeWarning(aKey, o, "Boolean", aDefaultValue, e);
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
			return (boolean[])typeWarning(aKey, o, "boolean[]", null, e);
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
			return (boolean[][])typeWarning(aKey, o, "boolean[][]", null, e);
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
			return (ArrayList<Boolean>)typeWarning(aKey, o, "ArrayList<Boolean>", null, e);
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
			if (mStrict)
			{
				return (Byte)o;
			}
			return ((Number)o).byteValue();
		}
		catch (ClassCastException e)
		{
			return (Byte)typeWarning(aKey, o, "Byte", aDefaultValue, e);
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
			return (byte[])typeWarning(aKey, o, "byte[]", null, e);
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
			return (byte[][])typeWarning(aKey, o, "byte[][]", null, e);
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
			return (ArrayList<Byte>)typeWarning(aKey, o, "ArrayList<Byte>", null, e);
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
			if (mStrict || o instanceof Character)
			{
				return (Character)o;
			}
			return (char)((Number)o).shortValue();
		}
		catch (ClassCastException e)
		{
			return (Character)typeWarning(aKey, o, "Character", aDefaultValue, e);
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
			return (char[])typeWarning(aKey, o, "char[]", null, e);
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
			return (char[][])typeWarning(aKey, o, "char[][]", null, e);
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
			return (ArrayList<Character>)typeWarning(aKey, o, "ArrayList<Character>", null, e);
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
			if (mStrict)
			{
				return (Double)o;
			}
			return ((Number)o).doubleValue();
		}
		catch (ClassCastException e)
		{
			return (Double)typeWarning(aKey, o, "Double", aDefaultValue, e);
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
			return (double[])typeWarning(aKey, o, "double[]", null, e);
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
			return (double[][])typeWarning(aKey, o, "double[][]", null, e);
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
			return (ArrayList<Double>)typeWarning(aKey, o, "ArrayList<Double>", null, e);
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
			if (mStrict)
			{
				return (Float)o;
			}
			return ((Number)o).floatValue();
		}
		catch (ClassCastException e)
		{
			return (Float)typeWarning(aKey, o, "Float", aDefaultValue, e);
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
			return (float[])typeWarning(aKey, o, "float[]", null, e);
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
			return (float[][])typeWarning(aKey, o, "float[][]", null, e);
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
			return (ArrayList<Float>)typeWarning(aKey, o, "ArrayList<Float>", null, e);
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
			if (mStrict)
			{
				return (Integer)o;
			}
			return ((Number)o).intValue();
		}
		catch (ClassCastException e)
		{
			return (Integer)typeWarning(aKey, o, "Integer", aDefaultValue, e);
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
			return (int[])typeWarning(aKey, o, "int[]", null, e);
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
			return (int[][])typeWarning(aKey, o, "int[][]", null, e);
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
			return (ArrayList<Integer>)typeWarning(aKey, o, "ArrayList<Integer>", null, e);
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
			if (mStrict)
			{
				return (Long)o;
			}
			return ((Number)o).longValue();
		}
		catch (ClassCastException e)
		{
			return (Long)typeWarning(aKey, o, "Long", aDefaultValue, e);
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
			return (long[])typeWarning(aKey, o, "long[]", null, e);
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
			return (long[][])typeWarning(aKey, o, "long[][]", null, e);
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
			return (ArrayList<Long>)typeWarning(aKey, o, "ArrayList<Long>", null, e);
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
			if (mStrict)
			{
				return (Short)o;
			}
			return ((Number)o).shortValue();
		}
		catch (ClassCastException e)
		{
			return (Short)typeWarning(aKey, o, "Short", aDefaultValue, e);
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
			return (short[])typeWarning(aKey, o, "short[]", null, e);
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
			return (short[][])typeWarning(aKey, o, "short[][]", null, e);
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
			return (ArrayList<Short>)typeWarning(aKey, o, "ArrayList<Short>", null, e);
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
			return (String)typeWarning(aKey, o, "String", aDefaultValue, e);
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
			return (String[])typeWarning(aKey, o, "String[]", null, e);
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
			return (String[][])typeWarning(aKey, o, "String[][]", null, e);
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
			return (ArrayList<String>)typeWarning(aKey, o, "ArrayList<String>", null, e);
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
			if (!mStrict && !(o instanceof Date))
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
			return (Date)typeWarning(aKey, o, "Date", aDefaultValue, e);
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
			return (Date[])typeWarning(aKey, o, "Date[]", null, e);
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
			return (Date[][])typeWarning(aKey, o, "Date[][]", null, e);
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
			return (ArrayList<Date>)typeWarning(aKey, o, "ArrayList<Date>", null, e);
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
			put(key, aOther.mValues.get(key));
		}
		return this;
	}


	/**
	 * Inserts a Bundle value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBundle(String aKey, Bundle aValue)
	{
		put(aKey, aValue);
		return this;
	}


	public Bundle putBundleArray(String aKey, Bundle... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	public Bundle putBundleMatrix(String aKey, Bundle[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	public Bundle putBundleArrayList(String aKey, ArrayList<Bundle> aValue)
	{
		put(aKey, aValue);
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
		put(aKey, bundle);
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
		put(aKey, bundles);
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
		put(aKey, bundles);
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
		put(aKey, bundles);
		return this;
	}


	/**
	 * Inserts a Boolean value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBoolean(String aKey, boolean aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanArray(String aKey, boolean... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanMatrix(String aKey, boolean[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putBooleanArrayList(String aKey, ArrayList<Boolean> aValue)
	{
		put(aKey, aValue);
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
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteArray(String aKey, byte... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteMatrix(String aKey, byte[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putByteArrayList(String aKey, ArrayList<Byte> aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a char value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putChar(String aKey, char aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharArray(String aKey, char... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharMatrix(String aKey, char[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putCharArrayList(String aKey, ArrayList<Character> aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a double value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDouble(String aKey, double aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a double array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleArray(String aKey, double... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a double array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleArray(String aKey, double[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a double list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDoubleArrayList(String aKey, ArrayList<Double> aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a float value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloat(String aKey, float aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatArray(String aKey, float... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatMatrix(String aKey, float[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putFloatArrayList(String aKey, ArrayList<Float> aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts an int value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putInt(String aKey, int aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts an int array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntArray(String aKey, int... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts an int array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntMatrix(String aKey, int[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putIntArrayList(String aKey, ArrayList<Integer> aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a long value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLong(String aKey, long aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a long array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongArray(String aKey, long... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a long array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongMatrix(String aKey, long[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a long list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putLongArrayList(String aKey, ArrayList<Long> aValue)
	{
		put(aKey, aValue);
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
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a short array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArray(String aKey, short... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a short array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArray(String aKey, short[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a short list value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putShortArrayList(String aKey, ArrayList<Short> aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a String value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putString(String aKey, String aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a String array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringArray(String aKey, String... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a String array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringMatrix(String aKey, String[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putStringArrayList(String aKey, ArrayList<String> aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a Date value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDate(String aKey, Date aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a Date array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateArray(String aKey, Date... aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts a Date array value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateMatrix(String aKey, Date[][] aValue)
	{
		put(aKey, aValue);
		return this;
	}


	/**
	 * Inserts an ArrayList value into the mapping of this Bundle, replacing any existing value for the given key.
	 */
	public Bundle putDateArrayList(String aKey, ArrayList<Date> aValue)
	{
		put(aKey, aValue);
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


	void put(String aKey, Object aValue)
	{
		if (aKey == null)
		{
			throw new IllegalArgumentException("Provided key is null.");
		}

		mValues.put(aKey, aValue);
	}


	/**
	 * Override this method to handle type casting errors.
	 *
	 * @return
	 *    default implementation return the default value provided
	 */
	protected Object typeWarning(String aKey, Object aValue, String aClassName, Object aDefaultValue, Exception aException)
	{
		System.err.println("Attempt to cast generated internal exception: Key '" + aKey+ "' expected "+aClassName+" but value was a "+aValue.getClass().getSimpleName()+". The default value '"+aDefaultValue+"' was returned.");

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


	public static Bundle createBundle(Map<String,?> aMap, ConvertValue aConvertValue)
	{
		return createBundle(aMap, null, aConvertValue);
	}


	public static Bundle createBundle(Map<?,?> aMap, ConvertValue aConvertKey, ConvertValue aConvertValue)
	{
		Bundle bundle = new Bundle();

		for (Entry entry : aMap.entrySet())
		{
			Object key = entry.getKey();
			Object value = entry.getValue();

			if (aConvertKey != null)
			{
				key = aConvertKey.process(key);
			}
			if (aConvertValue != null)
			{
				value = aConvertValue.process(value);
			}

			if (key != null)
			{
				if (!(key instanceof String))
				{
					throw new IllegalStateException("A key was not converted to String: " + entry.getKey());
				}

				bundle.put((String)key, value);
			}
		}

		return bundle;
	}


	public interface ConvertValue
	{
		/**
		 * Return the value as a standard Java type or other type supported by the Bundle implementation.
		 *
		 * @param aValue
		 *   a java object.
		 * @return
		 *   a standard Java type or other type supported by the Bundle implementation.
		 */
		Object process(Object aValue);
	}
}