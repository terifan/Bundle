package org.terifan.bundle.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.terifan.bundle.bundle_test.Log;
import org.terifan.bundle.io.BitInputStream;
import org.terifan.bundle.io.BitOutputStream;


public class HuffmanTest 
{
	public static void main(String... args)
	{
		try
		{
			int[] lengths;
			byte[] buffer;

			{
				Huffman tree = new Huffman(16).buildTree(42,15,0,10,8,20,23,48,0,9,16,21,5,7,0,18);

				lengths = tree.extractLengths();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (BitOutputStream bos = new BitOutputStream(baos))
				{
					tree.encode(bos, 7);
					tree.encode(bos, 2);
					tree.encode(bos, 0);
					tree.encode(bos, 4);
					tree.encode(bos, 1);
				}

				Log.out.println(tree);

				buffer = baos.toByteArray();
			}

			for (int i : lengths) Log.out.print(i+",");
			Log.out.println();

			{
				Huffman tree = new Huffman(lengths.length).reconstructTree(lengths);

				BitInputStream bis = new BitInputStream(new ByteArrayInputStream(buffer));
				for (int i = 0; i < 5; i++)
				{
					Log.out.println(tree.decode(bis).getSymbol());
				}
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
