package samples;

import org.terifan.bundle.Bundle;


public class Test11
{
	public static void main(String... args)
	{
		try
		{
			System.out.println(new Bundle().putBundlable("test", new _Vector(1,2,3)).marshalJSON(true));
			System.out.println(new Bundle().putBundlable("test", new _RGB(1,2,3)).marshalJSON(true));
			System.out.println(new Bundle().putBundlable("test", new _Position(1,2,3)).marshalJSON(true));
			System.out.println(new Bundle().putBundlable("test", new _Triangle(new _Vector(1,2,3),new _Vector(4,5,6),new _Vector(7,8,9))).marshalJSON(true));

			byte[] data = new Bundle().putBundlable("test", new _Triangle(new _Vector(1,2,3),new _Vector(4,5,6),new _Vector(7,8,9))).marshal();
			Log.hexDump(data);


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
