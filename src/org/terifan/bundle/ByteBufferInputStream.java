package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class ByteBufferInputStream extends InputStream
{
	private ByteBuffer mByteBuffer;


	public ByteBufferInputStream(ByteBuffer aByteBuffer)
	{
		mByteBuffer = aByteBuffer;
	}


	@Override
	public int read() throws IOException
	{
		return 0xff & mByteBuffer.get();
	}


	@Override
	public int read(byte[] b) throws IOException
	{
		mByteBuffer.get(b);
		return b.length;
	}


	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		mByteBuffer.get(b,off,len);
		return len;
	}
}
