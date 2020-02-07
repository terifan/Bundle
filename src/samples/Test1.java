package samples;

import org.terifan.bundle.Array;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.BundlableValue;
import org.terifan.bundle.Bundlable;


public class Test1
{
	public static void main(String... args)
	{
		try
		{
			Bundle bundle = new Bundle()
				.putArray("coordinates", Array.of(new Vector(0,0,0), new Vector(0,0,1), new Vector(0,1,0), new Vector(0,1,1), new Vector(1,0,0), new Vector(1,0,1), new Vector(1,1,0), new Vector(1,1,1)))
				.putArray("colors", Array.of(new RGB(0,0,0), new RGB(0,0,1), new RGB(0,1,0), new RGB(0,1,1), new RGB(1,0,0), new RGB(1,0,1), new RGB(1,1,0), new RGB(1,1,1)))
				.putArray("indices", Array.of(Array.of(0,1,2), Array.of(0,2,3), Array.of(4,5,6), Array.of(4,6,7)))
				.putNumber("double", 3.14);

			System.out.println(bundle.marshalJSON(false));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	static class RGB implements BundlableValue<Integer>
	{
		private int r, g, b;


		public RGB()
		{
		}


		public RGB(int aR, int aG, int aB)
		{
			this.r = aR;
			this.g = aG;
			this.b = aB;
		}


//		@Override
//		public void readExternal(Bundle aBundle)
//		{
//			r = aBundle.getInt("r");
//			g = aBundle.getInt("g");
//			b = aBundle.getInt("b");
//		}
//
//
//		@Override
//		public void writeExternal(Bundle aBundle)
//		{
//			aBundle.putNumber("r", r);
//			aBundle.putNumber("g", g);
//			aBundle.putNumber("b", b);
//		}


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


	static class Vector implements Bundlable
	{
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
		public void readExternal(Bundle aBundle)
		{
			x = aBundle.getInt("x");
			y = aBundle.getInt("y");
			z = aBundle.getInt("z");
		}


		@Override
		public void writeExternal(Bundle aBundle)
		{
			aBundle.putNumber("x", x);
			aBundle.putNumber("y", y);
			aBundle.putNumber("z", z);
		}


		@Override
		public String toString()
		{
			return "Vector{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
		}
	}
}
