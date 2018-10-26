package samples;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.terifan.bundle2.BinaryDecoderX;
import org.terifan.bundle2.BinaryDecoderX.PathEvaluation;
import org.terifan.bundle2.BinaryEncoderX;
import org.terifan.bundle2.BundlableValueX;
import org.terifan.bundle2.BundlableX;
import org.terifan.bundle2.BundleX;
import org.terifan.bundle2.BundleX.BundleArray;


public class Test
{
	public static void main(String... args)
	{
		try
		{
			BundleX bundle = new BundleX()
				.putBundle("numbers", new BundleX()
					.putNumber("number", 7)
					.putArray("ints", new BundleArray().add(1, 4, 9))
					.putArray("doubles", new BundleArray().add(1.3).add(2.2).add(3.1))
				)
				.putNumber("byte", (byte)7)
				.putNumber("short", (short)7777)
				.putNumber("int", 654984)
				.putNumber("long", 164516191981L)
				.putNumber("float", 7.2f)
				.putNumber("double", 3.14)
				.putArray("arrays", new BundleArray().add("horse", new BundleArray().add("monkey", "pig"), 777, new BundleArray().add("girl", "boy")))
				.putArray("strings", new BundleArray().add("a", null).add("b").add("c"))
				.putString("null", null)
				.putBoolean("boolean", true)
				.putArray("booleans", new BundleArray().add(true).add(false).add(true))
				.putArray("bundles", new BundleArray().add(new BundleX().putString("key", "value")))
				.putBundle("bundle", new BundleX().putString("key", "value"))
				.putString("string", "text")
				.putBundle("color", new Color(196,128,20))
				.putArray("colors", new BundleArray().add(new Color(196,128,20), new Color(96,128,220)))
				.putObject("rgb", new Color(196,128,20))
				.putArray("rgbs", new BundleArray().add(new Color(196,128,20), new Color(96,128,220)))
				.putObject("values", new PackedArray(96,128,220))
				.putSerializable("date", new Date())
			;

			System.out.println(bundle);

			System.out.println(bundle.getBundle("numbers").getArray("ints").get(1));
			System.out.println(bundle.getBundle("numbers").toArray("ints")[1]);
			System.out.println(bundle.getBundle("numbers").getArray("ints").stream().collect(Collectors.averagingDouble(e->(Integer)e)));
			System.out.println(bundle.getArray("strings").stream().collect(Collectors.averagingDouble(e->e==null?0:e.toString().length())));
			System.out.println(bundle.getSerializable(Date.class, "date"));

			Color color = bundle.getObject(Color.class, "rgb");
			System.out.println(color);

			PackedArray pa = bundle.getObject(PackedArray.class, "values");
			System.out.println(pa);

			for (Object v : bundle.getArray("colors"))
			{
				System.out.println(v);
			}

			for (Color v : bundle.getObjectArray(Color.class, "colors"))
			{
				System.out.println(v);
			}

			System.out.println();

			byte[] data = new BinaryEncoderX().marshal(bundle);

			System.out.println(new String(data).replace('\n', '-').replace('\r', '-').replace('\t', '-').replace('\0', '-'));
			System.out.println();

//			PathEvaluation path = new PathEvaluation("colors", 1);
//			PathEvaluation path = new BinaryDecoderX.PathEvaluation();
			PathEvaluation path = new PathEvaluation("arrays", 1, 1);

			BundleX b = new BinaryDecoderX().unmarshal(new ByteArrayInputStream(data), path);
			System.out.println(b);

////			baos.reset();
////			DeflaterOutputStream dos = new DeflaterOutputStream(baos);
////			dos.write(bundle.toString().getBytes("utf-8"));
////			dos.close();
//
//			System.out.println(new String(baos.toByteArray()).replace('\n', '-').replace('\r', '-').replace('\t', '-').replace('\0', '-'));
//
//			for (int v : bundle.getBundle("numbers").getIntArray("ints"))
//			{
//				System.out.println(v);
//			}
//
//			for (double v : bundle.getBundle("numbers").getDoubleArray("doubles"))
//			{
//				System.out.println(v);
//			}

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


	static class PackedArray implements BundlableValueX<String>
	{
		private int[] mValues;


		public PackedArray()
		{
		}


		public PackedArray(int... aValues)
		{
			mValues = aValues;
		}


		@Override
		public void readExternal(String aValue)
		{
			mValues = Stream.of(aValue.split(" ")).mapToInt(Integer::parseInt).toArray();
		}


		@Override
		public String writeExternal()
		{
			return Arrays.stream(mValues).mapToObj(Integer::toString).collect(Collectors.joining(" "));
		}


		@Override
		public String toString()
		{
			return "PackedArray{mValues=" + Arrays.toString(mValues) + '}';
		}
	}
}
