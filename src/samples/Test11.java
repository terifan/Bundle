package samples;

import java.io.FileInputStream;
import org.terifan.bundle.Bundle;


public class Test11
{
	public static void main(String... args)
	{
		try
		{
//			System.out.println(new Bundle().putBundlable("test", new _Vector(1,2,3)).marshalJSON(true));
//			System.out.println(new Bundle().putBundlable("test", new _RGB(1,2,3)).marshalJSON(true));
//			System.out.println(new Bundle().putBundlable("test", new _Position(1,2,3)).marshalJSON(true));
//			System.out.println(new Bundle().putBundlable("test", new _Triangle(new _Vector[]{new _Vector(1,2,3),new _Vector(4,5,6),new _Vector(7,8,9)}, new _RGB[]{new _RGB(1, 0, 0), new _RGB(0, 1, 0), new _RGB(0, 0, 1)})).marshalJSON(true));
//
//			byte[] data = new Bundle().putBundlable("test", new _Triangle(new _Vector[]{new _Vector(1,2,3),new _Vector(4,5,6),new _Vector(7,8,9)}, new _RGB[]{new _RGB(1, 0, 0), new _RGB(0, 1, 0), new _RGB(0, 0, 1)})).marshal();
//			Log.hexDump(data);
//
//
//			Bundle bundle = new Bundle().unmarshal(new FileInputStream(""));
//
//			_Triangle t = bundle.getBundleArrayList("triangles").get(0).getBundlable(_Triangle.class, "test");



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
