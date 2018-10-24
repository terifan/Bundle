package samples;

import java.util.stream.Collectors;
import org.terifan.bundle2.BundlableValueX;
import org.terifan.bundle2.BundlableX;
import org.terifan.bundle2.BundleX;
import org.terifan.bundle2.BundleX.BooleanArray;
import org.terifan.bundle2.BundleX.BundleArray;
import org.terifan.bundle2.BundleX.NumberArray;
import org.terifan.bundle2.BundleX.StringArray;


public class Test
{
	public static void main(String... args)
	{
		try
		{
			BundleX bundle = new BundleX()
				.putBundle("numbers", new BundleX()
					.putNumber("number", 7)
					.putArray("ints", new NumberArray().add(1, 4, 9))
					.putArray("doubles", new NumberArray().add(1.3).add(2.2).add(3.1))
				)
				.putArray("strings", new StringArray().add("a", null).add("b").add("c"))
				.putBoolean("boolean", true)
				.putArray("booleans", new BooleanArray().add(true).add(false).add(true))
				.putArray("bundles", new BundleArray().add(new BundleX().putString("key", "value")))
				.putBundle("bundle", new BundleX().putString("key", "value"))
				.putString("string", "text")
				.putBundle("color", new Color(196,128,20))
				.putArray("colors", new BundleArray().add(new Color(196,128,20), new Color(96,128,220)))
				.putObject("rgb", new Color(196,128,20))
				.putArray("rgbs", new NumberArray().add(new Color(196,128,20), new Color(96,128,220)))
			;

			System.out.println(bundle.getBundle("numbers").getNumberArray("ints").get(1));

			System.out.println(bundle.getBundle("numbers").getIntArray("ints")[1]);

			System.out.println(bundle.getBundle("numbers").getNumberStream("ints").collect(Collectors.averagingDouble(e->(Integer)e)));

			System.out.println(bundle.getObject(Color.class, "rgb"));

			for (int v : bundle.getBundle("numbers").getIntArray("ints"))
			{
				System.out.println(v);
			}

			for (double v : bundle.getBundle("numbers").getDoubleArray("doubles"))
			{
				System.out.println(v);
			}

			System.out.println(bundle);

//			System.out.println(new BundleX(bundle.toString()));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	static class Color implements BundlableX, BundlableValueX<Integer>
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
		public void readExternal(BundleX aBundle)
		{
			r = aBundle.getInt("r");
			g = aBundle.getInt("g");
			b = aBundle.getInt("b");
		}


		@Override
		public void writeExternal(BundleX aBundle)
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
}
