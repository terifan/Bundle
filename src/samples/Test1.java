package samples;

import java.util.Date;
import org.terifan.bundle.Array;
import org.terifan.bundle.Bundle;


public class Test1
{
	public static void main(String... args)
	{
		try
		{
//			Bundle bundle = new Bundle()
//				.putArray("positions", Array.of(new _Position(0,0,0), new _Position(0,0,1), new _Position(0,1,0), new _Position(0,1,1), new _Position(1,0,0), new _Position(1,0,1), new _Position(1,1,0), new _Position(1,1,1)))
//				.putArray("coordinates", Array.of(new _Vector(0,0,0), new _Vector(0,0,1), new _Vector(0,1,0), new _Vector(0,1,1), new _Vector(1,0,0), new _Vector(1,0,1), new _Vector(1,1,0), new _Vector(1,1,1)))
//				.putArray("colors", Array.of(new _RGB(0,0,0), new _RGB(0,0,1), new _RGB(0,1,0), new _RGB(0,1,1), new _RGB(1,0,0), new _RGB(1,0,1), new _RGB(1,1,0), new _RGB(1,1,1)))
//				.putBundle("indices", new Bundle().putArray("a",Array.of(0,1,2)).putArray("b",Array.of(0,2,3)).putArray("c",Array.of(4,5,6)).putArray("d",Array.of(4,6,7)))
//				.putNumber("PI", Math.PI)
//				.putBundlable("tri", new _Triangle(new _Vector[]{new _Vector(0,-1,0), new _Vector(1,0,0), new _Vector(-1,0,0)}, new _RGB[]{new _RGB(1, 0, 0), new _RGB(0, 1, 0), new _RGB(0, 0, 1)}));
//
//			Array array = new Array().add(bundle);
//
//			System.out.println(bundle);
//			System.out.println(array);
//			System.out.println(array.getBundle(0).getArray("array").getArray(2).toIntArrayList());
//			System.out.println(array.getBundle(0).getArray("array").getIntArrayList(2));

			_RGB c = new _RGB(1,2,3);
			_Vector v = new _Vector(1,2,3);
			_Triangle t = new _Triangle(new _Vector[]{new _Vector(0,1,2), new _Vector(3,4,5), new _Vector(6,7,8)}, new _RGB[]{new _RGB(10, 11, 12), new _RGB(13, 14, 15), new _RGB(16, 17, 18)});

			Bundle bun = new Bundle();
			bun.putBundlable("#col", c);
			bun.putBundlable("#vec", v);
			bun.putBundlable("#tri", t);

			System.out.println(bun);

			_Triangle u = bun.getBundlable("#tri", _Triangle.class);

			System.out.println(t);
			System.out.println(u);

			Array arr = new Array();
			arr.putBundlable(0, c);
			arr.putBundlable(1, v);
			arr.putBundlable(2, t);

			System.out.println(arr);

			_Triangle u2 = arr.getBundlable(2, _Triangle.class);

			System.out.println(u2);


//			Bundle bundle = new Bundle()
//				.putArray("positions", Array.of(new _Position(0,0,0), new _Position(0,0,1), new _Position(0,1,0), new _Position(0,1,1), new _Position(1,0,0), new _Position(1,0,1), new _Position(1,1,0), new _Position(1,1,1)))
//				.putArray("coordinates", Array.of(new _Vector(0,0,0), new _Vector(0,0,1), new _Vector(0,1,0), new _Vector(0,1,1), new _Vector(1,0,0), new _Vector(1,0,1), new _Vector(1,1,0), new _Vector(1,1,1)))
//				.putArray("colors", Array.of(new _RGB(0,0,0), new _RGB(0,0,1), new _RGB(0,1,0), new _RGB(0,1,1), new _RGB(1,0,0), new _RGB(1,0,1), new _RGB(1,1,0), new _RGB(1,1,1)))
//				.putBundle("indices", new Bundle().putArray("a",Array.of(0,1,2)).putArray("b",Array.of(0,2,3)).putArray("c",Array.of(4,5,6)).putArray("d",Array.of(4,6,7)))
//				.putNumber("PI", Math.PI);
//
//			String json = bundle.marshalJSON(false);
//
//			System.out.println(json);
//
//			bundle = new Bundle().unmarshalJSON(new String(json));
//
//			_Triangle tri = bundle.getBundlable(_Triangle.class, "tri");
//
//			System.out.println(new Bundle().putBundlable("xxx", tri).marshalJSON(false));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
