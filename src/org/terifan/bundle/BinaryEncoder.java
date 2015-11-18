package org.terifan.bundle;

import org.terifan.bundle.io.BitOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import org.terifan.bundle.bundle_test.Log;


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
			mOutput.writeVLC(-1);
			return;
		}

		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);

		mOutput.writeVLC(keys.length);

		for (String key : keys)
		{
			int type = aBundle.getType(key);
			mOutput.writeBits(type >> 8, 2);
			mOutput.writeBits(type & 0xff, 4);
		}

		mOutput.align();

		for (String key : keys)
		{
			writeString(key);
		}

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			int type = aBundle.getType(key);

			ObjectType objectType = ObjectType.values()[type >> 8];
			ValueType valueType = ValueType.values()[type & 0xff];

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

		mOutput.writeVLC(hasNull ? -length : length);

		if (hasNull)
		{
			for (int i = 0; i < length; i++)
			{
				mOutput.writeBit(Array.get(aValue, i) == null);
			}

			mOutput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object item = Array.get(aValue, i);

			if (item != null)
			{
				writeArray(aValueType, item);
			}
		}
	}


	private void writeList(ValueType aValueType, Object aValue) throws IOException
	{
		List list = (List)aValue;
		int length = list.size();
		boolean hasNull = false;

		for (int i = 0; i < length; i++)
		{
			if (list.get(i) == null)
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
				mOutput.writeBit(list.get(i) == null);
			}

			mOutput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object item = list.get(i);

			if (item != null)
			{
				writeValue(aValueType, item);
			}
		}

		mOutput.align();
	}


	private void writeArray(ValueType aValueType, Object aValue) throws IOException
	{
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

		mOutput.writeVLC(hasNull ? -length : length);

		if (hasNull)
		{
			for (int i = 0; i < length; i++)
			{
				mOutput.writeBit(Array.get(aValue, i) == null);
			}

			mOutput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object item = Array.get(aValue, i);

			if (item != null)
			{
				writeValue(aValueType, item);
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
				mOutput.writeVLC((Character)aValue);
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
			mOutput.writeVLC(-1);
		}
		else
		{
			long time = aValue.getTime();

			if (time < 0)
			{
				throw new IllegalArgumentException("Negative time not supported: " + aValue);
			}

			mOutput.writeVLC(time);
		}
	}


	private void writeString(String aValue) throws IOException
	{
		if (aValue == null)
		{
			mOutput.writeVLC(-1);
		}
		else
		{
			byte[] buf = Convert.encodeUTF8(aValue);
			mOutput.writeVLC(buf.length);
			mOutput.write(buf);
		}
	}
}