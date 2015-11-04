package org.terifan.bundle;

import org.terifan.bundle.bundle_test.Log;

public class HuffmanTree 
{
	public HuffmanTree()
	{
	}


	public static int[][] construct(int... aSuffixLengths)
	{
		int symbolCount = 0;
		int levels = aSuffixLengths.length - 1;
		for (int i = 0; i <= levels; i++)
		{
			symbolCount += 1 << aSuffixLengths[i];
		}
		
//		Log.out.println(symbolCount);

		int[][] huffman = new int[symbolCount][2];

		for (int i = 0, k = 0; i <= levels; i++)
		{
			int prefixLen = i == levels ? i : i + 1;
			int prefix = (i == levels ? (1 << i) - 1 : (2 << i) - 2) << aSuffixLengths[i];

			for (int j = 0; j < 1 << aSuffixLengths[i]; j++ ,k++)
			{
				huffman[k][0] = prefix + j;
				huffman[k][1] = (prefixLen + aSuffixLengths[i]);

//				String s = Integer.toString(huffman[k][0], 2);
//				Log.out.printf("%3d %s%s\n", k, "00000000".substring(0, huffman[k][1]-s.length()), s);
			}
		}
		
		return huffman;
	}
}
