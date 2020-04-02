package samples;

import java.util.Arrays;
import org.terifan.bundle.Array;
import org.terifan.bundle.Bundlable;


class Triangle implements Bundlable<Array>
{
	private Vector[] mVerticies;


	public Triangle()
	{
	}


	public Triangle(Vector... aVerticies)
	{
		mVerticies = aVerticies;
	}


	@Override
	public void readExternal(Array aBundle)
	{
		mVerticies = new Vector[3];
		for (int i = 0; i < 3; i++)
		{
			Array arr = new Array();
			mVerticies[i] = new Vector();
			mVerticies[i].readExternal(arr);
			aBundle.add(arr);
		}
	}


	@Override
	public void writeExternal(Array aBundle)
	{
		for (Vector v : mVerticies)
		{
			Array arr = new Array();
			v.writeExternal(arr);
			aBundle.add(arr);
		}
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
		final Triangle other = (Triangle)obj;
		if (!Arrays.deepEquals(this.mVerticies, other.mVerticies))
		{
			return false;
		}
		return true;
	}
}
