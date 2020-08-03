package samples;

import org.terifan.bundle.Array;
import org.terifan.bundle.Bundle;


public class Test2
{
	public static void main(String... args)
	{
		try
		{
			Bundle bundle = new Bundle().unmarshalJSON("{'a':100, 'b':'string', 'c':.14, 'd':0xffffff, 'e':true}");
			Array array = new Array().unmarshalJSON("[100, 'string', .14, 0xffffff, true]");

			System.out.println(bundle);
			System.out.println(array);

			System.out.println(bundle.get("a") + " " + bundle.getInt("a") + " " + array.getInt(0));
			System.out.println(bundle.get("b") + " " + bundle.getString("b") + " " + array.getString(1));
			System.out.println(bundle.get("c") + " " + bundle.getDouble("c") + " " + array.getDouble(2));
			System.out.println(bundle.get("c") + " " + bundle.getFloat("c") + " " + array.getFloat(2));
			System.out.println(bundle.get("c") + " " + bundle.getInt("c") + " " + array.getInt(2));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
