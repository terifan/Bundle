package org.terifan.bundle;

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
		String[] keys = writeBundleHeader(aBundle);

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.classify(value);

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
					mOutput.align();
				}
			}
		}
	}


	private String[] writeBundleHeader(Bundle aBundle) throws IOException
	{
		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);

		mOutput.writeVLC(keys.length);

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType fieldType = FieldType.classify(value);

			mOutput.writeBits(fieldType.ordinal(), 4);

			if (fieldType != FieldType.NULL && fieldType != FieldType.EMPTY)
			{
				Class<? extends Object> cls = value.getClass();

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

			mOutput.align();

			writeString(key);
		}

		return keys;
	}


	private void writeMatrix(FieldType aFieldType, Object aValue) throws ArrayIndexOutOfBoundsException, IOException, IllegalArgumentException
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
				mOutput.writeBit(Array.get(aValue, i) != null ? 1 : 0);
			}

			mOutput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object item = Array.get(aValue, i);

			if (item != null)
			{
				writeArray(aFieldType, item);
			}
		}
	}


	private void writeArray(FieldType aFieldType, Object aValue) throws IOException
	{
		int length = Array.getLength(aValue);
		boolean hasNull = false;

		if (aFieldType == FieldType.BUNDLE || aFieldType == FieldType.DATE || aFieldType == FieldType.STRING)
		{
			for (int i = 0; i < length; i++)
			{
				if (Array.get(aValue, i) == null)
				{
					hasNull = true;
					break;
				}
			}
		}

		mOutput.writeVLC(hasNull ? -length : length);

		if (hasNull)
		{
			for (int i = 0; i < length; i++)
			{
				mOutput.writeBit(Array.get(aValue, i) == null ? 1 : 0);
			}

			mOutput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object item = Array.get(aValue, i);

			if (item != null)
			{
				writeValue(aFieldType, item);
			}
		}

		mOutput.align();
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
				mOutput.writeVLC(((Date)aValue).getTime());
				break;
			case BUNDLE:
				writeBundle((Bundle)aValue);
				break;
			default:
				throw new IOException("Unsupported field type: " + aFieldType);
		}
	}


	private void writeString(String aValue) throws IOException
	{
		mOutput.writeVLC(aValue.length());
		mOutput.write(Convert.encodeUTF8(aValue));
	}
}