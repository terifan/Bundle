package samples;

import org.terifan.bundle.*;
import java.io.Serializable;


public class _RGB implements Serializable, Bundlable<Array>
{
	private static final long serialVersionUID = 1L;

	private int r, g, b;


	public _RGB()
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


	public _RGB(int aR, int aG, int aB)
	{
		this.r = aR;
		this.g = aG;
		this.b = aB;
	}


	@Override
	public String toString()
	{
		return "Color{r=" + r + ", g=" + g + ", b=" + b + '}';
	}


	@Override
	public void readExternal(Array aArray)
	{
		r = aArray.getInt(0);
		g = aArray.getInt(1);
		b = aArray.getInt(2);
	}


	@Override
	public void writeExternal(Array aArray)
	{
		aArray.add(r);
		aArray.add(g);
		aArray.add(b);
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
		final _RGB other = (_RGB)obj;
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
