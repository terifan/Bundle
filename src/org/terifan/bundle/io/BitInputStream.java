package org.terifan.bundle.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * BitInputStream allow reading bits from the underlying stream.
 */
public class BitInputStream //extends InputStream
{
	private InputStream mInputStream;
	private int mBitBuffer;
	private int mBitCount;
	private boolean mEOF;


	public BitInputStream(InputStream aInputStream) throws IOException
	{
		mInputStream = aInputStream;
	}


	public int readBit() throws IOException
	{
		if (mBitCount == 0)
		{
			mBitBuffer = mInputStream.read();

			if (mBitBuffer == -1)
			{
				mEOF = true;
				throw new IOException("Premature end of stream");
			}

			mBitCount = 8;
		}

		mBitCount--;
		int output = 1 & (mBitBuffer >> mBitCount);
		mBitBuffer &= (1L << mBitCount) - 1;

		return output;
	}


	public int readBits(int aCount) throws IOException
	{
		assert aCount <= 24;

		int output = 0;

		while (aCount > mBitCount)
		{
			aCount -= mBitCount;
			output |= mBitBuffer << aCount;
			mBitBuffer = mInputStream.read();
			mBitCount = 8;

			if (mBitBuffer == -1)
			{
				mBitCount = 0;
				mEOF = true;
				throw new IOException("Premature end of stream");
			}
		}

		if (aCount > 0)
		{
			mBitCount -= aCount;
			output |= mBitBuffer >> mBitCount;
			mBitBuffer &= (1 << mBitCount) - 1;
		}

		return output;
	}


	public int peekBits(int aCount) throws IOException
	{
		while (mBitCount < aCount)
		{
			int i = mInputStream.read();

			if (i == -1)
			{
				i = 0;
			}

			mBitBuffer <<= 8;
			mBitBuffer += i;
			mBitCount += 8;
		}

		return mBitBuffer >>> (mBitCount - aCount);
	}


	public int read(byte[] aBuffer) throws IOException
	{
		return read(aBuffer, 0, aBuffer.length);
	}


	public int read(byte[] aBuffer, int aOffset, int aLength) throws IOException
	{
		if (mBitCount == 0)
		{
			return mInputStream.read(aBuffer, aOffset, aLength);
		}

		int len = 0;

		try
		{
			while (aLength-- > 0)
			{
				aBuffer[aOffset++] = (byte)readBits(8);
				len++;
			}
		}
		catch (IOException e)
		{
			if (!mEOF)
			{
				throw e;
			}
		}

		return len;
	}


	public void skipBits(int n) throws IOException
	{
		for (int i = 0; i < n; i++)
		{
			readBit();
		}
	}


	public void align() throws IOException
	{
		while (mBitCount > 0)
		{
			readBit();
		}
	}


	public int getBitCount()
	{
		return mBitCount;
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
			int b = (int)readBits(8);
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
			long b = readBits(8);
			value += (b & 127) << n;
			if (b < 128)
			{
				return value;
			}
		}

		throw new IllegalStateException("Variable int64 exceeds maximum length");
	}
}