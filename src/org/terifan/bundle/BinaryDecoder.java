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
		int entryCount = mInput.readVar32() - 1;

		if (entryCount == -1)
		{
			return null;
		}

		String[] keys = new String[entryCount];
		int[] types = new int[entryCount];

		for (int i = 0; i < entryCount; i++)
		{
			types[i] = mInput.readBits(8);
			keys[i] = readString();
		}

		for (int i = 0; i < entryCount; i++)
		{
			ObjectType objectType = ObjectType.values()[types[i] >> 4];
			ValueType valueType = ValueType.values()[types[i] & 15];
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

			aBundle.put(keys[i], value, valueType, objectType);
		}

		return aBundle;
	}


	private Object readMatrix(ValueType aValueType) throws IOException
	{
		return readSequence(aValueType, new Sequence()
		{
			Object array;

			@Override
			public void allocate(int aSize)
			{
				array = Array.newInstance(aValueType.getPrimitiveType(), aSize, 0);
			}

			@Override
			public Object read(ValueType aValueType) throws IOException
			{
				return readArray(aValueType);
			}

			@Override
			public void put(int aIndex, Object aValue)
			{
				Array.set(array, aIndex, aValue);
			}

			@Override
			public Object getResult()
			{
				return array;
			}
		});
	}


	private Object readArray(ValueType aValueType) throws IOException
	{
		return readSequence(aValueType, new Sequence()
		{
			Object array;

			@Override
			public void allocate(int aSize)
			{
				array = Array.newInstance(aValueType.getPrimitiveType(), aSize);
			}

			@Override
			public Object read(ValueType aValueType) throws IOException
			{
				return readValue(aValueType);
			}

			@Override
			public void put(int aIndex, Object aValue)
			{
				Array.set(array, aIndex, aValue);
			}

			@Override
			public Object getResult()
			{
				return array;
			}
		});
	}


	private Object readList(ValueType aValueType) throws IOException
	{
		return readSequence(aValueType, new Sequence()
		{
			ArrayList list;

			@Override
			public void allocate(int aSize)
			{
				list = new ArrayList(aSize);
			}

			@Override
			public Object read(ValueType aValueType) throws IOException
			{
				return readValue(aValueType);
			}

			@Override
			public void put(int aIndex, Object aValue)
			{
				assert aIndex == list.size();
				list.add(aValue);
			}

			@Override
			public Object getResult()
			{
				return list;
			}
		});
	}


	private Object readSequence(ValueType aValueType, Sequence aSequence) throws IOException
	{
		int length = mInput.readVar32S();
		boolean[] flags = null;

		if (length < 0)
		{
			length = -length;
			flags = new boolean[length];

			for (int i = 0; i < length; i++)
			{
				flags[i] = mInput.readBit() == 0;
			}

			mInput.align();
		}

		aSequence.allocate(length);

		for (int i = 0; i < length; i++)
		{
			Object value = null;

			if (flags == null || flags[i])
			{
				value = aSequence.read(aValueType);
			}

			aSequence.put(i, value);
		}

		mInput.align();

		return aSequence.getResult();
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
				return (short)mInput.readVar32S();
			case CHAR:
				return (char)mInput.readVar32();
			case INT:
				return mInput.readVar32S();
			case LONG:
				return mInput.readVar64S();
			case FLOAT:
				return Float.intBitsToFloat(mInput.readVar32S());
			case DOUBLE:
				return Double.longBitsToDouble(mInput.readVar64S());
			case STRING:
				return readString();
			case DATE:
				return readDate();
			case BUNDLE:
				return readBundle(new Bundle());
			default:
				throw new IOException("Unsupported field type: " + aValueType);
		}
	}


	private Date readDate() throws IOException
	{
		long time = mInput.readVar64() - 1;

		if (time == -1)
		{
			return null;
		}

		return new Date(time);
	}


	private String readString() throws IOException
	{
		int len = mInput.readVar32() - 1;

		if (len == -1)
		{
			return null;
		}

		byte[] buf = new byte[len];

		if (mInput.read(buf) != buf.length)
		{
			throw new IOException("Unexpected end of stream");
		}

		return Convert.decodeUTF8(buf, 0, buf.length);
	}


	private static interface Sequence
	{
		public void allocate(int aSize);

		public Object read(ValueType aaValueType) throws IOException;

		public void put(int aIndex, Object aValue);

		public Object getResult();
	}
}
