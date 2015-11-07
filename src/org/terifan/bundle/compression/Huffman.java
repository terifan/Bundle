package org.terifan.bundle.compression;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.terifan.bundle.io.BitInputStream;
import org.terifan.bundle.io.BitOutputStream;


/**
 * This class builds and maintains a canonical Huffman tree.
 */
public class Huffman 
{
	private final static Comparator<? super Node> mFrequencySorter = (e,f)->Integer.compare(f.mFrequency, e.mFrequency);
	private final static Comparator<? super Node> mLengthSymbolSorter = (e,f)->e.mLength == f.mLength ? Integer.compare(e.mSymbol, f.mSymbol) : Integer.compare(e.mLength, f.mLength);
	private final static Comparator<? super Node> mSymbolSorter = (e,f)->Integer.compare(e.mSymbol, f.mSymbol);

	private final Node[] mNodes;
	private final int mSymbolCount;
	private int[] mDecoderLookup;
	private int mMaxCodeLength;


	/**
	 * Create a baseline Huffman tree. All symbols are initiated with a one (1) frequency.
	 * 
	 * @param aSymbolCount 
	 *   the number of symbols the tree supports.
	 */
	public Huffman(int aSymbolCount)
	{
		mSymbolCount = aSymbolCount;
		mNodes = new Node[aSymbolCount];

		for (int i = 0; i < aSymbolCount; i++)
		{
			mNodes[i] = new Node(i, 0, 0);
			mNodes[i].mFrequency = 1;
		}

		int[] freq = new int[aSymbolCount];
		Arrays.fill(freq, 1);
		buildTree(freq);
	}


	/**
	 * Builds a tree using the frequencies provided.
	 * 
	 * @return 
	 *   this instance.
	 */
	public Huffman buildTree(int... aFrequencies)
	{
		if (aFrequencies.length != mSymbolCount)
		{
			throw new IllegalArgumentException();
		}

		Node[] nodes = mNodes.clone();
		int len = 0;

		for (int i = 0; i < mSymbolCount; i++)
		{
			mNodes[i].mCode = 0;
			mNodes[i].mLength = 0;
			mNodes[i].mFrequency = aFrequencies[i];

			if (aFrequencies[i] > 0)
			{
				len++;
			}
		}

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
		
		reconstructTreeImpl();
		
		updateDecoderLookup();

		return this;
	}


	/**
	 * Reconstruct a tree using the code lengths returned by the <code>extractLengths()</code> method.
	 * 
	 * @return 
	 *   this instance.
	 */
	public Huffman reconstructTree(int... aCodeLengths)
	{
		if (aCodeLengths.length != mSymbolCount)
		{
			throw new IllegalArgumentException();
		}

		mMaxCodeLength = 0;
		
		for (int i = 0; i < mSymbolCount; i++)
		{
			mNodes[i].mCode = 0;
			mNodes[i].mFrequency = 0;
			mNodes[i].mLength = aCodeLengths[i];
		}

		reconstructTreeImpl();

		updateDecoderLookup();

		return this;
	}


	/**
	 * This is where the canonical magic happens, using lengths to reconstruct the tree.
	 */
	private void reconstructTreeImpl()
	{
		Arrays.sort(mNodes, mLengthSymbolSorter);

		int j = 0;
		for (; mNodes[j].mLength == 0 && j < mSymbolCount; j++)
		{
		}

		mNodes[j].mCode = 0;
		int prevLength = mNodes[j].mLength;
		j++;

		for (int i = 0; j < mSymbolCount; j++)
		{
			int length = mNodes[j].mLength;

			i = (i + 1) << (length - prevLength);

			prevLength = length;
			mNodes[j].mCode = i;
		}

		Arrays.sort(mNodes, mSymbolSorter);
	}


	/**
	 * Compute lengths and release interior nodes.
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


	/**
	 * Return an array containing all code lengths. These lengths are all necessary to reconstruct a tree.
	 */
	public int[] extractLengths()
	{
		int[] lengths = new int[mSymbolCount];
		for (int i = 0; i < mSymbolCount; i++)
		{
			lengths[i] = mNodes[i].mLength;
		}

		return lengths;
	}


	/**
	 * Builds a 2**mMaxCodeLength entry table for lookups.
	 */
	private void updateDecoderLookup()
	{
		mMaxCodeLength = 0;

		for (Node node : mNodes)
		{
			mMaxCodeLength = Math.max(mMaxCodeLength, node.mLength);
		}

		mDecoderLookup = new int[1 << mMaxCodeLength];

		for (int i = 0, symbol = 0; i < mDecoderLookup.length; symbol++)
		{
			int length = mNodes[symbol].mLength;

			if (length > 0)
			{
				int code = mNodes[symbol].mCode << (mMaxCodeLength - length);

				for (int k = 0, sz = 1 << (mMaxCodeLength - length); k < sz; k++, i++)
				{
					mDecoderLookup[k + code] = symbol;
				}
			}
		}
	}


	/**
	 * Return the code/length pair used to encode a certain symbol.
	 */
	public Symbol getSymbol(int aSymbol)
	{
		return mNodes[aSymbol];
	}


	/**
	 * Decode a symbol given the code provided.
	 * 
	 * @param aPeekBits
	 *   bits read from an underlying stream. Must contain <code>getMaxCodeLength()</code> bits.
	 * @return 
	 *   the symbol
	 */
	public Symbol decode(int aPeekBits)
	{
		return mNodes[mDecoderLookup[aPeekBits]];
	}


	/**
	 * Decode the next symbol from the stream provided.
	 * 
	 * Note: This method will peek ahead of the current position in stream.
	 * 
	 * @return
	 *   the symbol that was decoded.
	 */
	public Symbol decode(BitInputStream aBitInputStream) throws IOException
	{
		Node node = mNodes[mDecoderLookup[aBitInputStream.peekBits(mMaxCodeLength)]];
		aBitInputStream.skipBits(node.mLength);
		
		return node;
	}


	/**
	 * Encode a symbol to the stream.
	 * 
	 * @return
	 *   the symbol that was encoded.
	 */
	public Symbol encode(BitOutputStream aBitOutputStream, int aSymbol) throws IOException
	{
		if (aSymbol < 0 || aSymbol >= mSymbolCount)
		{
			throw new IllegalArgumentException();
		}

		Node node = mNodes[aSymbol];
		
		if (node.mFrequency == 0)
		{
			throw new IllegalArgumentException();
		}
		
		aBitOutputStream.writeBits(node.mCode, node.mLength);
		
		return node;
	}


	/**
	 * Return the longest code.
	 */
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

		/**
		 * Bit pattern written to an underlying stream.
		 */
		int getCode();

		/**
		 * Length of the bit pattern.
		 */
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
}