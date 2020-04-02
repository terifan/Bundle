package samples;

import org.terifan.bundle.*;
import java.io.Serializable;


class Position implements BundlableValue<double[]>, Serializable
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
	public void readExternal(double[] aValues)
	{
		values = aValues;
	}


	@Override
	public double[] writeExternal()
	{
		return values;
	}


	@Override
	public String toString()
	{
		return "Position{" + values[0] + "," + values[1] + "," + values[2] + '}';
	}
}
