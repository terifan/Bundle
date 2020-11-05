package samples;

import java.io.IOException;
import org.terifan.bundle.Array;
import org.terifan.bundle.Bundlable;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.BundleInput;
import org.terifan.bundle.BundleOutput;


public class _Vector implements Bundlable
{
	private static final long serialVersionUID = 1L;

	private double x, y, z;


	public _Vector()
	{
	}


	public _Vector(double aX, double aY, double aZ)
	{
		this.x = aX;
		this.y = aY;
		this.z = aZ;
	}


	@Override
	public void readExternal(BundleInput aIn)
	{
		Array in = aIn.array();
		x = in.getDouble(0);
		y = in.getDouble(1);
		z = in.getDouble(2);
	}


	@Override
	public void writeExternal(BundleOutput aOut)
	{
		aOut.array(x, y, z);
	}


	@Override
	public String toString()
	{
		return "_Vector{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
	}


	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 73 * hash + (int)(Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 73 * hash + (int)(Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 73 * hash + (int)(Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
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
		final _Vector other = (_Vector)obj;
		if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
		{
			return false;
		}
		if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
		{
			return false;
		}
		if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z))
		{
			return false;
		}
		return true;
	}
}
