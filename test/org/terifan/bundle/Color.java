package org.terifan.bundle;


class Color implements Bundlable, BundlableValue<Integer>
{
	private int r, g, b;


	public Color()
	{
	}


	public Color(int aR, int aG, int aB)
	{
		this.r = aR;
		this.g = aG;
		this.b = aB;
	}


	@Override
	public void readExternal(Bundle aBundle)
	{
		r = aBundle.getInt("r");
		g = aBundle.getInt("g");
		b = aBundle.getInt("b");
	}


	@Override
	public void writeExternal(Bundle aBundle)
	{
		aBundle.putNumber("r", r);
		aBundle.putNumber("g", g);
		aBundle.putNumber("b", b);
	}


	@Override
	public void readExternal(Integer aValue)
	{
		r = 0xff & (aValue >> 16);
		g = 0xff & (aValue >> 8);
		b = 0xff & (aValue);
	}


	@Override
	public Integer writeExternal()
	{
		return (r << 16) + (g << 8) + b;
	}


	@Override
	public String toString()
	{
		return "Color{r=" + r + ", g=" + g + ", b=" + b + '}';
	}
}
