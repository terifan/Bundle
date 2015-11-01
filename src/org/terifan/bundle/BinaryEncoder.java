package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


// header
//  #copy header id
//	fields
//   field
//    #same type as before
//	    ?new type
//    #name
//  #terminator
// data
//  fields
//   field
//    #value

public class BinaryEncoder implements Encoder
{
	private TreeMap<String,Integer> mKeys;
	private BitOutputStream mOutput;

	private HashMap<String,Integer> mStrings;
	private HashMap<String,Integer> mHeaders;

FrequencyTable tblHeaders = new FrequencyTable(1000);
FrequencyTable tblStrings = new FrequencyTable(1000);
FrequencyTable tblStringLengths = new FrequencyTable(1000);
FrequencyTable tblKeys = new FrequencyTable(1000);
FrequencyTable tblFieldType = new FrequencyTable(20);
FrequencyTable tblKeyLengths = new FrequencyTable(50);

int[][] huffman1 = {
{2,0b01},
{2,0b10},
{2,0b11},
{4,0b001},
{5,0b00001},
{5,0b00010},
{5,0b00011},
{8,0b00000000},
{8,0b00000001},
{8,0b00000010},
{8,0b00000011},
{8,0b00000100},
{8,0b00000101},
{8,0b00000110},
{8,0b00000111}
};

long mDeltaDate;
long mDeltaLong;
long mStatisticsRawCount;


	private TreeMap<FieldType,Integer> mStatistics = new TreeMap<>();
	private TreeMap<String,Integer> mStatisticsOperations = new TreeMap<>();


	public BinaryEncoder()
	{
		tblFieldType.encode(FieldType.INT.ordinal());
		tblFieldType.encode(FieldType.STRING.ordinal());
		tblFieldType.encode(FieldType.DOUBLE.ordinal());
		tblFieldType.encode(FieldType.BUNDLE.ordinal());
		tblFieldType.encode(FieldType.DATE.ordinal());
		tblFieldType.encode(FieldType.LONG.ordinal());
		tblFieldType.encode(FieldType.BOOLEAN.ordinal());
		tblFieldType.encode(FieldType.NULL.ordinal());
		tblFieldType.encode(FieldType.EMPTY.ordinal());
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
		mOutput = new BitOutputStream(aOutputStream);
		mKeys = new TreeMap<>();

		mStrings = new HashMap<>();
		mHeaders = new HashMap<>();

		writeBundle(aBundle);

		mOutput.finish();
		mOutput = null;
		mKeys = null;
	}


	public String getStatistics()
	{
		return "raw=" + mStatisticsRawCount + ", op=" + mStatisticsOperations + ", types=" + mStatistics;
	}


	private void writeBundle(Bundle aBundle) throws IOException
	{
		String[] keys = writeBundleHeader(aBundle);

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.classify(value);

			mStatistics.put(fieldType, mStatistics.getOrDefault(fieldType, 0) + 1);
			
			if (fieldType != FieldType.NULL && fieldType != FieldType.EMPTY)
			{
				Class<? extends Object> cls = value.getClass();

				if (cls.isArray())
				{
					if (cls.getComponentType().isArray())
					{
						writeMatrix(fieldType, value);
					}
					else
					{
						writeArray(fieldType, value);
					}
				}
				else if (List.class.isAssignableFrom(cls))
				{
					writeArray(fieldType, ((List)value).toArray());
				}
				else
				{
					writeValue(fieldType, value);
				}
			}
		}
	}


	private void writeByteArray(byte[] aValue) throws IOException
	{
		mOutput.writeVariableInt(aValue.length, 3, 1, false);
		mOutput.write(aValue);
		
		mStatisticsRawCount += aValue.length;
	}


	private void writeMatrix(FieldType aFieldType, Object aValue) throws ArrayIndexOutOfBoundsException, IOException, IllegalArgumentException
	{
		if (aFieldType == FieldType.BYTE)
		{
			writeByteMatrix(aValue);
			return;
		}

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
			mOutput.writeVariableInt(rows, 3, 1, false);
			if (rows > 0)
			{
				int cols = Array.getLength(Array.get(aValue, 0));
				mOutput.writeVariableInt(cols, 3, 1, false);
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
			mOutput.writeVariableInt(len, 3, 1, false);
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

		for (int i = 0; i < buf.length; i++)
		{
			if (buf[i] == null || buf[i].length != buf[0].length)
			{
				full = false;
				break;
			}
		}

		if (full)
		{
			mOutput.writeBit(0);
			mOutput.writeVariableInt(buf.length, 3, 1, false);
			mOutput.writeVariableInt(buf[0].length, 3, 1, false);
			for (int i = 0; i < buf.length; i++)
			{
				mOutput.write(buf[i]);
		
				mStatisticsRawCount += buf.length;
			}
		}
		else
		{
			mOutput.writeBit(1);
			mOutput.writeVariableInt(buf.length, 3, 1, false);
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
					mOutput.writeVariableInt(buf[i].length, 3, 1, false);
					mOutput.write(buf[i]);
		
					mStatisticsRawCount += buf.length;
				}
			}
		}
	}


	private String[] writeBundleHeader(Bundle aBundle) throws IOException
	{
		int initialKeyCount = mKeys.size();
		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);


		StringBuilder signature = new StringBuilder();
		
		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.classify(value);
			Class<? extends Object> cls = value.getClass();

			signature.append(fieldType.ordinal());
			signature.append(":");
			signature.append(key);
			signature.append(":");

			if (fieldType != FieldType.NULL && fieldType != FieldType.EMPTY)
			{
				if (cls.isArray())
				{
					if (cls.getComponentType().isArray())
					{
						signature.append("1:");
					}
					else
					{
						signature.append("2:");
					}
				}
				else if (List.class.isAssignableFrom(cls))
				{
					signature.append("3:");
				}
				else
				{
					signature.append("4:");
				}
			}
		}
		
		Integer header = mHeaders.get(signature.toString());
		if (header != null)
		{
			inc("dup");

			mOutput.writeBit(1);
//			mOutput.writeVariableInt(header, 3, 0, false);
			mOutput.writeVariableInt(tblHeaders.encode(header), 1, 0, false);
			return keys;
		}

		inc("hdr");
		
		mHeaders.put(signature.toString(), mHeaders.size());

		mOutput.writeBit(0);
		mOutput.writeVariableInt(keys.length, 3, 0, false);

		FieldType prevFieldType = null;

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.classify(value);
			Class<? extends Object> cls = value.getClass();

			if (fieldType == prevFieldType)
			{
				mOutput.writeBit(1);
			}
			else
			{
				prevFieldType = fieldType;

				mOutput.writeBit(0);
//				mOutput.writeBits(fieldType.ordinal(), 4);

				int n = tblFieldType.encode(fieldType.ordinal());
				mOutput.writeBits(huffman1[n][1], huffman1[n][0]);

				if (fieldType != FieldType.NULL && fieldType != FieldType.EMPTY)
				{
					if (cls.isArray())
					{
						if (cls.getComponentType().isArray())
						{
							mOutput.writeBits(0b111, 3);
						}
						else
						{
							mOutput.writeBits(0b110, 3);
						}
					}
					else if (List.class.isAssignableFrom(cls))
					{
						mOutput.writeBits(0b10, 2);
					}
					else
					{
						mOutput.writeBits(0b0, 1);
					}
				}
			}

			if (mKeys.containsKey(key))
			{
				inc("key");

				mOutput.writeBit(0);
//				mOutput.writeVariableInt(mKeys.get(key), 3, 0, false);
//				mOutput.writeBitsInRange(mKeys.get(key), initialKeyCount);
				mOutput.writeVariableInt(tblKeys.encode(mKeys.get(key)), 3, 0, false);
			}
			else
			{
				byte[] buffer = Convert.encodeUTF8(key);

				mOutput.writeBit(1);
//				mOutput.writeVariableInt(buffer.length, 3, 0, false);
				mOutput.writeVariableInt(tblKeyLengths.encode(buffer.length), 3, 0, false);
				mOutput.write(buffer);
		
				mStatisticsRawCount += buffer.length;

				mKeys.put(key, mKeys.size());
			}
		}

		return keys;
	}


	private void writeArray(FieldType aFieldType, Object aValue) throws IOException
	{
		if (aFieldType == FieldType.BYTE)
		{
			writeByteArray((byte[])aValue);
			return;
		}

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

		mOutput.writeBit(hasNull ? 1 : 0);
		mOutput.writeVariableInt(length, 3, 0, false);

		for (int i = 0; i < length; i++)
		{
			Object item = Array.get(aValue, i);

			if (hasNull)
			{
				mOutput.writeBit(item == null ? 1 : 0);
			}

			if (item != null)
			{
				writeValue(aFieldType, item);
			}
		}
	}


	private void writeValue(FieldType aFieldType, Object aValue) throws IOException
	{
		switch (aFieldType)
		{
			case BOOLEAN:
				mOutput.writeBit((Boolean)aValue ? 1 : 0);
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
//				mOutput.writeVariableLong((Long)aValue, 3, 0, true);
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
				long time = ((Date)aValue).getTime();
//				mOutput.writeVariableLong(time, 7, 0, false);
				mOutput.writeVariableLong(time-mDeltaDate, 3, 1, true);
				mDeltaDate = time;
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
			inc("str");
			
			mOutput.writeBit(1);
//			mOutput.writeVariableInt(index, 3, 0, false);
			mOutput.writeVariableInt(tblStrings.encode(index), 3, 0, false);
			return true;
		}
		
		byte[] buffer = Convert.encodeUTF8((String)aValue);

		mOutput.writeBit(0);
//		mOutput.writeVariableInt(buffer.length, 3, 0, false);
		mOutput.writeVariableInt(tblStringLengths.encode(buffer.length), 3, 0, false);
		mOutput.write(buffer);
		
		mStatisticsRawCount += buffer.length;

		mStrings.put(s, mStrings.size());

		return false;
	}


	private void inc(String aOperation)
	{
		mStatisticsOperations.put(aOperation, mStatisticsOperations.getOrDefault(aOperation, 0) + 1);
	}
}