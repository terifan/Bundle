package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;


class VLCInputStream implements AutoCloseable
{
	private InputStream mInputStream;


	public VLCInputStream(InputStream aInputStream) throws IOException
	{
		mInputStream = aInputStream;
	}


	public int readInt8() throws IOException
	{
		return mInputStream.read();
	}


	public byte[] read(byte[] aBuffer) throws IOException
	{
		mInputStream.read(aBuffer);
		return aBuffer;
	}


	public byte[] read(byte[] aBuffer, int aOffset, int aLength) throws IOException
	{
		mInputStream.read(aBuffer, aOffset, aLength);
		return aBuffer;
	}


	public void skip(int aCount) throws IOException
	{
		mInputStream.skip(aCount);
	}


	public int readVar32S() throws IOException
	{
		int result = readVar32();

		return (result >>> 1) ^ -(result & 1);
	}


	public int readVar32() throws IOException
	{
		for (int n = 0, value = 0; n < 32; n+=7)
		{
			int b = readInt8();
			value += (b & 127) << n;
			if (b < 128)
			{
				return value;
			}
		}

		throw new IllegalStateException("Variable int32 exceeds maximum length");
	}


	public long readVar64S() throws IOException
	{
		long result = readVar64();

		return (result >>> 1) ^ -(result & 1);
	}


	public long readVar64() throws IOException
	{
		for (long n = 0, value = 0; n < 64; n+=7)
		{
			int b = readInt8();
			value += (long)(b & 127) << n;
			if (b < 128)
			{
				return value;
			}
		}

		throw new IllegalStateException("Variable int64 exceeds maximum length");
	}


	public int readInt32() throws IOException
	{
		return (readInt8() << 24) | (readInt8() << 16) + (readInt8() << 8) + readInt8();
	}


	public long readInt64() throws IOException
	{
		return (readInt32() & 0xFFFFFFFFL) | ((readInt32() & 0xFFFFFFFFL) << 32);
	}


	@Override
	public void close() throws IOException
	{
		if (mInputStream != null)
		{
			mInputStream.close();
			mInputStream = null;
		}
	}
}