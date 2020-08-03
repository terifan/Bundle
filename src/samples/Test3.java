package samples;

import org.terifan.bundle.Array;
import org.terifan.bundle.Bundle;


public class Test3
{
	public static void main(String... args)
	{
		try
		{
			int[][] colors = {{1,2,3},{4,5}};
			int[][] texcoords = {{6,7},{},null,{8,9}};

			Bundle bundle = new Bundle();

			bundle.putArray("mColorIndices", Array.of(colors));
			bundle.putArray("mTextureCoordinateIndices", Array.of(texcoords));

			System.out.println(bundle);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
