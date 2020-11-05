package samples;

import java.io.Serializable;
import org.terifan.bundle.*;


public class _RGB implements Bundlable, Serializable
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
		return "_RGB{r=" + r + ", g=" + g + ", b=" + b + '}';
	}


	@Override
	public void readExternal(BundleInput aIn)
	{
		Bundle in = aIn.bundle();
		r = in.getInt("r");
		g = in.getInt("g");
		b = in.getInt("b");
	}


	@Override
	public void writeExternal(BundleOutput aOut)
	{
		aOut.bundle()
			.putNumber("r", r)
			.putNumber("g", g)
			.putNumber("b", b);
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
