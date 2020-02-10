package org.terifan.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
			Bundle bundle = new Bundle();
			((Bundlable)aValue).writeExternal(bundle);
			addImpl(bundle);
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


	/**
	 * Create an array of item provided including primitives, arrays and objects implementing the Bundlable and BundlableValue interfaces.
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


	/**
	 * Create an array of item provided including primitives, arrays and objects implementing the Bundlable and BundlableValue interfaces.
	 *
	 * @param aValues an array of objects
	 * @return an array
	 */
	public static Array of(Object... aValues)
	{
		Array array = new Array();

		if (aValues == null)
		{
			array.add(null);
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
		if (aValue instanceof BundlableValue)
		{
			aValue = ((BundlableValue)aValue).writeExternal();
		}

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
			Bundle bundle = new Bundle();
			((Bundlable)aValue).writeExternal(bundle);
			addImpl(bundle);
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
}
