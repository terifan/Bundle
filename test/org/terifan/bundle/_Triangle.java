package org.terifan.bundle;

import java.util.Arrays;


class _Triangle implements Bundlable
{
	private _Vector[] mVerticies;
	private _Color[] mColors;


	public _Triangle()
	{
	}


	public _Triangle(_Vector[] aVerticies, _Color[] aColors)
	{
		mVerticies = aVerticies;
		mColors = aColors;
	}


	@Override
	public void readExternal(BundlableInput aIn)
	{
		Array in = aIn.array();
		mVerticies = in.getBundlableArray(0, _Vector.class);
		mColors = in.getBundlableArray(1, _Color.class);
	}


	@Override
	public void writeExternal(BundlableOutput aOut)
	{
		aOut.array(mVerticies, mColors);
	}


	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 29 * hash + Arrays.deepHashCode(this.mVerticies);
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
		final _Triangle other = (_Triangle)obj;
		if (!Arrays.deepEquals(this.mVerticies, other.mVerticies))
		{
			return false;
		}
		return true;
	}


	@Override
	public String toString()
	{
		return "_Triangle{" + "mVerticies=" + Arrays.deepToString(mVerticies) + ", mColors=" + Arrays.deepToString(mColors) + '}';
	}
}
