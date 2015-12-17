package org.terifan.bundle;

import java.io.IOException;
import java.io.OutputStream;


/**
 * BitOutputStream writes bits to the underlying byte stream.
 */
class BitOutputStream implements AutoCloseable
{
	private OutputStream mOutputStream;
	private int mShift;
	private int mBuffer;


	public BitOutputStream(OutputStream aOutputStream)
	{
		mOutputStream = aOutputStream;
		mBuffer = 0;
		mShift = 8;
	}


	public void writeBit(boolean aBit) throws IOException
	{
		writeBit(aBit ? 1 : 0);
	}


	public void writeBit(int aBit) throws IOException
	{
		mBuffer |= aBit << --mShift;

		if (mShift == 0)
		{
			mOutputStream.write(mBuffer & 0xFF);
			mBuffer = 0;
			mShift = 8;
		}
	}


	public void writeBits(int aValue, int aLength) throws IOException
	{
		if (aLength == 8 && mShift == 8)
		{
			mOutputStream.write(aValue);
		}
		else
		{
			while (aLength-- > 0)
			{
				writeBit((aValue >>> aLength) & 1);
			}
		}
	}


	public void write(byte[] aBuffer) throws IOException
	{
		write(aBuffer, 0, aBuffer.length);
	}


	public void write(byte[] aBuffer, int aOffset, int aLength) throws IOException
	{
		if (mShift == 8)
		{
			mOutputStream.write(aBuffer, aOffset, aLength);
		}
		else
		{
			while (aLength-- > 0)
			{
				writeBits(aBuffer[aOffset++] & 0xFF, 8);
			}
		}
	}


	public void finish() throws IOException
	{
		align();
	}


	@Override
	public void close() throws IOException
	{
		if (mOutputStream != null)
		{
			finish();

			mOutputStream.close();
			mOutputStream = null;
		}
	}


	public void align() throws IOException
	{
		if (mShift < 8)
		{
			writeBits(0, mShift);
		}
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
				writeBits(b, 8);
				break;
			}

			writeBits(128 + b, 8);
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
				writeBits(b, 8);
				break;
			}

			writeBits(128 + b, 8);
		}
	}


	public void writeExpGolomb(int aValue, int k) throws IOException
	{
		assert aValue >= 0 && aValue < (1<<30)-1;

		while (aValue >= (1 << k))
		{
			writeBit(1);
			aValue -= 1 << k;
			k++;
		}

		writeBit(0);

		while (k > 0)
		{
			k--;
			writeBit((aValue >>> k) & 1);
		}
	}


	public void writeUnary(int aSymbol) throws IOException
	{
		assert aSymbol >= 0;

		int l = aSymbol;
		while (l-- > 0)
		{
			writeBit(0);
		}
		writeBit(1);
	}
}