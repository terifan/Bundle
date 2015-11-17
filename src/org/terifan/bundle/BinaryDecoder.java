package org.terifan.bundle;

import org.terifan.bundle.io.BitInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;


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
		int keyCount = (int)mInput.readVLC();

		String[] keys = new String[keyCount];
		ValueType[] valueTypes = new ValueType[keyCount];
		ObjectType[] objectTypes = new ObjectType[keyCount];

		for (int i = 0; i < keyCount; i++)
		{
			objectTypes[i] = ObjectType.values()[(int)mInput.readBits(2)];
			valueTypes[i] = ValueType.values()[(int)mInput.readBits(4)];
		}

		mInput.align();

		for (int i = 0; i < keyCount; i++)
		{
			keys[i] = readString();
		}

		for (int i = 0; i < keyCount; i++)
		{
			ValueType valueType = valueTypes[i];
			ObjectType objectType = objectTypes[i];
			Object value;

			switch (objectType)
			{
				case VALUE:
					value = readValue(valueType);
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

			aBundle.put(keys[i], value, valueType, objectType);
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

		Object array = Array.newInstance(aValueType.getPrimitiveType(), (int)len, 0);

		for (int i = 0; i < len; i++)
		{
			Object value;

			if (nulls != null && nulls[i])
			{
				value = null;
			}
			else
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
				return new Date(mInput.readVLC());
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
