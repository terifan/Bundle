package samples;

import org.terifan.bundle.Bundle;


public class Test11
{
	public static void main(String... args)
	{
		try
		{
			System.out.println(new Bundle().putBundlable("test", new Vector(1,2,3)).marshalJSON(true));
			System.out.println(new Bundle().putBundlable("test", new RGB(1,2,3)).marshalJSON(true));
			System.out.println(new Bundle().putBundlable("test", new Position(1,2,3)).marshalJSON(true));
			System.out.println(new Bundle().putBundlable("test", new Triangle(new Vector(1,2,3),new Vector(4,5,6),new Vector(7,8,9))).marshalJSON(!true));

//			Triangle out = new Triangle(new Vector(0,-1,0), new Vector(1,0,0), new Vector(-1,0,0));
//
//			Bundle bundle = new Bundle().putBundlable("tri", out);
//
//			String json = bundle.marshalJSON(false);
//
//			System.out.println(json);
//
//			bundle = new Bundle().unmarshalJSON(new String(json));
//
//			Triangle in = bundle.getBundlable(Triangle.class, "tri");
//
//			System.out.println(in.equals(out));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
