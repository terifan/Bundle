package org.terifan.bundle;

import org.terifan.bundle.io.BitInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import org.terifan.bundle.bundle_test.Log;


public class BinaryDecoder implements Decoder
{
	private BitInputStream mInput;


	public BinaryDecoder()
	{
	}


	@Override
	public Bundle unmarshal(byte[] aBuffer) throws IOException
	{
		return unmarshal(new Bundle(), aBuffer);
	}


	@Override
	public Bundle unmarshal(Bundle aBundle, byte[] aBuffer) throws IOException
	{
		return unmarshal(aBundle, new ByteArrayInputStream(aBuffer));
	}


	@Override
	public Bundle unmarshal(ByteBuffer aBuffer) throws IOException
	{
		return unmarshal(new Bundle(), aBuffer);
	}


	@Override
	public Bundle unmarshal(Bundle aBundle, ByteBuffer aBuffer) throws IOException
	{
		return unmarshal(aBundle, new ByteBufferInputStream(aBuffer));
	}


	@Override
	public Bundle unmarshal(InputStream aInputStream) throws IOException
	{
		return unmarshal(new Bundle(), aInputStream);
	}


	@Override
	public Bundle unmarshal(Bundle aBundle, InputStream aInputStream) throws IOException
	{
		mInput = new BitInputStream(aInputStream);

		return readBundle(aBundle);
	}


	private Bundle readBundle(Bundle aBundle) throws IOException
	{
		int entryCount = (int)mInput.readVLC();

		if (entryCount == -1)
		{
			return null;
		}

		int[] types = new int[entryCount];

		for (int i = 0; i < entryCount; i++)
		{
			types[i] = (int)mInput.readBits(6);
		}

		mInput.align();

		for (int i = 0; i < entryCount; i++)
		{
			String key = readString();

			ObjectType objectType = ObjectType.values()[types[i] >> 4];
			ValueType valueType = ValueType.values()[types[i] & 0b1111];
			Object value;

			switch (objectType)
			{
				case VALUE:
					value = readValue(valueType);
					mInput.align();
					break;
				case ARRAYLIST:
					value = readList(valueType);
					break;
				case ARRAY:
					value = readArray(valueType);
					break;
				case MATRIX:
					value = readMatrix(valueType);
					break;
				default:
					throw new IOException();
			}

			aBundle.put(key, value, valueType, objectType);
		}

		return aBundle;
	}


	private Object readArray(ValueType aValueType) throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException, NegativeArraySizeException
	{
		ArrayList list = readList(aValueType);

		Object array = Array.newInstance(aValueType.getPrimitiveType(), list.size());

		for (int i = 0; i < list.size(); i++)
		{
			Array.set(array, i, list.get(i));
		}

		return array;
	}


	private Object readMatrix(ValueType aValueType) throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException, NegativeArraySizeException
	{
		long length = mInput.readVLC();
		boolean[] flags = null;

		if (length < 0)
		{
			length = -length;
			flags = new boolean[(int)length];

			for (int i = 0; i < length; i++)
			{
				flags[i] = mInput.readBit() == 0;
			}

			mInput.align();
		}

		Object array = Array.newInstance(aValueType.getPrimitiveType(), (int)length, 0);

		for (int i = 0; i < length; i++)
		{
			Object value = null;

			if (flags == null || flags[i])
			{
				value = readArray(aValueType);
			}

			Array.set(array, i, value);
		}

		return array;
	}


	private ArrayList readList(ValueType aValueType) throws IOException
	{
		long len = mInput.readVLC();
		boolean[] nulls = null;

		if (len < 0)
		{
			len = -len;
			nulls = new boolean[(int)len];

			for (int i = 0; i < len; i++)
			{
				nulls[i] = mInput.readBit() == 1;
			}

			mInput.align();
		}

		ArrayList list = new ArrayList((int)len);

		for (int i = 0; i < len; i++)
		{
			Object value;

			if (nulls != null && nulls[i])
			{
				value = null;
			}
			else
			{
				value = readValue(aValueType);
			}

			list.add(value);
		}

		mInput.align();

		return list;
	}


	private Object readValue(ValueType aValueType) throws IOException
	{
		switch (aValueType)
		{
			case BOOLEAN:
				return mInput.readBit() == 1;
			case BYTE:
				return (byte)mInput.readBits(8);
			case SHORT:
				return (short)mInput.readVLC();
			case CHAR:
				return (char)mInput.readVLC();
			case INT:
				return (int)mInput.readVLC();
			case LONG:
				return mInput.readVLC();
			case FLOAT:
				return Float.intBitsToFloat((int)mInput.readVLC());
			case DOUBLE:
				return Double.longBitsToDouble(mInput.readVLC());
			case STRING:
				return readString();
			case DATE:
				long time = mInput.readVLC();
				if (time == -1)
				{
					return null;
				}
				return new Date(time);
			case BUNDLE:
				return readBundle(new Bundle());
			default:
				throw new IOException("Unsupported field type: " + aValueType);
		}
	}


	private String readString() throws IOException
	{
		long len = mInput.readVLC();

		if (len == -1)
		{
			return null;
		}

		byte[] buf = new byte[(int)len];

		if (mInput.read(buf) != buf.length)
		{
			throw new IOException("Unexpected end of stream");
		}

		return Convert.decodeUTF8(buf, 0, buf.length);
	}
}
