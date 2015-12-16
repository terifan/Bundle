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
			types[i] = mInput.readVar32();
			keys[i] = readString();
		}

		for (int i = 0; i < entryCount; i++)
		{
			int objectType = FieldType.collectionType(types[i]);
			int valueType = FieldType.valueType(types[i]);
			Object value;

			switch (objectType)
			{
				case FieldType.VALUE:
					value = readValue(valueType);
					mInput.align();
					break;
				case FieldType.ARRAYLIST:
					value = readList(valueType);
					break;
				case FieldType.ARRAY:
					value = readArray(valueType);
					break;
				case FieldType.MATRIX:
					value = readMatrix(valueType);
					break;
				default:
					throw new IOException();
			}

			aBundle.put(keys[i], value, objectType, valueType);
		}

		return aBundle;
	}


	private Object readMatrix(int aValueType) throws IOException
	{
		return readSequence(aValueType, new MatrixSequence(aValueType));
	}


	private Object readArray(int aValueType) throws IOException
	{
		return readSequence(aValueType, new ArraySequence(aValueType));
	}


	private Object readList(int aValueType) throws IOException
	{
		return readSequence(aValueType, new ListSequence());
	}


	private Object readSequence(int aValueType, Sequence aSequence) throws IOException
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


	private Object readValue(int aValueType) throws IOException
	{
		switch (aValueType)
		{
			case FieldType.BOOLEAN:
				return mInput.readBit() == 1;
			case FieldType.BYTE:
				return (byte)mInput.readBits(8);
			case FieldType.SHORT:
				return (short)mInput.readVar32S();
			case FieldType.CHAR:
				return (char)mInput.readVar32();
			case FieldType.INT:
				return mInput.readVar32S();
			case FieldType.LONG:
				return mInput.readVar64S();
			case FieldType.FLOAT:
				return Float.intBitsToFloat(mInput.readVar32S());
			case FieldType.DOUBLE:
				return Double.longBitsToDouble(mInput.readVar64S());
			case FieldType.STRING:
				return readString();
			case FieldType.DATE:
				return readDate();
			case FieldType.BUNDLE:
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

		return UTF8.decodeUTF8(buf, 0, buf.length);
	}


	private static interface Sequence
	{
		public void allocate(int aSize);

		public Object read(int aaValueType) throws IOException;

		public void put(int aIndex, Object aValue);

		public Object getResult();
	}


	private class ListSequence implements Sequence
	{
		private ArrayList mList;


		@Override
		public void allocate(int aSize)
		{
			mList = new ArrayList(aSize);
		}


		@Override
		public Object read(int aValueType) throws IOException
		{
			return readValue(aValueType);
		}


		@Override
		public void put(int aIndex, Object aValue)
		{
			assert aIndex == mList.size();
			mList.add(aValue);
		}


		@Override
		public Object getResult()
		{
			return mList;
		}
	}


	private class ArraySequence implements Sequence
	{
		private int mValueType;
		private Object mArray;


		public ArraySequence(int aValueType)
		{
			mValueType = aValueType;
		}


		@Override
		public void allocate(int aSize)
		{
			mArray = Array.newInstance(FieldType.getPrimitiveType(mValueType), aSize);
		}


		@Override
		public Object read(int aValueType) throws IOException
		{
			return readValue(aValueType);
		}


		@Override
		public void put(int aIndex, Object aValue)
		{
			Array.set(mArray, aIndex, aValue);
		}


		@Override
		public Object getResult()
		{
			return mArray;
		}
	}


	private class MatrixSequence implements Sequence
	{
		private int mValueType;
		private Object mArray;


		public MatrixSequence(int aValueType)
		{
			mValueType = aValueType;
		}


		@Override
		public void allocate(int aSize)
		{
			mArray = Array.newInstance(FieldType.getPrimitiveType(mValueType), aSize, 0);
		}


		@Override
		public Object read(int aValueType) throws IOException
		{
			return readArray(aValueType);
		}


		@Override
		public void put(int aIndex, Object aValue)
		{
			Array.set(mArray, aIndex, aValue);
		}


		@Override
		public Object getResult()
		{
			return mArray;
		}
	}
}
