package org.terifan.bundle.io;

import java.io.IOException;
import java.io.OutputStream;


/**
 * BitOutputStream writes bits to the underlying byte stream.
 */
public class BitOutputStream implements AutoCloseable
{
	private OutputStream mOutputStream;
	private int mBitsToGo;
	private int mBitBuffer;
	private long mBitsWritten;


	public BitOutputStream(OutputStream aOutputStream)
	{
		mOutputStream = aOutputStream;
		mBitBuffer = 0;
		mBitsToGo = 8;
	}


	public void writeBit(boolean aBit) throws IOException
	{
		writeBit(aBit ? 1 : 0);
	}


	public void writeBit(int aBit) throws IOException
	{
		mBitBuffer |= aBit << --mBitsToGo;
		mBitsWritten++;

		if (mBitsToGo == 0)
		{
			mOutputStream.write(mBitBuffer & 0xFF);
			mBitBuffer = 0;
			mBitsToGo = 8;
		}
	}


	public void writeBits(int aValue, int aLength) throws IOException
	{
		if (aLength == 8 && mBitsToGo == 8)
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


	public void writeBits(long aValue, int aLength) throws IOException
	{
		if (aLength > 32)
		{
			writeBits((int)(aValue >>> 32), aLength - 32);
		}

		writeBits((int)aValue, Math.min(aLength, 32));
	}


	public void write(byte[] aBuffer) throws IOException
	{
		write(aBuffer, 0, aBuffer.length);
	}


	public void write(byte[] aBuffer, int aOffset, int aLength) throws IOException
	{
		if (mBitsToGo == 8)
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
		if (mBitsToGo < 8)
		{
			writeBits(0, mBitsToGo);
		}
	}


	public void writeExpGolomb(int val, int k) throws IOException
	{
		assert val >= 0 && val < (1<<30)-1;

		while (val >= (1 << k))
		{
			writeBit(1);
			val -= 1 << k;
			k++;
		}

		writeBit(0);

		while (k > 0)
		{
			k--;
			writeBit((val >>> k) & 1);
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