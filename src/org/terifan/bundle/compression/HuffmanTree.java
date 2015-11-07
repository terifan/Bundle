package org.terifan.bundle.compression;

import java.util.Arrays;
import java.util.Comparator;
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
			Node newNode = new Node(left.mFrequency + right.mFrequency, left, right);

			len--;
			int i = len;
			for (; i > 0 && nodes[i - 1].mFrequency < newNode.mFrequency; i--)
			{
			}

			System.arraycopy(nodes, i, nodes, i + 1, len - i);
			nodes[i] = newNode;
		}

		update(nodes[0], 0, 0);

		return reconstructTree(original);
	}
	
	
	public Node[] reconstructTree(int... aLengths)
	{
		Node[] nodes = new Node[aLengths.length];
		for (int i = 0; i < aLengths.length; i++)
		{
			nodes[i] = new Node(i, 0, aLengths[i]);
		}
		
		return reconstructTree(nodes);
	}


	private Node[] reconstructTree(Node[] aNodes)
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
	
	
	private void update(Node aNode, int aCode, int aLength)
	{
		if (aNode.mLeft != null)
		{
			update(aNode.mLeft, aCode << 1, aLength + 1);
		}
		if (aNode.mRight != null)
		{
			update(aNode.mRight, (aCode << 1) + 1, aLength + 1);
		}
		if (aNode.mLeft == null && aNode.mRight == null)
		{
			aNode.mCode = aCode;
			aNode.mLength = aLength;
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

//			nodes = tree.reconstructTree(tree.extractLengths(nodes));
			
			nodes = tree.reconstructTree(2, 4, 3, 2, 2, 4);

			for (Node node : nodes)
			{
				String s = "00000000" + Integer.toString(node.mCode,2);
				Log.out.printf("%2d %2d %s\n", node.mSymbol, node.mLength, s.substring(s.length()-node.mLength));
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
