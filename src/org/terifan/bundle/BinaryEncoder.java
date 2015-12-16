package org.terifan.bundle;

import org.terifan.bundle.io.BitOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;


public class BinaryEncoder implements Encoder
{
	private BitOutputStream mOutput;


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

		writeBundle(aBundle);

		mOutput.finish();
		mOutput = null;
	}


	private void writeBundle(Bundle aBundle) throws IOException
	{
		if (aBundle == null)
		{
			mOutput.writeVar32(0);
			return;
		}

		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);

		mOutput.writeVar32(1 + keys.length);

		for (String key : keys)
		{
			mOutput.writeVar32(aBundle.getType(key));
			writeString(key);
		}

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			int type = aBundle.getType(key);

			int objectType = FieldType.collectionType(type);
			int valueType = FieldType.valueType(type);

			switch (objectType)
			{
				case FieldType.VALUE:
					writeValue(valueType, value);
					mOutput.align();
					break;
				case FieldType.ARRAY:
					writeArray(valueType, value);
					break;
				case FieldType.ARRAYLIST:
					writeList(valueType, value);
					break;
				case FieldType.MATRIX:
					writeMatrix(valueType, value);
					break;
				default:
					throw new InternalError();
			}
		}
	}


	private void writeMatrix(int aValueType, Object aValue) throws IOException
	{
		writeSequence(new MatrixSequence(aValue, aValueType));
	}


	private void writeList(int aValueType, Object aValue) throws IOException
	{
		writeSequence(new ListSequence(aValue, aValueType));
	}


	private void writeArray( int aValueType, Object aValue) throws IOException
	{
		writeSequence(new ArraySequence(aValue, aValueType));
	}


	private void writeSequence(Sequence aSequence) throws IOException
	{
		int length = aSequence.size();
		boolean hasNull = false;

		for (int i = 0; i < length; i++)
		{
			if (aSequence.get(i) == null)
			{
				hasNull = true;
				break;
			}
		}

		mOutput.writeVar32S(hasNull ? -length : length);

		if (hasNull)
		{
			for (int i = 0; i < length; i++)
			{
				mOutput.writeBit(aSequence.get(i) == null);
			}

			mOutput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object item = aSequence.get(i);

			if (item != null)
			{
				aSequence.write(i);
			}
		}

		mOutput.align();
	}


	private void writeValue(int aValueType, Object aValue) throws IOException
	{
		switch (aValueType)
		{
			case FieldType.BOOLEAN:
				mOutput.writeBit((Boolean)aValue);
				break;
			case FieldType.BYTE:
				mOutput.writeBits(0xff & (Byte)aValue, 8);
				break;
			case FieldType.SHORT:
				mOutput.writeVar32S((Short)aValue);
				break;
			case FieldType.CHAR:
				mOutput.writeVar32((Character)aValue);
				break;
			case FieldType.INT:
				mOutput.writeVar32S((Integer)aValue);
				break;
			case FieldType.LONG:
				mOutput.writeVar64S((Long)aValue);
				break;
			case FieldType.FLOAT:
				mOutput.writeVar32S(Float.floatToIntBits((Float)aValue));
				break;
			case FieldType.DOUBLE:
				mOutput.writeVar64S(Double.doubleToLongBits((Double)aValue));
				break;
			case FieldType.STRING:
				writeString((String)aValue);
				break;
			case FieldType.DATE:
				writeDate((Date)aValue);
				break;
			case FieldType.BUNDLE:
				writeBundle((Bundle)aValue);
				break;
			default:
				throw new IOException("Unsupported field type: " + aValueType);
		}
	}


	private void writeDate(Date aValue) throws IOException
	{
		if (aValue == null)
		{
			mOutput.writeVar64(0);
		}
		else
		{
			long time = aValue.getTime();

			if (time < 0)
			{
				throw new IllegalArgumentException("Negative time not supported: " + aValue);
			}

			mOutput.writeVar64(1 + time);
		}
	}


	private void writeString(String aValue) throws IOException
	{
		if (aValue == null)
		{
			mOutput.writeVar32(0);
		}
		else
		{
			byte[] buf = UTF8.encodeUTF8(aValue);
			mOutput.writeVar32(1 + buf.length);
			mOutput.write(buf);
		}
	}


	private static interface Sequence
	{
		int size();

		Object get(int aIndex);

		void write(int aIndex) throws IOException;
	}


	private class MatrixSequence implements Sequence
	{
		private Object mValue;
		private int mValueType;


		public MatrixSequence(Object aValue, int aValueType)
		{
			mValue = aValue;
			mValueType = aValueType;
		}


		@Override
		public int size()
		{
			return Array.getLength(mValue);
		}


		@Override
		public Object get(int aIndex)
		{
			return Array.get(mValue, aIndex);
		}


		@Override
		public void write(int aIndex) throws IOException
		{
			writeArray(mValueType, get(aIndex));
		}
	}


	private class ArraySequence implements Sequence
	{
		private Object mValue;
		private int mValueType;


		public ArraySequence(Object aValue, int aValueType)
		{
			mValue = aValue;
			mValueType = aValueType;
		}


		@Override
		public int size()
		{
			return Array.getLength(mValue);
		}


		@Override
		public Object get(int aIndex)
		{
			return Array.get(mValue, aIndex);
		}


		@Override
		public void write(int aIndex) throws IOException
		{
			writeValue(mValueType, get(aIndex));
		}
	}


	private class ListSequence implements Sequence
	{
		private Object mValue;
		private int mValueType;


		public ListSequence(Object aValue, int aValueType)
		{
			mValue = aValue;
			mValueType = aValueType;
		}


		@Override
		public int size()
		{
			return ((List)mValue).size();
		}


		@Override
		public Object get(int aIndex)
		{
			return ((List)mValue).get(aIndex);
		}


		@Override
		public void write(int aIndex) throws IOException
		{
			writeValue(mValueType, get(aIndex));
		}
	}
}