package org.terifan.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import static org.terifan.bundle.BundleConstants.assertSupportedType;


public class Array extends Container<Integer, Array> implements Serializable, Iterable
{
	private static final long serialVersionUID = 1L;

	protected ArrayList<Object> mValues;


	public Array()
	{
		mValues = new ArrayList<>();
	}


	public Array(Object... aValues)
	{
		this();
		add(aValues);
	}


	@Override
	public Object get(Integer aIndex)
	{
		return mValues.get(aIndex);
	}


	@Override
	Array set(Integer aIndex, Object aValue)
	{
		mValues.add(aIndex, aValue);
		return this;
	}


	public Array add(Object aValue)
	{
		if (aValue instanceof BundlableValue)
		{
			aValue = ((BundlableValue)aValue).writeExternal();
		}

		if (aValue instanceof Bundlable)
		{
			Bundle bundle = new Bundle();
			((Bundlable)aValue).writeExternal(bundle);
			mValues.add(bundle);
		}
		else if (aValue != null && aValue.getClass().isArray())
		{
			throw new IllegalArgumentException("Us the of(...) method to create Array instances of arrays.");
		}
		else
		{
			assertSupportedType(aValue);
			mValues.add(aValue);
		}

		return this;
	}


	public Array addAll(Object... aValues)
	{
		if (aValues == null)
		{
			add((Object)null);
		}
		else
		{
			for (Object v : aValues)
			{
				add(v);
			}
		}
		return this;
	}


	public Array addAll(List<Object> aValues)
	{
		aValues.forEach(this::add);
		return this;
	}


	public Array addAll(Stream aValues)
	{
		aValues.forEach(this::add);
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
//
//			// TODO: fix
//
//			try
//			{
//				return Arrays.equals(marshal(), ((Array)aOther).marshal());
//			}
//			catch (IOException e)
//			{
//				throw new IllegalArgumentException(e);
//			}
		}

		return false;
	}


	/**
	 * Create an array of item provided including primitives, objects, array and objects implementing the Bundlable interface.
	 * <p>
	 * e.g. creating an array using <code>new Array(new int[2], new boolean[2], "hello");</code> will result in this array:
	 * [0,0,false,false,"hello"]
	 * </p>
	 * <p>
	 * Creating multi dimensional arrays require a cast to Object:
	 *
	 * e.g <code>new Array((Object)new int[][]{{1,2},{3,4}});</code> will result in this array: [[1,2],[3,4]]
	 * </p>
	 * <p>
	 * <strong>Warning</strong>: multi dimensional arrays without cast will be merged:
	 *
	 * e.g <code>new Array(new int[][]{{1,2},{3,4}});</code> will result in this array: [1,2,3,4]
	 * </p>
	 *
	 * @param aBundlable an array of objects
	 * @return an array
	 */
	public static Array of(Object aBundlable)
	{
		Array array = new Array();

		if (aBundlable != null && aBundlable.getClass().isArray())
		{
			for (int i = 0, sz = java.lang.reflect.Array.getLength(aBundlable); i < sz; i++)
			{
				Object w = java.lang.reflect.Array.get(aBundlable, i);
				if (w != null && w.getClass().isArray())
				{
					array.add(Array.of(w));
				}
				else
				{
					array.add(w);
				}
			}
		}
		else if (aBundlable instanceof List)
		{
			for (Object w : (List)aBundlable)
			{
				if (w != null && w.getClass().isArray())
				{
					array.add(Array.of(w));
				}
				else
				{
					array.add(w);
				}
			}
		}
		else if (aBundlable instanceof Bundlable)
		{
			array.add(Bundle.of(aBundlable));
		}
		else
		{
			array.add(aBundlable);
		}

		return array;
	}


	/**
	 * Create an array of item provided including primitives, objects, array and objects implementing the Bundlable interface.
	 * <p>
	 * e.g. creating an array using <code>new Array(new int[2], new boolean[2], "hello");</code> will result in this array:
	 * [0,0,false,false,"hello"]
	 * </p>
	 * <p>
	 * Creating multi dimensional arrays require a cast to Object:
	 *
	 * e.g <code>new Array((Object)new int[][]{{1,2},{3,4}});</code> will result in this array: [[1,2],[3,4]]
	 * </p>
	 * <p>
	 * <strong>Warning</strong>: multi dimensional arrays without cast will be merged:
	 *
	 * e.g <code>new Array(new int[][]{{1,2},{3,4}});</code> will result in this array: [1,2,3,4]
	 * </p>
	 *
	 * @param aBundlable an array of objects
	 * @return an array
	 */
	public static Array of(Object... aBundlable)
	{
		Array array = new Array();

		for (Object v : aBundlable)
		{
			if (v != null && v.getClass().isArray())
			{
				for (int i = 0, sz = java.lang.reflect.Array.getLength(v); i < sz; i++)
				{
					Object w = java.lang.reflect.Array.get(v, i);
					if (w != null && w.getClass().isArray())
					{
						array.add(Array.of(w));
					}
					else
					{
						array.add(w);
					}
				}
			}
			else if (v instanceof Bundlable)
			{
				array.add(Bundle.of(v));
			}
			else
			{
				array.add(v);
			}
		}

		return array;
	}
}
