package samples;

import org.terifan.bundle.*;
import java.io.Serializable;


class Vector implements Serializable, Bundlable<Array>
{
	private static final long serialVersionUID = 1L;

	private double x, y, z;


	public Vector()
	{
	}


	public Vector(double aX, double aY, double aZ)
	{
		this.x = aX;
		this.y = aY;
		this.z = aZ;
	}


	@Override
	public void readExternal(Array aBundle)
	{
		x = aBundle.getDouble(0);
		y = aBundle.getDouble(1);
		z = aBundle.getDouble(2);
	}


	@Override
	public void writeExternal(Array aBundle)
	{
		aBundle.putNumber(0, x);
		aBundle.putNumber(1, y);
		aBundle.putNumber(2, z);
	}


	@Override
	public String toString()
	{
		return "Vector{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
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
		final Vector other = (Vector)obj;
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
