package org.terifan.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import static org.terifan.bundle.BundleConstants.assertSupportedType;
import org.terifan.bundle.JSONEncoder.Printer;


public class Array extends Container<Integer,Array> implements Serializable, Iterable
{
	private static final long serialVersionUID = 1L;

	protected ArrayList mValues;


	public Array()
	{
		mValues = new ArrayList<>();
	}


	public Array(Object... aValues)
	{
		this();
		mValues.addAll(Arrays.asList(aValues));
	}


	@Override
	public Object get(Integer aIndex)
	{
		return mValues.get(aIndex);
	}


	@Override
	Array put(Integer aIndex, Object aValue)
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
		else
		{
			assertSupportedType(aValue);
			mValues.add(aValue);
		}

		return this;
	}


	public Array add(Object... aValues)
	{
		for (Object v : aValues)
		{
			add(v);
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
		StringBuilder builder = new StringBuilder();
		new JSONEncoder().marshalArray(new Printer(builder, true), this);
		return builder.toString();
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
}