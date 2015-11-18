package org.terifan.bundle.bundle_test;

import org.terifan.bundle.BinaryEncoder;
import org.terifan.bundle.Bundle;


public class TestPrintHex
{
	public static void main(String ... args)
	{
		try
		{
			Bundle bundle = new Bundle()
				.putInt("article_id", 12345)
				.putString("name", "bunch of monkies")
				.putString("description", "cage filled with raging monkies")
				.putBundle("attributes", new Bundle()
					.putDoubleArray("cage_dimensions", 3.8, 2.5, 2.5)
				);

			BinaryEncoder binaryEncoder = new BinaryEncoder();
			Log.hexDump(binaryEncoder.marshal(bundle));
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
