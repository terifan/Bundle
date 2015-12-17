package org.terifan.bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;


class BinaryEncoder
{
	private BitOutputStream mOutput;


	public void marshal(Bundle aBundle, OutputStream aOutputStream) throws IOException
	{
		mOutput = new BitOutputStream(aOutputStream);

		mOutput.writeVar32(0); // version
		
		writeBundle(aBundle);

		mOutput.finish();
		mOutput = null;
	}


	private void writeBundle(Bundle aBundle) throws IOException
	{
		mOutput.writeVar32(aBundle.size());

		for (String key : aBundle.keySet())
		{
			Object value = aBundle.get(key);
			int fieldType = aBundle.getType(key);

			writeValue(key, FieldType.STRING);
			mOutput.writeVar32S(value == null ? -fieldType : fieldType);

			if (value != null)
			{
				int collectionType = FieldType.collectionType(fieldType);
				int valueType = FieldType.valueType(fieldType);

				switch (collectionType)
				{
					case FieldType.VALUE:
						writeValue(value, valueType);
						mOutput.align();
						break;
					case FieldType.ARRAY:
					case FieldType.ARRAYLIST:
					case FieldType.MATRIX:
						writeSequence(value, collectionType, valueType);
						break;
					default:
						throw new InternalError();
				}
			}
		}
	}


	private void writeSequence(Object aSequence, final int aCollectionType, final int aValueType) throws IOException
	{
		final int length;

		if (aCollectionType == FieldType.ARRAYLIST)
		{
			length = ((List)aSequence).size();
		}
		else
		{
			length = Array.getLength(aSequence);
		}

		boolean hasNull = false;

		for (int i = 0; i < length; i++)
		{
			Object value = getElement(aCollectionType, aSequence, i);

			if (value == null)
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
				Object value = getElement(aCollectionType, aSequence, i);

				mOutput.writeBit(value == null);
			}

			mOutput.align();
		}

		for (int i = 0; i < length; i++)
		{
			Object value = getElement(aCollectionType, aSequence, i);

			if (value != null)
			{
				if (aCollectionType == FieldType.MATRIX)
				{
					writeSequence(value, FieldType.ARRAY, aValueType);
				}
				else
				{
					writeValue(value, aValueType);
				}
			}
		}

		mOutput.align();
	}


	private Object getElement(int aCollectionType, Object aValue, int aIndex)
	{
		Object value;

		if (aCollectionType == FieldType.ARRAYLIST)
		{
			value = ((List)aValue).get(aIndex);
		}
		else
		{
			value = Array.get(aValue, aIndex);
		}
		
		return value;
	}


	private void writeValue(Object aValue, int aValueType) throws IOException
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
				byte[] buf = UTF8.encodeUTF8((String)aValue);
				mOutput.writeVar32(buf.length);
				mOutput.write(buf);
				break;
			case FieldType.DATE:
				mOutput.writeVar64S(((Date)aValue).getTime());
				break;
			case FieldType.BUNDLE:
				writeBundle((Bundle)aValue);
				break;
			default:
				throw new IOException("Unsupported field type: " + aValueType);
		}
	}
}