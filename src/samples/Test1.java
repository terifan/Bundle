package samples;

import org.terifan.bundle.Array;
import org.terifan.bundle.Bundle;


public class Test1
{
	public static void main(String... args)
	{
		try
		{
			Bundle bundle = new Bundle()
				.putArray("positions", Array.of(new _Position(0,0,0), new _Position(0,0,1), new _Position(0,1,0), new _Position(0,1,1), new _Position(1,0,0), new _Position(1,0,1), new _Position(1,1,0), new _Position(1,1,1)))
				.putArray("coordinates", Array.of(new _Vector(0,0,0), new _Vector(0,0,1), new _Vector(0,1,0), new _Vector(0,1,1), new _Vector(1,0,0), new _Vector(1,0,1), new _Vector(1,1,0), new _Vector(1,1,1)))
				.putArray("colors", Array.of(new _RGB(0,0,0), new _RGB(0,0,1), new _RGB(0,1,0), new _RGB(0,1,1), new _RGB(1,0,0), new _RGB(1,0,1), new _RGB(1,1,0), new _RGB(1,1,1)))
				.putBundle("indices", new Bundle().putArray("a",Array.of(0,1,2)).putArray("b",Array.of(0,2,3)).putArray("c",Array.of(4,5,6)).putArray("d",Array.of(4,6,7)))
				.putNumber("PI", Math.PI)
				.putBundlable("tri", new _Triangle(new _Vector(0,-1,0), new _Vector(1,0,0), new _Vector(-1,0,0)));

			String json = bundle.marshalJSON(false);

			System.out.println(json);

			bundle = new Bundle().unmarshalJSON(new String(json));

			_Triangle tri = bundle.getBundlable(_Triangle.class, "tri");

			System.out.println(new Bundle().putBundlable("xxx", tri).marshalJSON(false));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
