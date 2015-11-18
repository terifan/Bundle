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
			mOutput.writeUVLC(0);
			return;
		}

		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);

		mOutput.writeUVLC(1 + keys.length);

		for (String key : keys)
		{
			mOutput.writeBits(aBundle.getType(key), 8);
			writeString(key);
		}

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			int type = aBundle.getType(key);

			ObjectType objectType = ObjectType.values()[type >> 4];
			ValueType valueType = ValueType.values()[type & 15];

			switch (objectType)
			{
				case VALUE:
					writeValue(valueType, value);
					mOutput.align();
					break;
				case ARRAY:
					writeArray(valueType, value);
					break;
				case ARRAYLIST:
					writeList(valueType, value);
					break;
				case MATRIX:
					writeMatrix(valueType, value);
					break;
				default:
					throw new InternalError();
			}
		}
	}


	private void writeMatrix(ValueType aValueType, Object aValue) throws IOException
	{
		writeSequence(new Sequence()
		{
			@Override
			public int size()
			{
				return Array.getLength(aValue);
			}

			@Override
			public Object get(int aIndex)
			{
				return Array.get(aValue, aIndex);
			}

			@Override
			public void write(int aIndex) throws IOException
			{
				writeArray(aValueType, get(aIndex));
			}
		});
	}


	private void writeList(ValueType aValueType, Object aValue) throws IOException
	{
		writeSequence(new Sequence()
		{
			@Override
			public int size()
			{
				return ((List)aValue).size();
			}

			@Override
			public Object get(int aIndex)
			{
				return ((List)aValue).get(aIndex);
			}

			@Override
			public void write(int aIndex) throws IOException
			{
				writeValue(aValueType, get(aIndex));
			}
		});
	}


	private void writeArray(final ValueType aValueType, final Object aValue) throws IOException
	{
		writeSequence(new Sequence()
		{
			@Override
			public int size()
			{
				return Array.getLength(aValue);
			}

			@Override
			public Object get(int aIndex)
			{
				return Array.get(aValue, aIndex);
			}

			@Override
			public void write(int aIndex) throws IOException
			{
				writeValue(aValueType, get(aIndex));
			}
		});
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

		mOutput.writeVLC(hasNull ? -length : length);

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


	private void writeValue(ValueType aValueType, Object aValue) throws IOException
	{
		switch (aValueType)
		{
			case BOOLEAN:
				mOutput.writeBit((Boolean)aValue);
				break;
			case BYTE:
				mOutput.writeBits(0xff & (Byte)aValue, 8);
				break;
			case SHORT:
				mOutput.writeVLC((Short)aValue);
				break;
			case CHAR:
				mOutput.writeUVLC((Character)aValue);
				break;
			case INT:
				mOutput.writeVLC((Integer)aValue);
				break;
			case LONG:
				mOutput.writeVLC((Long)aValue);
				break;
			case FLOAT:
				mOutput.writeVLC(Float.floatToIntBits((Float)aValue));
				break;
			case DOUBLE:
				mOutput.writeVLC(Double.doubleToLongBits((Double)aValue));
				break;
			case STRING:
				writeString((String)aValue);
				break;
			case DATE:
				writeDate((Date)aValue);
				break;
			case BUNDLE:
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
			mOutput.writeUVLC(0);
		}
		else
		{
			long time = aValue.getTime();

			if (time < 0)
			{
				throw new IllegalArgumentException("Negative time not supported: " + aValue);
			}

			mOutput.writeUVLC(1 + time);
		}
	}


	private void writeString(String aValue) throws IOException
	{
		if (aValue == null)
		{
			mOutput.writeUVLC(0);
		}
		else
		{
			byte[] buf = Convert.encodeUTF8(aValue);
			mOutput.writeUVLC(1 + buf.length);
			mOutput.write(buf);
		}
	}


	private static interface Sequence
	{
		int size();

		Object get(int aIndex);

		void write(int aIndex) throws IOException;
	}
}