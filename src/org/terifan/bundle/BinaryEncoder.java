package org.terifan.bundle;

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
		String[] keys = writeBundleHeader(aBundle);

		for (String key : keys)
		{
			Object value = aBundle.get(key);
			FieldType2 fieldType = aBundle.getType(key);

			if (fieldType.name().endsWith("_MATRIX"))
			{
				writeMatrix(fieldType, value);
			}
			else if (fieldType.name().endsWith("_ARRAY"))
			{
				writeArray(fieldType, value);
			}
			else if (fieldType.name().endsWith("_ARRAYLIST"))
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


	private String[] writeBundleHeader(Bundle aBundle) throws IOException
	{
		String[] keys = aBundle.keySet().toArray(new String[aBundle.size()]);

		mOutput.writeVLC(keys.length);

		for (String key : keys)
		{
			mOutput.writeBits(aBundle.getType(key).ordinal(), 6);
		}

		mOutput.align();

		for (String key : keys)
		{
			writeString(key);
		}

		return keys;
	}


	private void writeMatrix(FieldType2 aFieldType, Object aValue) throws ArrayIndexOutOfBoundsException, IOException, IllegalArgumentException
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
				writeArray(aFieldType, item);
			}
		}
	}


	private void writeArray(FieldType2 aFieldType, Object aValue) throws IOException
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
				writeValue(aFieldType, item);
			}
		}

		mOutput.align();
	}


	private void writeValue(FieldType2 aFieldType, Object aValue) throws IOException
	{
		switch (aFieldType)
		{
			case BOOLEAN:
			case BOOLEAN_ARRAY:
			case BOOLEAN_ARRAYLIST:
			case BOOLEAN_MATRIX:
				mOutput.writeBit((Boolean)aValue);
				break;
			case BYTE:
			case BYTE_ARRAY:
			case BYTE_ARRAYLIST:
			case BYTE_MATRIX:
				mOutput.writeBits(0xff & (Byte)aValue, 8);
				break;
			case SHORT:
			case SHORT_ARRAY:
			case SHORT_ARRAYLIST:
			case SHORT_MATRIX:
				mOutput.writeVLC((Short)aValue);
				break;
			case CHAR:
			case CHAR_ARRAY:
			case CHAR_ARRAYLIST:
			case CHAR_MATRIX:
				mOutput.writeVLC((Character)aValue);
				break;
			case INT:
			case INT_ARRAY:
			case INT_ARRAYLIST:
			case INT_MATRIX:
				mOutput.writeVLC((Integer)aValue);
				break;
			case LONG:
			case LONG_ARRAY:
			case LONG_ARRAYLIST:
			case LONG_MATRIX:
				mOutput.writeVLC((Long)aValue);
				break;
			case FLOAT:
			case FLOAT_ARRAY:
			case FLOAT_ARRAYLIST:
			case FLOAT_MATRIX:
				mOutput.writeVLC(Float.floatToIntBits((Float)aValue));
				break;
			case DOUBLE:
			case DOUBLE_ARRAY:
			case DOUBLE_ARRAYLIST:
			case DOUBLE_MATRIX:
				mOutput.writeVLC(Double.doubleToLongBits((Double)aValue));
				break;
			case STRING:
			case STRING_ARRAY:
			case STRING_ARRAYLIST:
			case STRING_MATRIX:
				writeString((String)aValue);
				break;
			case DATE:
			case DATE_ARRAY:
			case DATE_ARRAYLIST:
			case DATE_MATRIX:
				mOutput.writeVLC(((Date)aValue).getTime());
				break;
			case BUNDLE:
			case BUNDLE_ARRAY:
			case BUNDLE_ARRAYLIST:
			case BUNDLE_MATRIX:
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