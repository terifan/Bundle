package org.terifan.bundle;

import java.io.IOException;
import java.io.OutputStream;


class VLCOutputStream implements AutoCloseable
{
	private OutputStream mOutputStream;


	public VLCOutputStream(OutputStream aOutputStream)
	{
		mOutputStream = aOutputStream;
	}


	public void writeInt8(int aValue) throws IOException
	{
		mOutputStream.write(aValue);
	}


	public void write(byte[] aBuffer) throws IOException
	{
		write(aBuffer, 0, aBuffer.length);
	}


	public void write(byte[] aBuffer, int aOffset, int aLength) throws IOException
	{
		mOutputStream.write(aBuffer, aOffset, aLength);
	}


	public void writeVar32S(long aValue) throws IOException
	{
		writeVar32((aValue << 1) ^ (aValue >> 31));
	}


	public void writeVar32(long aValue) throws IOException
	{
		for (;;)
		{
			int b = (int)(aValue & 127);
			aValue >>>= 7;

			if (aValue == 0)
			{
				writeInt8(b);
				break;
			}

			writeInt8(128 + b);
		}
	}


	public void writeVar64S(long aValue) throws IOException
	{
		writeVar64((aValue << 1) ^ (aValue >> 63));
	}


	public void writeVar64(long aValue) throws IOException
	{
		for (;;)
		{
			int b = (int)(aValue & 127);
			aValue >>>= 7;

			if (aValue == 0)
			{
				writeInt8(b);
				break;
			}

			writeInt8(128 + b);
		}
	}


	public void writeInt32(int aValue) throws IOException
	{
		mOutputStream.write(0xff & (aValue >>> 24));
		mOutputStream.write(0xff & (aValue >>> 16));
		mOutputStream.write(0xff & (aValue >>> 8));
		mOutputStream.write(0xff & (aValue >>> 0));
	}


	public void writeInt64(long aValue) throws IOException
	{
		writeInt32((int)(aValue));
		writeInt32((int)(aValue >>> 32));
	}


	@Override
	public void close() throws IOException
	{
		if (mOutputStream != null)
		{
			mOutputStream.close();
			mOutputStream = null;
		}
	}
}