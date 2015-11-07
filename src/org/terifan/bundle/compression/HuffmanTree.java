package org.terifan.bundle.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.terifan.bundle.io.BitInputStream;
import org.terifan.bundle.io.BitOutputStream;
import org.terifan.bundle.bundle_test.Log;


/**
 * This class builds and maintains a canonical Huffman tree.
 */
public class HuffmanTree 
{
	private final static Comparator<? super Node> mFrequencySorter = (e,f)->Integer.compare(f.mFrequency, e.mFrequency);
	private final static Comparator<? super Node> mLengthSymbolSorter = (e,f)->e.mLength == f.mLength ? Integer.compare(e.mSymbol, f.mSymbol) : Integer.compare(e.mLength, f.mLength);
	private final static Comparator<? super Node> mSymbolSorter = (e,f)->Integer.compare(e.mSymbol, f.mSymbol);

	private final Node[] mNodes;
	private final int mSymbolCount;
	private int[] mDecoderLookup;
	private int mMaxCodeLength;


	public HuffmanTree(int aSymbolCount)
	{
		mSymbolCount = aSymbolCount;
		mNodes = new Node[aSymbolCount];

		for (int i = 0; i < aSymbolCount; i++)
		{
			mNodes[i] = new Node(i, 0, 0);
			mNodes[i].mFrequency = 1;
		}

		buildTree();
	}


	public HuffmanTree buildTree(int... aFrequencies)
	{
		if (aFrequencies.length != mSymbolCount && aFrequencies.length != 0)
		{
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < aFrequencies.length; i++)
		{
			mNodes[i].mFrequency = 1 + aFrequencies[i]; // zero frequencies not supported
		}

		Node[] nodes = mNodes.clone();
		Arrays.sort(nodes, mFrequencySorter);

		int len = mSymbolCount;

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

		reconstructTreeImpl();
		
		return this;
	}


	public HuffmanTree reconstructTree(int... aLengths)
	{
		if (aLengths.length != mSymbolCount)
		{
			throw new IllegalArgumentException();
		}

		mMaxCodeLength = 0;

		for (int i = 0; i < mSymbolCount; i++)
		{
			int len = aLengths[i];

			mNodes[i].mLength = len;

			if (len > mMaxCodeLength)
			{
				mMaxCodeLength = len;
			}
		}
		
		reconstructTreeImpl();
		
		updateDecoderLookup();
		
		return this;
	}


	private void reconstructTreeImpl()
	{
		Arrays.sort(mNodes, mLengthSymbolSorter);
		
		mNodes[0].mCode = 0;
		for (int i = 0, j = 1; j < mSymbolCount; j++)
		{
			i = (i + 1) << (mNodes[j].mLength - mNodes[j - 1].mLength);

			mNodes[j].mCode = i;
		}

		Arrays.sort(mNodes, mSymbolSorter);
	}


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


	private int[] extractLengths()
	{
		int[] lengths = new int[mSymbolCount];
		for (int i = 0; i < mSymbolCount; i++)
		{
			lengths[i] = mNodes[i].mLength;
		}

		return lengths;
	}
	
	
	private void updateDecoderLookup()
	{
		mDecoderLookup = new int[1 << mMaxCodeLength];

		for (int i = 0, symbol = 0; i < mDecoderLookup.length; symbol++)
		{
			int length = mNodes[symbol].mLength;
			int code = mNodes[symbol].mCode << (mMaxCodeLength - length);

			for (int k = 0, sz = 1 << (mMaxCodeLength - length); k < sz; k++, i++)
			{
				mDecoderLookup[k + code] = symbol;
			}
		}
	}


	public Symbol getSymbol(int aSymbol)
	{
		return mNodes[aSymbol];
	}


	public Symbol decode(int aPeekBits)
	{
		return mNodes[mDecoderLookup[aPeekBits]];
	}


	public Symbol decode(BitInputStream aBitInputStream) throws IOException
	{
		Node node = mNodes[mDecoderLookup[aBitInputStream.peekBits(mMaxCodeLength)]];
		aBitInputStream.skipBits(node.mLength);
		
		return node;
	}


	public void encode(BitOutputStream aBitOutputStream, int aSymbol) throws IOException
	{
		if (aSymbol < 0 || aSymbol >= mSymbolCount)
		{
			throw new IllegalArgumentException();
		}

		Node node = mNodes[aSymbol];
		aBitOutputStream.writeBits(node.mCode, node.mLength);
	}


	public int getMaxCodeLength()
	{
		return mMaxCodeLength;
	}


	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Node node : mNodes)
		{
			String s = "00000000" + Integer.toString(node.mCode,2);
			sb.append(String.format("%2d %2d %4d %s%s", node.mSymbol, node.mLength, node.mFrequency, s.substring(s.length() - node.mLength), node != mNodes[mSymbolCount - 1] ? "\n" : ""));
		}

		return sb.toString();
	}
	
	
	public interface Symbol
	{
		int getSymbol();
		
		int getCode();
		
		int getLength();
	}


	private static class Node implements Symbol
	{
		private int mSymbol;
		private int mFrequency;
		private Node mLeft;
		private Node mRight;
		private int mCode;
		private int mLength;


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


		@Override
		public int getSymbol()
		{
			return mSymbol;
		}


		@Override
		public int getCode()
		{
			return mCode;
		}


		@Override
		public int getLength()
		{
			return mLength;
		}
	}
	
	
	public static void main(String... args)
	{
		try
		{
			int[] lengths;
			byte[] buffer;

			{
				HuffmanTree tree = new HuffmanTree(16).buildTree(42,15,0,10,8,20,23,48,0,9,16,21,5,7,0,18);

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
				HuffmanTree tree = new HuffmanTree(lengths.length).reconstructTree(lengths);

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
