package org.terifan.bundle;


class Color implements Bundlable, BundlableValue<Integer>
{
	private int r, g, b;


	public Color()
	{
	}


	public int getRed()
	{
		return r;
	}


	public int getGreen()
	{
		return g;
	}


	public int getBlue()
	{
		return b;
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


	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 37 * hash + this.r;
		hash = 37 * hash + this.g;
		hash = 37 * hash + this.b;
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
		final Color other = (Color)obj;
		if (this.r != other.r)
		{
			return false;
		}
		if (this.g != other.g)
		{
			return false;
		}
		if (this.b != other.b)
		{
			return false;
		}
		return true;
	}
}
