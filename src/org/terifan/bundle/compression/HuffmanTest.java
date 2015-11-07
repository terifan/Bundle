package org.terifan.bundle.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import org.terifan.bundle.bundle_test.Log;
import org.terifan.bundle.compression.Huffman.Symbol;
import org.terifan.bundle.io.BitInputStream;
import org.terifan.bundle.io.BitOutputStream;


public class HuffmanTest 
{
	public static void main(String... args)
	{
		try
		{
			Random r = new Random(1);
//			int[] valid = {0,1,7,15};
			int[] valid = {0,1,3,4,5,6,7,9,10,11,12,13,15};
			int[] input = new int[1000000];
			for (int i = 0; i < input.length; i++)
			{
				input[i] = valid[r.nextInt(valid.length)];
			}

			int[] lengths;
			byte[] buffer;
			int[][] codebook;

			{
//				Huffman tree = new Huffman(16).buildTree(42,15,0,0,0,0,0,48,0,0,0,0,0,0,0,18);
				Huffman tree = new Huffman(16).buildTree(42,15,0,10,8,20,23,48,0,9,16,21,5,7,0,18);

				lengths = tree.extractLengths();
				codebook = tree.extractCodebook();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (BitOutputStream bos = new BitOutputStream(baos))
				{
					for (int i : input)
					{
						tree.encode(bos, i);
					}
				}

//				Log.out.println(tree);

				buffer = baos.toByteArray();
			}

			Log.out.println("---------------------------");
			for (int i : lengths) Log.out.print(i+",");
			Log.out.println();
			Log.out.println("---------------------------");
			Log.out.print("(");
			for (int i : codebook[0])
			{
				Log.out.print(i+",");
			}
			Log.out.print("),(");
			for (int i : codebook[1])
			{
				Log.out.print(i+",");
			}
			Log.out.println(")");
			Log.out.println("---------------------------------");

			{
//				Huffman tree = new Huffman(lengths.length).reconstructTree(lengths);

				Huffman tree = new Huffman(lengths.length).reconstructTree(codebook);

				Log.out.println(tree);

				BitInputStream bis = new BitInputStream(new ByteArrayInputStream(buffer));
				for (int i : input)
				{
					Symbol symbol = tree.decode(bis);
					if (symbol.getSymbol() != i)
					{
						Log.out.println("ERROR " + symbol.getSymbol() + " != " + i);
						break;
					}
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
