package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import samples.Log;


public class BinaryEncoderNGTest
{
	@Test
	public void testMarshalObject() throws IOException
	{
		byte[] content = new byte[9000];
		new Random().nextBytes(content);

		Bundle out = new Bundle();
		out.putSerializable("object", new Preview(new Bundle().putString("name", "Adam"), content));

		byte[] data = out.marshal();

		Bundle in = new Bundle().unmarshal(data);
		Preview p = in.getSerializable(Preview.class, "object");

		assertEquals(in, out);
		assertEquals(p.getContent(), content);
	}



	@Test
	public void testJavaObjectOutput() throws IOException, ClassNotFoundException
	{
		byte[] content = new byte[9000];
		new Random().nextBytes(content);

		Bundle out = new Bundle();
		out.putSerializable("object", new Preview(new Bundle().putString("name", "Adam"), content));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			oos.writeObject(out);
		}

		byte[] data = baos.toByteArray();

		Bundle in;
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data)))
		{
			in = (Bundle)ois.readObject();
		}

		Preview p = in.getSerializable(Preview.class, "object");

		assertEquals(in, out);
		assertEquals(p.getContent(), content);
	}
}
