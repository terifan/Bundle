package org.terifan.bundle;

import java.io.Serializable;


class Position implements Bundlable<Array>, Serializable
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
	public void readExternal(Array aArray)
	{
		values = new double[]
		{
			aArray.getDouble(0),
			aArray.getDouble(1),
			aArray.getDouble(2)
		};
	}


	@Override
	public void writeExternal(Array aBundle)
	{
	}


	@Override
	public String toString()
	{
		return "Position{" + values[0] + "," + values[1] + "," + values[2] + '}';
	}
}
