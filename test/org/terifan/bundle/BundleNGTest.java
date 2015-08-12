package org.terifan.bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class BundleNGTest
{
	@Test
	public void testExternalizable() throws IOException, ClassNotFoundException
	{
		Bundle bundle = createSimpleBundle();

		byte[] out = new BinaryEncoder().marshal(bundle);

		Bundle unbundled = new BinaryDecoder().unmarshal(out);

		assertEquals(bundle, unbundled);

		Log.hexDump(out);

//		System.out.println(new JSONEncoder().marshal(bundle));
	}


	static Bundle createSimpleBundle()
	{
		Random r = new Random();

		Bundle bundle = new Bundle()
			.putBoolean("boolean", r.nextBoolean())
			.putByte("byte", (byte)r.nextInt())
			.putShort("short", (short)r.nextInt())
			.putChar("char", (char)r.nextInt())
			.putInt("int", r.nextInt())
			.putLong("long", r.nextLong())
			.putFloat("float", r.nextFloat())
			.putDouble("double", r.nextDouble())
			.putDate("date", new Date(r.nextLong() & -1))
			.putString("string", "string")
			.putString("null", null)
			.putBundle("bundle", new Bundle()
				.putByte("one", r.nextInt())
				.putByte("two", r.nextInt())
			)
			.putIntArray("ints", r.nextInt(), r.nextInt(), r.nextInt())
			.putByteArray("bytes", (byte)r.nextInt(), (byte)r.nextInt(), (byte)r.nextInt())
			.putIntArrayList("intList", new ArrayList<>(Arrays.asList(r.nextInt(), r.nextInt(), r.nextInt())));
		return bundle;
	}
}