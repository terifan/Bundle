package org.terifan.bundle;

import org.terifan.bundle.io.BitOutputStream;
import org.terifan.bundle.compression.FrequencyTable;
import org.terifan.bundle.compression.Deflate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.terifan.bundle.bundle_test.Log;
import org.terifan.bundle.compression.Huffman;


public class StyxEncoder implements Encoder
{
	private TreeMap<String,Integer> mKeys;
	private BitOutputStream mOutput;

	private HashMap<String,Integer> mStrings;
	private HashMap<String,Integer> mHeaders;
	private HashMap<String,Integer> mHeaderTypes;

	private FrequencyTable mFreqHeaders;
	private FrequencyTable mFreqStrings;
	private FrequencyTable mFreqStringLengths;
	private FrequencyTable mFreqKeys;
	private FrequencyTable mFreqKeyLengths;
	private FrequencyTable mFreqKeysCount;

	private long mDeltaLong;

	private Deflate mLzjbStrings;
	private Deflate mLzjbKeys;
	private Deflate mLzjbBytes;
	private Deflate mLzjbDates;

	private Huffman mTypeHuffman;
	private Huffman mBundleHuffman;

	private TreeMap<ValueType,Integer> mStatistics;


	public StyxEncoder()
	{
	}


	@Override
	public byte[] marshal(Bundle aBundle) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshal(aBundle, baos);
		return baos.toByteArray();
	}


	@Override
	public void marshal(Bundle aBundle, ByteBuffer aBuffer) throws IOException
	{
		marshal(aBundle, new ByteBufferOutputStream(aBuffer));
	}


	@Override
	public void marshal(Bundle aBundle, OutputStream aOutputStream) throws IOException
	{
		mStatistics = new TreeMap<>();

		mFreqHeaders = new FrequencyTable(1000);
		mFreqStrings = new FrequencyTable(1000);
		mFreqStringLengths = new FrequencyTable(1000);
		mFreqKeys = new FrequencyTable(1000);
		mFreqKeyLengths = new FrequencyTable(50);
		mFreqKeysCount = new FrequencyTable(1000);

		mLzjbStrings = new Deflate();
		mLzjbKeys = new Deflate();
		mLzjbBytes = new Deflate();
		mLzjbDates = new Deflate();

		mOutput = new BitOutputStream(aOutputStream);
		mKeys = new TreeMap<>();

		mStrings = new HashMap<>();
		mHeaders = new HashMap<>();
		mHeaderTypes = new HashMap<>();

		int[] typeHistogram = new int[64];
		HashMap<Integer,Integer> headerHistogram = new HashMap<>();

		buildHistogram(aBundle, typeHistogram, headerHistogram);

		mTypeHuffman = new Huffman(64).buildTree(typeHistogram);
//		mTypeHuffman.encodeCodebook(mOutput);


		int[] bundleHistogramInt = new int[headerHistogram.size()];
		for (int i = 0; i < bundleHistogramInt.length; i++)
		{
			bundleHistogramInt[i] = 1; // headerHistogram.get(i);
		}

		mOutput.writeExpGolomb(bundleHistogramInt.length, 4);

		mBundleHuffman = new Huffman(bundleHistogramInt.length);
//		mBundleHuffman = new Huffman(bundleHistogramInt.length).buildTree(bundleHistogramInt);
//		mBundleHuffman.encodeCodebook(mOutput);


		writeBundle(aBundle);

		mOutput.finish();
		mOutput = null;
		mKeys = null;
	}


	private void buildHistogram(Bundle aBundle, int[] aHistogram, HashMap<Integer,Integer> aHeaderHistogram) throws IOException
	{
		if (aBundle == null)
		{
			return;
		}

		Set<String> keySet = aBundle.keySet();
		String[] keys = keySet.toArray(new String[aBundle.size()]);

		String signature = buildBundleSignature(aBundle, keys);

		Integer header = mHeaderTypes.get(signature);
		if (header != null)
		{
			aHeaderHistogram.put(header, aHeaderHistogram.get(header) + 1);
		}
		else
		{
			aHeaderHistogram.put(mHeaderTypes.size(), 1);
			mHeaderTypes.put(signature, mHeaderTypes.size());
		}


		for (String key : keys)
		{
			ValueType fieldType = aBundle.getValueType(key);

			aHistogram[fieldType.ordinal()]++;

			if (fieldType == ValueType.BUNDLE)
			{
				buildHistogram(aBundle.getBundle(key), aHistogram, aHeaderHistogram);
			}
			else
			{
				switch (aBundle.getObjectType(key))
				{
					case ARRAY:
						for (Bundle b : aBundle.getBundleArray(key))
						{
							buildHistogram(b, aHistogram, aHeaderHistogram);
						}
						break;
					case ARRAYLIST:
						for (Bundle b : aBundle.getBundleArrayList(key))
						{
							buildHistogram(b, aHistogram, aHeaderHistogram);
						}
						break;
					case MATRIX:
						for (Bundle[] bb : aBundle.getBundleMatrix(key))
						{
							if (bb != null)
							{
								for (Bundle b : bb)
								{
									buildHistogram(b, aHistogram, aHeaderHistogram);
								}
							}
						}
						break;
				}
			}
		}
	}


	private void writeBundle(Bundle aBundle) throws IOException
	{
		String[] keys = writeBundleHeader(aBundle);

		for (String key : keys)
		{
			ValueType fieldType = aBundle.getValueType(key);
			Object value = aBundle.get(key);

			mStatistics.put(fieldType, mStatistics.getOrDefault(fieldType, 0) + 1);

			if (fieldType.name().endsWith("_MATRIX"))
			{
				writeMatrix(fieldType, value);
			}
			else if (fieldType.name().endsWith("_ARRAY"))
			{
				writeArray(fieldType, value);
			}
			else if (fieldType.name().endsWith("_ARRAYLIST"))
			{
				writeArray(fieldType, ((List)value).toArray());
			}
			else
			{
				writeValue(fieldType, value);
			}
		}
	}

	private HashMap<Integer,Integer> mLastHeaderIndex = new HashMap<>();
	private int mPrevHeaderIndex;

	private String[] writeBundleHeader(Bundle aBundle) throws IOException
	{
		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);

		String signature = buildBundleSignature(aBundle, keys);

		Integer header = mHeaders.get(signature);
		if (header != null)
		{
			mOutput.writeBit(1);

			if (header.intValue() == mLastHeaderIndex.getOrDefault(mPrevHeaderIndex, -1))
			{
				mOutput.writeBit(1);
				mPrevHeaderIndex = header;
			}
			else
			{
				if (mLastHeaderIndex.containsKey(mPrevHeaderIndex))
				{
					mOutput.writeBit(0);
				}

				mLastHeaderIndex.put(mPrevHeaderIndex, header);
				mPrevHeaderIndex = header;

//				mOutput.writeExpGolomb(mFreqHeaders.encode(header), 1);
				mBundleHuffman.encode(mOutput, header);
				mBundleHuffman.increment(header);
				mBundleHuffman.buildTree();
			}

			return keys;
		}

		int headerIndex = mHeaders.size();

		mHeaders.put(signature, headerIndex);
		mLastHeaderIndex.put(mPrevHeaderIndex, headerIndex);
		mPrevHeaderIndex = headerIndex;

		mOutput.writeBit(0);
		mOutput.writeExpGolomb(mFreqKeysCount.encode(keys.length), 2);

		for (String key : keys)
		{
			ValueType fieldType = aBundle.getValueType(key);
			Object value = aBundle.get(key);

			if (value == null)
			{
				continue;
			}

			mTypeHuffman.encode(mOutput, fieldType.ordinal());
			mTypeHuffman.increment(fieldType.ordinal());
			mTypeHuffman.buildTree();

			if (mKeys.containsKey(key))
			{
				mOutput.writeBit(0);
				mOutput.writeExpGolomb(mFreqKeys.encode(mKeys.get(key)), 3);
			}
			else
			{
				byte[] buffer = Convert.encodeUTF8(key);

				mOutput.writeBit(1);
				mOutput.writeExpGolomb(mFreqKeyLengths.encode(key.length()), 3);
				mLzjbKeys.write(mOutput, buffer);

				mKeys.put(key, mKeys.size());
			}
		}

		return keys;
	}


	private String buildBundleSignature(Bundle aBundle, String[] aKeys)
	{
		StringBuilder signature = new StringBuilder();

		for (String key : aKeys)
		{
			signature.append(aBundle.getValueType(key).ordinal());
			signature.append(":");
			signature.append(key);
			signature.append(",");
		}

		return signature.toString();
	}


	private void writeMatrix(ValueType aFieldType, Object aValue) throws ArrayIndexOutOfBoundsException, IOException, IllegalArgumentException
	{
//		if (aFieldType == FieldType2.BYTE_MATRIX)
//		{
//			writeByteMatrix(aValue);
//			return;
//		}

		int rows = Array.getLength(aValue);
		boolean full = true;

		for (int i = 0, j = 0; i < rows; i++)
		{
			Object arr = Array.get(aValue, i);
			if (arr == null || i > 0 && Array.getLength(arr) != j)
			{
				full = false;
				break;
			}
			else if (i == 0)
			{
				j = Array.getLength(arr);
			}
		}

		if (full)
		{
			mOutput.writeBit(0);
			mOutput.writeExpGolomb(rows, 3);
			if (rows > 0)
			{
				int cols = Array.getLength(Array.get(aValue, 0));
				mOutput.writeExpGolomb(cols, 3);
				for (int i = 0; i < rows; i++)
				{
					Object arr = Array.get(aValue, i);
					for (int j = 0; j < cols; j++)
					{
						writeValue(aFieldType, Array.get(arr, j));
					}
				}
			}
		}
		else
		{
			int len = Array.getLength(aValue);
			mOutput.writeBit(1);
			mOutput.writeExpGolomb(len, 3);
			for (int i = 0; i < len; i++)
			{
				Object v = Array.get(aValue, i);
				if (v == null)
				{
					mOutput.writeBit(1);
				}
				else
				{
					mOutput.writeBit(0);
					writeArray(aFieldType, v);
				}
			}
		}
	}


	private void writeByteMatrix(Object aValue) throws ArrayIndexOutOfBoundsException, IOException, IllegalArgumentException
	{
		byte[][] buf = (byte[][])aValue;
		boolean full = true;

		for (byte[] item : buf)
		{
			if (item == null || item.length != buf[0].length)
			{
				full = false;
				break;
			}
		}

		if (full)
		{
			mOutput.writeBit(0);
			mOutput.writeExpGolomb(buf.length, 3);
			mOutput.writeExpGolomb(buf[0].length, 3);
			for (byte[] item : buf)
			{
				mLzjbBytes.write(mOutput, item);
			}
		}
		else
		{
			mOutput.writeBit(1);
			mOutput.writeExpGolomb(buf.length, 3);
			for (int i = 0; i < buf.length; i++)
			{
				Object v = Array.get(aValue, i);
				if (v == null)
				{
					mOutput.writeBit(1);
				}
				else
				{
					mOutput.writeBit(0);
					mOutput.writeExpGolomb(buf[i].length, 3);
					mLzjbBytes.write(mOutput, buf[i]);
				}
			}
		}
	}


	private void writeByteArray(byte[] aValue) throws IOException
	{
		mOutput.writeExpGolomb(aValue.length, 3);
		mLzjbBytes.write(mOutput, aValue);
	}


	private void writeArray(ValueType aFieldType, Object aValue) throws IOException
	{
//		if (aFieldType == FieldType2.BYTE_ARRAY)
//		{
//			writeByteArray((byte[])aValue);
//			return;
//		}

		int length = Array.getLength(aValue);

		boolean hasNull = false;
		for (int i = 0; i < length; i++)
		{
			if (Array.get(aValue, i) == null)
			{
				hasNull = true;
				break;
			}
		}

		mOutput.writeBit(hasNull);
		mOutput.writeExpGolomb(length, 3);

		for (int i = 0; i < length; i++)
		{
			Object item = Array.get(aValue, i);

			if (hasNull)
			{
				mOutput.writeBit(item == null);
			}

			if (item != null)
			{
				writeValue(aFieldType, item);
			}
		}
	}


	private void writeValue(ValueType aFieldType, Object aValue) throws IOException
	{
		switch (aFieldType)
		{
			case BOOLEAN:
				mOutput.writeBit((Boolean)aValue);
				break;
			case BYTE:
				mOutput.writeBits(0xff & (Byte)aValue, 8);
				break;
			case SHORT:
				mOutput.writeVariableInt((Short)aValue, 3, 0, true);
				break;
			case CHAR:
				mOutput.writeVariableInt((Character)aValue, 3, 0, false);
				break;
			case INT:
				mOutput.writeVariableInt((Integer)aValue, 3, 0, true);
				break;
			case LONG:
				mOutput.writeVariableLong((Long)aValue-mDeltaLong, 3, 1, true);
				mDeltaLong = (Long)aValue;
				break;
			case FLOAT:
				mOutput.writeVariableInt(Float.floatToIntBits((Float)aValue), 3, 1, false);
				break;
			case DOUBLE:
				mOutput.writeVariableLong(Double.doubleToLongBits((Double)aValue), 3, 1, false);
				break;
			case STRING:
				packString(aValue);
				break;
			case DATE:
//				mOutput.writeVariableLong(((Date)aValue).getTime(), 7, 0, true);
				mLzjbDates.write(mOutput, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(aValue).getBytes());
				break;
			case BUNDLE:
				writeBundle((Bundle)aValue);
				break;
			default:
				throw new IOException("Unsupported field type: " + aFieldType);
		}
	}


	private boolean packString(Object aValue) throws IOException
	{
		String s = (String)aValue;
		Integer index = mStrings.get(s);

		if (index != null)
		{
			mOutput.writeBit(1);
			mOutput.writeExpGolomb(mFreqStrings.encode(index), 3);
			return true;
		}

		byte[] buffer = Convert.encodeUTF8((String)aValue);

		mOutput.writeBit(0);
		mOutput.writeExpGolomb(mFreqStringLengths.encode(buffer.length), 3);

		mLzjbStrings.write(mOutput, buffer);

		mStrings.put(s, mStrings.size());

		return false;
	}


	public String getStatistics()
	{
		return ""; //mBundleHuffman.getCumulativeFrequency() + ", " + mTypeHuffman.getCumulativeFrequency() + ", " + mStatistics;
	}
}