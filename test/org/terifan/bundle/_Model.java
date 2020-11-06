package org.terifan.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


class _Model implements Bundlable
{
	private ArrayList<_Triangle> mTriangles;


	public _Model()
	{
	}


	public _Model(_Triangle... aTriangles)
	{
		mTriangles = new ArrayList<>(Arrays.asList(aTriangles));
	}


	@Override
	public void readExternal(BundlableInput aIn)
	{
		mTriangles = aIn.array().getBundlableArrayList(0, _Triangle.class);
	}


	@Override
	public void writeExternal(BundlableOutput aOut)
	{
		aOut.array(mTriangles);
	}


	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 29 * hash + Objects.hashCode(this.mTriangles);
		return hash;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final _Model other = (_Model)obj;
		if (!Objects.equals(this.mTriangles, other.mTriangles))
		{
			return false;
		}
		return true;
	}


	@Override
	public String toString()
	{
		return "_Model{" + "mTriangles=" + mTriangles + '}';
	}
}
