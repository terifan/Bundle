package org.terifan.bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public class ByteBufferOutputStream extends OutputStream
{
	private ByteBuffer mByteBuffer;


	public ByteBufferOutputStream(ByteBuffer aByteBuffer)
	{
		mByteBuffer = aByteBuffer;
	}


	@Override
	public void write(byte[] b) throws IOException
	{
		mByteBuffer.put(b);
	}


	@Override
	public void write(int b) throws IOException
	{
		mByteBuffer.put((byte)b);
	}
}
