package samples;

import org.terifan.bundle.*;
import java.io.Serializable;
import java.util.Arrays;


class Position implements Serializable, Bundlable<Bundle>
{
	private static final long serialVersionUID = 1L;
	private double[] values;


	public Position()
	{
	}


	public Position(double aX, double aY, double aZ)
	{
		values = new double[]{aX, aY, aZ};
	}


	@Override
	public void readExternal(Bundle aValues)
	{
		Array coords = aValues.getArray("coords");
		values = new double[]
		{
			coords.getDouble(0),
			coords.getDouble(1),
			coords.getDouble(2)
		};
	}


	@Override
	public void writeExternal(Bundle aValues)
	{
		aValues.putNumber("x", values[0]);
		aValues.putNumber("y", values[1]);
		aValues.putNumber("z", values[2]);
	}


	@Override
	public String toString()
	{
		return "Position{" + values[0] + "," + values[1] + "," + values[2] + '}';
	}


	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 73 * hash + Arrays.hashCode(this.values);
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
		final Position other = (Position)obj;
		if (!Arrays.equals(this.values, other.values))
		{
			return false;
		}
		return true;
	}
}
