package org.terifan.bundle.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.terifan.bundle.BitInputStream;
import org.terifan.bundle.BitOutputStream;
import org.terifan.bundle.bundle_test.Log;


public class HuffmanTree 
{
	private final static Comparator<? super Node> mFrequencySorter = (e,f)->Integer.compare(f.mFrequency, e.mFrequency);
	private final static Comparator<? super Node> mLengthSymbolSorter = (e,f)->e.mLength == f.mLength ? Integer.compare(e.mSymbol, f.mSymbol) : Integer.compare(e.mLength, f.mLength);
	private final static Comparator<? super Node> mSymbolSorter = (e,f)->Integer.compare(e.mSymbol, f.mSymbol);


	public HuffmanTree()
	{
	}


	private Node[] buildTree(int... aFrequencies)
	{
		int len = aFrequencies.length;

		Node[] nodes = new Node[len];
		for (int i = 0; i < len; i++)
		{
			nodes[i] = new Node(aFrequencies[i], i);
		}

		Node[] original = nodes.clone();

		Arrays.sort(nodes, mFrequencySorter);

		while (len > 1)
		{
			Node left = nodes[len - 2];
			Node right = nodes[len - 1];
			int newFreq = left.mFrequency + right.mFrequency;

			len--;
			int i = len;
			for (; i > 0 && nodes[i - 1].mFrequency < newFreq; i--)
			{
			}

			System.arraycopy(nodes, i, nodes, i + 1, len - i);
			nodes[i] = new Node(newFreq, left, right);
		}

		update(nodes[0], 0);

		return reconstructTreeImpl(original);
	}


	public Node[] reconstructTree(int... aLengths)
	{
		Node[] nodes = new Node[aLengths.length];
		for (int i = 0; i < aLengths.length; i++)
		{
			nodes[i] = new Node(i, 0, aLengths[i]);
		}
		
		return reconstructTreeImpl(nodes);
	}


	private Node[] reconstructTreeImpl(Node[] aNodes)
	{
		Arrays.sort(aNodes, mLengthSymbolSorter);
		
		aNodes[0].mCode = 0;
		for (int i = 0, j = 1; j < aNodes.length; j++)
		{
			i = (i + 1) << (aNodes[j].mLength - aNodes[j - 1].mLength);

			aNodes[j].mCode = i;
		}

		Arrays.sort(aNodes, mSymbolSorter);

		return aNodes;
	}


	/**
	 * Update length of the leaf nodes (actual symbols) and also remove references to child nodes from interior nodes so they can be garbage collected.
	 */
	private void update(Node aNode, int aLength)
	{
		aNode.mLength = aLength;
		if (aNode.mLeft != null)
		{
			update(aNode.mLeft, aLength + 1);
			aNode.mLeft = null;
		}
		if (aNode.mRight != null)
		{
			update(aNode.mRight, aLength + 1);
			aNode.mRight = null;
		}
	}


	private int[] extractLengths(Node[] aNodes)
	{
		int[] lengths = new int[aNodes.length];
		for (int i = 0; i < aNodes.length; i++)
		{
			lengths[i] = aNodes[i].mLength;
		}

		return lengths;
	}
	
	
	public int[] createDecoderLookupTable(Node[] aNodes, int aMaxCodeSize)
	{
		int[] alphabet = new int[1 << aMaxCodeSize];

		for (int i = 0, symbol = 0; i < alphabet.length; symbol++)
		{
			int length = aNodes[symbol].mLength;
			int code = aNodes[symbol].mCode << (aMaxCodeSize - length);

			for (int k = 0, sz = 1 << (aMaxCodeSize - length); k < sz; k++, i++)
			{
				alphabet[k + code] = symbol;
			}
		}
		
		return alphabet;
	}
	
	
	public static class Node
	{
		private int mSymbol;
		private int mFrequency;
		private Node mLeft;
		private Node mRight;
		private int mCode;
		private int mLength;

		private Node(int aFrequency, int aSymbol)
		{
			mFrequency = aFrequency;
			mSymbol = aSymbol;
		}

		private Node(int aSymbol, int aCode, int aLength)
		{
			mSymbol = aSymbol;
			mCode = aCode;
			mLength = aLength;
		}

		private Node(int aFrequency, Node aLeft, Node aRight)
		{
			mFrequency = aFrequency;
			mLeft = aLeft;
			mRight = aRight;
		}


		public int getSymbol()
		{
			return mSymbol;
		}


		public int getCode()
		{
			return mCode;
		}


		public int getLength()
		{
			return mLength;
		}
		
		
		public void writeTo(BitOutputStream aBitOutputStream) throws IOException
		{
			aBitOutputStream.writeBits(mCode, mLength);
		}
	}


	public static int[][] constructSuffixTree(int... aSuffixLengths)
	{
		int symbolCount = 0;
		int levels = aSuffixLengths.length - 1;
		for (int i = 0; i <= levels; i++)
		{
			symbolCount += 1 << aSuffixLengths[i];
		}
		
		int[][] huffman = new int[symbolCount][2];

		for (int i = 0, k = 0; i <= levels; i++)
		{
			int prefixLen = i == levels ? i : i + 1;
			int prefix = (i == levels ? (1 << i) - 1 : (2 << i) - 2) << aSuffixLengths[i];

			for (int j = 0; j < 1 << aSuffixLengths[i]; j++ ,k++)
			{
				huffman[k][0] = prefix + j;
				huffman[k][1] = (prefixLen + aSuffixLengths[i]);
			}
		}
		
		return huffman;
	}
	
	
	public static void main(String... args)
	{
		try
		{
			HuffmanTree tree = new HuffmanTree();
//			Node[] nodes = tree.buildTree(2,4,1,2);
//			Node[] nodes = tree.buildTree(6,3,7,6,10,6,15,5);
			Node[] nodes = tree.buildTree(42,15,10,8,20,23,48,9,16,21,5,7,18);

			for (Node node : nodes)
			{
				String s = "00000000" + Integer.toString(node.mCode,2);
				Log.out.printf("%2d %2d %s\n", node.mSymbol, node.mLength, s.substring(s.length()-node.mLength));
			}

			Log.out.println("----------");

			nodes = tree.reconstructTree(tree.extractLengths(nodes));

//			nodes = tree.reconstructTree(2, 4, 3, 2, 2, 4);

			for (Node node : nodes)
			{
				String s = "00000000" + Integer.toString(node.mCode,2);
				Log.out.printf("%2d %2d %s\n", node.mSymbol, node.mLength, s.substring(s.length()-node.mLength));
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (BitOutputStream bos = new BitOutputStream(baos))
			{
				nodes[7].writeTo(bos);
				nodes[2].writeTo(bos);
				nodes[0].writeTo(bos);
				nodes[4].writeTo(bos);
				nodes[1].writeTo(bos);
			}

			int[] alphabet = tree.createDecoderLookupTable(nodes, 8);

			BitInputStream bis = new BitInputStream(new ByteArrayInputStream(baos.toByteArray()));
			for (int i = 0; i < 5; i++)
			{
				Node n = nodes[alphabet[bis.peekBits(8)]];
				bis.skipBits(n.mLength);
				Log.out.println(n.mSymbol);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
