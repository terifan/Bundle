package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.terifan.bundle.bundle_test.Log;


public class BinaryEncoder implements Encoder
{
	private TreeMap<String,Integer> mKeys;
	private BitOutputStream mOutput;

	private HashMap<String,Integer> mStrings;
	private HashMap<String,Integer> mHeaders;

	private boolean PACK;
	private boolean ALIGN;


	public BinaryEncoder()
	{
		setPACK(false);
	}


	public BinaryEncoder setPACK(boolean aPACK)
	{
		PACK = aPACK;
		ALIGN = !PACK;
		return this;
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


	private void writeBundle(Bundle aBundle) throws IOException
	{
		String[] keys = writeBundleKeys(aBundle);

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.classify(value);

			mOutput.writeBits(fieldType.ordinal(), 4);

			if (fieldType == FieldType.NULL || fieldType == FieldType.EMPTY_LIST)
			{
				continue;
			}

			Class<? extends Object> cls = value.getClass();

			if (cls.isArray())
			{
				if (cls.getComponentType().isArray())
				{
					mOutput.writeBits(0b111, 3);
					writeMatrix(fieldType, value);
				}
				else
				{
					mOutput.writeBits(0b110, 3);
					writeArray(fieldType, value);
				}
			}
			else if (List.class.isAssignableFrom(cls))
			{
				mOutput.writeBits(0b10, 2);
				writeArray(fieldType, ((List)value).toArray());
			}
			else
			{
				mOutput.writeBits(0b0, 1);
				writeValue(fieldType, value);
			}
		}
	}


	private void writeByteArray(Object aValue) throws IOException
	{
		mOutput.writeVariableInt(((byte[])aValue).length, 3, 4, false);
		if (ALIGN) mOutput.align();
		mOutput.write((byte[])aValue);
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
			mOutput.writeVariableInt(rows, 3, 4, false);
			if (rows > 0)
			{
				int cols = Array.getLength(Array.get(aValue, 0));
				mOutput.writeVariableInt(cols, 3, 4, false);
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
			mOutput.writeVariableInt(len, 3, 4, false);
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
			mOutput.writeVariableInt(buf.length, 3, 4, false);
			mOutput.writeVariableInt(buf[0].length, 3, 4, false);
			if (ALIGN) mOutput.align();
			for (int i = 0; i < buf.length; i++)
			{
				mOutput.write(buf[i]);
			}
		}
		else
		{
			mOutput.writeBit(1);
			mOutput.writeVariableInt(buf.length, 3, 4, false);
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
					mOutput.writeVariableInt(buf[i].length, 3, 4, false); // 3,1?
					if (ALIGN) mOutput.align();
					mOutput.write(buf[i]);
				}
			}
		}
	}


	private String[] writeBundleKeys(Bundle aBundle) throws IOException
	{
		int initialKeyCount = mKeys.size();
		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);
		ByteArrayOutputStream keyData = new ByteArrayOutputStream();

		if (packHeaders(keys))
		{
			return keys;
		}

		mOutput.writeVariableInt(keys.length, 3, 0, false);

		for (String key : keys)
		{
			if (mKeys.containsKey(key))
			{
				mOutput.writeBit(0);
				mOutput.writeBitsInRange(mKeys.get(key), initialKeyCount);
			}
			else
			{
				byte[] buffer = Convert.encodeUTF8(key);
				keyData.write(buffer);

				mOutput.writeBit(1);
				mOutput.writeVariableInt(buffer.length, 3, 0, false);

				mKeys.put(key, mKeys.size());
			}
		}

		if (keyData.size() > 0)
		{
			if (ALIGN) mOutput.align();

			mOutput.write(keyData.toByteArray());
		}

		return keys;
	}


	private boolean packHeaders(String[] aKeys) throws IOException
	{
		if (PACK)
		{
			String header = "" + aKeys.length;

			for (String key : aKeys)
			{
				if (mKeys.containsKey(key))
				{
					header += "," + mKeys.get(key);
				}
				else
				{
					header = null;
					break;
				}
			}

			if (header != null)
			{
				if (mHeaders.containsKey(header))
				{
					mOutput.writeBit(1);
					mOutput.writeVariableInt(mHeaders.get(header), 3, 0, false);

					return true;
				}

				mHeaders.put(header, mHeaders.size());
			}

			mOutput.writeBit(0);
		}

		return false;
	}


	private void writeArray(FieldType aFieldType, Object aValue) throws IOException
	{
		if (aFieldType == FieldType.BYTE)
		{
			writeByteArray(aValue);
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

		mOutput.writeVariableInt(hasNull ? -length : length, 3, 0, true);

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
				mOutput.writeVariableInt((Integer)aValue, 3, 1, true);
				break;
			case LONG:
				mOutput.writeVariableLong((Long)aValue, 7, 0, true); // 3,1?
				break;
			case FLOAT:
				mOutput.writeVariableInt(Float.floatToIntBits((Float)aValue), 7, 0, false); // 3,1
				break;
			case DOUBLE:
				mOutput.writeVariableLong(Double.doubleToLongBits((Double)aValue), 7, 0, false); // 3,1
				break;
			case STRING:
				if (packString(aValue))
				{
					break;
				}

				byte[] buffer = Convert.encodeUTF8((String)aValue);
				mOutput.writeVariableInt(buffer.length, 3, 0, false);
				if (ALIGN) mOutput.align();
				mOutput.write(buffer);
				break;
			case DATE:
				mOutput.writeVariableLong(((Date)aValue).getTime(), 7, 0, false);
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
		if (PACK)
		{
			String s = (String)aValue;
			Integer index = mStrings.get(s);

			if (index != null)
			{
				mOutput.writeBit(1);
				mOutput.writeVariableInt(index, 3, 0, false);
				return true;
			}

			mStrings.put(s, mStrings.size());
			mOutput.writeBit(0);
		}

		return false;
	}
}