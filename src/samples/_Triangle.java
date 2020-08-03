package samples;

import java.util.Arrays;
import org.terifan.bundle.Array;
import org.terifan.bundle.Bundlable;
import org.terifan.bundle.Bundle;


class _Triangle implements Bundlable<Bundle>
{
	private _Vector[] mVerticies;
	private _RGB[] mColors;


	public _Triangle()
	{
	}


	public _Triangle(_Vector[] aVerticies, _RGB[] aColors)
	{
		mVerticies = aVerticies;
		mColors = aColors;
	}


	@Override
	public void readExternal(Bundle aBundle)
	{
//		mVerticies = new _Vector[3];
//		for (int i = 0; i < 3; i++)
//		{
//			Array arr = new Array();
//			mVerticies[i] = new _Vector();
//			mVerticies[i].readExternal(arr);
//			aBundle.add(arr);
//		}
	}


	@Override
	public void writeExternal(Bundle aBundle)
	{
		Array coords = new Array();
		Array colors = new Array();
		for (_Vector v : mVerticies)
		{
			Array arr = new Array();
			v.writeExternal(arr);
			coords.add(arr);
		}
		for (_RGB v: mColors)
		{
			Array col = new Array();
			v.writeExternal(col);
			colors.add(col);
		}
		aBundle.putArray("coords", coords);
		aBundle.putArray("colors", colors);
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
}
