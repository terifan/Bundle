package org.terifan.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class Array extends Container<Integer, Array> implements Serializable, Iterable
{
	private static final long serialVersionUID = 1L;

	protected ArrayList<Object> mValues;


	public Array()
	{
		mValues = new ArrayList<>();
	}


	@Override
	public <T> T get(Integer aIndex)
	{
		return (T)mValues.get(aIndex);
	}


	@Override
	Array set(Integer aIndex, Object aValue)
	{
		if (aIndex == size())
		{
			mValues.add(aValue);
		}
		else
		{
			mValues.set(aIndex, aValue);
		}
		return this;
	}


	public Array add(Object aValue)
	{
		if (aValue != null && aValue.getClass().isArray())
		{
			for (int i = 0, sz = java.lang.reflect.Array.getLength(aValue); i < sz; i++)
			{
				Object v = java.lang.reflect.Array.get(aValue, i);

				if (v != null && v.getClass().isArray())
				{
					addRecursive(v);
				}
				else
				{
					addImpl(v);
				}
			}
		}
		else if (aValue instanceof List)
		{
			for (Object w : (List)aValue)
			{
				addRecursive(w);
			}
		}
		else if (aValue instanceof Stream)
		{
			((Stream)aValue).forEach(this::addRecursive);
		}
		else if (aValue instanceof Bundlable)
		{
			BundlableOutput out = new BundlableOutput();
			((Bundlable)aValue).writeExternal(out);
			addImpl(out.getContainer());
		}
		else
		{
			addRecursive(aValue);
		}

		return this;
	}


	public Array add(Object... aValues)
	{
		if (aValues == null)
		{
			addImpl(null);
		}
		else
		{
			for (Object v : aValues)
			{
				addRecursive(v);
			}
		}

		return this;
	}


	@Override
	public int size()
	{
		return mValues.size();
	}


	@Override
	public Array clear()
	{
		mValues.clear();
		return this;
	}


	@Override
	public Array remove(Integer aIndex)
	{
		mValues.remove((int)aIndex);
		return this;
	}


	@Override
	public boolean containsKey(Integer aKey)
	{
		return aKey != null && mValues.size() > aKey;
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
		return marshalJSON(true);
	}


	@Override
	MurmurHash32 hashCode(MurmurHash32 aHash)
	{
		for (Object value : mValues)
		{
			super.hashCode(aHash, value);
		}

		return aHash;
	}


	@Override
	public boolean equals(Object aOther)
	{
		if (aOther instanceof Array)
		{
			return mValues.equals(((Array)aOther).mValues);
		}

		return false;
	}


//	 * Create an array of item provided including primitives, arrays and objects implementing the Bundlable and BundlableValue interfaces.
	/**
	 * Create an array of item provided including primitives and arrays.
	 *
	 * Note: if the object provided is an array it will be consumed, e.g. these two samples will result in the same structure (json: "[1,2,3,4]"):
	 * <code>
	 *    Array.of(1,2,3,4);
	 *
	 *    int[] values = {1,2,3,4};
	 *    Array.of(values);
	 * </code>
	 *
	 * @param aValues an array of objects
	 * @return an array
	 */
	public static Array of(Object aValue)
	{
		Array array = new Array();

		if (aValue != null && aValue.getClass().isArray())
		{
			for (int i = 0, sz = java.lang.reflect.Array.getLength(aValue); i < sz; i++)
			{
				Object v = java.lang.reflect.Array.get(aValue, i);

				if (v != null && v.getClass().isArray())
				{
					array.addRecursive(v);
				}
				else
				{
					array.add(v);
				}
			}
		}
		else if (aValue instanceof List)
		{
			for (Object w : (List)aValue)
			{
				array.addRecursive(w);
			}
		}
		else if (aValue instanceof Stream)
		{
			((Stream)aValue).forEach(array::addRecursive);
		}
		else if (aValue == null || isSupportedType(aValue) || Serializable.class.isAssignableFrom(aValue.getClass()))
		{
			array.addRecursive(aValue);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}

		return array;
	}


//	 * Create an array of item provided including primitives, arrays and objects implementing the Bundlable and BundlableValue interfaces.
	/**
	 * Create an array of item provided including primitives and arrays.
	 *
	 * @param aValues an array of objects
	 * @return an array
	 */
	public static Array of(Object... aValues)
	{
		Array array = new Array();

		if (aValues == null)
		{
			array.addImpl(null);
		}
		else
		{
			for (Object v : aValues)
			{
				array.addRecursive(v);
			}
		}

		return array;
	}


	public static Array from(Object[] aValues)
	{
		Array array = new Array();

		if (aValues == null)
		{
			array.addImpl(null);
		}
		else
		{
			for (Object v : aValues)
			{
				array.addRecursive(v);
			}
		}

		return array;
	}


	private void addRecursive(Object aValue)
	{
		if (aValue != null && aValue.getClass().isArray())
		{
			Array arr = new Array();

			for (int i = 0, sz = java.lang.reflect.Array.getLength(aValue); i < sz; i++)
			{
				arr.addRecursive(java.lang.reflect.Array.get(aValue, i));
			}

			addImpl(arr);
		}
		else if (aValue instanceof List)
		{
			Array arr = new Array();

			for (Object w : (List)aValue)
			{
				arr.addRecursive(w);
			}

			addImpl(arr);
		}
		else if (aValue instanceof Stream)
		{
			Array arr = new Array();

			((Stream)aValue).forEach(arr::addRecursive);

			addImpl(arr);
		}
		else if (aValue instanceof Bundlable)
		{
			BundlableOutput out = new BundlableOutput();
 			((Bundlable)aValue).writeExternal(out);
			addImpl(out.getContainer());
		}
		else if (aValue == null || isSupportedType(aValue))
		{
			addImpl(aValue);
		}
		else if (Serializable.class.isAssignableFrom(aValue.getClass()))
		{
			putSerializable(size(), (Serializable)aValue);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}
	}


	private void addImpl(Object aValue)
	{
		mValues.add(aValue);
	}


//	public <T extends Bundlable> T[] to(Class<T> aType)
//	{
//		Object arr = java.lang.reflect.Array.newInstance(aType, size());
//
//		for (int i = 0; i < size(); i++)
//		{
//			java.lang.reflect.Array.set(arr, i, getBundlable(aType, i));
//		}
//
//		return (T[])arr;
//	}


	@Override
	public Map<Integer, Object> toMap()
	{
		LinkedHashMap<Integer, Object> map = new LinkedHashMap<>();
		int i = 0;
		for (Object v : mValues)
		{
			map.put(i++, v);
		}
		return map;
	}


	public ArrayList<Boolean> getBooleanArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Boolean.class);
	}


	public ArrayList<Byte> getByteArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Byte.class);
	}


	public ArrayList<Short> getShortArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Short.class);
	}


	public ArrayList<Integer> getIntArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Integer.class);
	}


	public ArrayList<Long> getLongArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Long.class);
	}


	public ArrayList<Float> getFloatArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Float.class);
	}


	public ArrayList<Double> getDoubleArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Double.class);
	}


	public ArrayList<Number> getNumberArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Number.class);
	}


	public ArrayList<Bundle> getBundleArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), Bundle.class);
	}


	public ArrayList<String> getStringArrayList(int aIndex)
	{
		return castArrayList(getArray(aIndex), String.class);
	}


	public ArrayList<Boolean> toBooleanArrayList()
	{
		return castArrayList(this, Boolean.class);
	}


	public ArrayList<Byte> toByteArrayList()
	{
		return castArrayList(this, Byte.class);
	}


	public ArrayList<Short> toShortArrayList()
	{
		return castArrayList(this, Short.class);
	}


	public ArrayList<Integer> toIntArrayList()
	{
		return castArrayList(this, Integer.class);
	}


	public ArrayList<Long> toLongArrayList()
	{
		return castArrayList(this, Long.class);
	}


	public ArrayList<Float> toFloatArrayList()
	{
		return castArrayList(this, Float.class);
	}


	public ArrayList<Double> toDoubleArrayList()
	{
		return castArrayList(this, Double.class);
	}


	public ArrayList<Number> toNumberArrayList()
	{
		return castArrayList(this, Number.class);
	}


	public ArrayList<Bundle> toBundleArrayList()
	{
		return castArrayList(this, Bundle.class);
	}


	public ArrayList<String> toStringArrayList()
	{
		return castArrayList(this, String.class);
	}


	private <T> ArrayList<T> castArrayList(Array a, Class<T> aType)
	{
		ArrayList<T> out = new ArrayList<>();

		for (int i = 0; i < a.size(); i++)
		{
			Object v = a.get(i);

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
}
