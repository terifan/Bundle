package samples;

import java.util.Arrays;
import org.terifan.bundle.Array;
import org.terifan.bundle.Bundlable;
import org.terifan.bundle.BundleInput;
import org.terifan.bundle.BundleOutput;


class _Triangle implements Bundlable
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
	public void readExternal(BundleInput aIn)
	{
		mVerticies = aIn.array().getBundlableArray(0, _Vector.class);
		mColors = aIn.array().getBundlableArray(1, _RGB.class);
	}


	@Override
	public void writeExternal(BundleOutput aOut)
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
