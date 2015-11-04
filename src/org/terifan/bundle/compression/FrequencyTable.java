package org.terifan.bundle.compression;

import java.util.Random;
import org.terifan.bundle.bundle_test.Log;


public class FrequencyTable
{
	private final static int MAX_CUMULATIVE_FREQUENCY = Integer.MAX_VALUE;

	private final int mSymbolCount;
	private final int[] mCharToSymbol;
	private final int[] mSymbolToChar;
	private final int[] mSymbolFreq;
	private final int[] mSymbolCum;


	public FrequencyTable(int aSymbolCount)
	{
		mSymbolCount = aSymbolCount;

		mCharToSymbol = new int[mSymbolCount];
		mSymbolToChar = new int[mSymbolCount + 1];
		mSymbolFreq = new int[mSymbolCount + 1];
		mSymbolCum = new int[mSymbolCount + 1];

		mSymbolCum[mSymbolCount] = 0;
		for (int symbol = mSymbolCount; symbol >= 1; symbol--)
		{
			int character = symbol - 1;
			mCharToSymbol[character] = symbol;
			mSymbolToChar[symbol] = character;
			mSymbolFreq[symbol] = 1;
			mSymbolCum[symbol - 1] = mSymbolCum[symbol] + mSymbolFreq[symbol];
		}

		mSymbolFreq[0] = 0;
	}


	private void inc(int aSymbol)
	{
		if (mSymbolCum[0] >= MAX_CUMULATIVE_FREQUENCY)
		{
			int c = 0;
			for (int i = mSymbolCount; i > 0; i--)
			{
				mSymbolCum[i] = c;
				int j = (mSymbolFreq[i] + 1) >> 1;
				mSymbolFreq[i] = j;
				c += j;
			}
			mSymbolCum[0] = c;
		}

		int i;
		for (i = aSymbol; mSymbolFreq[i] == mSymbolFreq[i - 1]; i--)
		{
		}

		if (i < aSymbol)
		{
			int ch_i = mSymbolToChar[i];
			int ch_sym = mSymbolToChar[aSymbol];
			mSymbolToChar[i] = ch_sym;
			mSymbolToChar[aSymbol] = ch_i;
			mCharToSymbol[ch_i] = aSymbol;
			mCharToSymbol[ch_sym] = i;
		}

		mSymbolFreq[i]++;

		while (--i >= 0)
		{
			mSymbolCum[i]++;
		}

//		Log.out.println(this+" "+mSymbolCum[0]);
	}


	public int encode(int aChar)
	{
		int symbol = mCharToSymbol[aChar];
		inc(symbol);
		return symbol - 1;
	}


	public int decode(int aSymbol)
	{
		int chr = mSymbolToChar[aSymbol + 1];
		inc(aSymbol + 1);
		return chr;
	}


	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder("{");
		for (int i = 1; i <= mSymbolCount; i++)
		{
			if (i > 1)
			{
				s.append(", ");
			}
			s.append(mSymbolToChar[i] + "=" + mSymbolFreq[i]);
		}
		s.append("}");
		return s.toString();
	}


	public static void main(String ... args)
	{
		try
		{
			Random r = new Random(1);

			int n = 10;

			int[] input = new int[1000];
			int[] data = new int[input.length];
			for (int i = 0; i < input.length; i++)
			{
				input[i] = r.nextInt(n);
			}

			FrequencyTable ctx = new FrequencyTable(n);

//			long t = System.nanoTime();
			for (int i = 0; i < input.length; i++)
			{
				data[i] = ctx.encode(input[i]);
			}
//			Log.out.println((System.nanoTime()-t)/1000000.0);

			Log.out.println("---------------------------------------------------------------");

			ctx = new FrequencyTable(n);
			for (int i = 0; i < input.length; i++)
			{
				int out = ctx.decode(data[i]);
				if (out != input[i])
				{
					Log.out.println(out+" != "+input[i]);
					break;
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}