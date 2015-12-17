package org.terifan.bundle;

import org.terifan.bundle.io.BitInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
		int entryCount = mInput.readVar32();

		for (int i = 0; i < entryCount; i++)
		{
			String key = (String)readValue(FieldType.STRING);
			int fieldType = mInput.readVar32S();
			Object value;

			if (fieldType < 0)
			{
				value = null;
				fieldType = -fieldType;
			}
			else
			{
				int collectionType = FieldType.collectionType(fieldType);
				int valueType = FieldType.valueType(fieldType);

				switch (collectionType)
				{
					case FieldType.VALUE:
						value = readValue(valueType);
						mInput.align();
						break;
					case FieldType.ARRAY:
					case FieldType.ARRAYLIST:
					case FieldType.MATRIX:
						value = readSequence(collectionType, valueType);
						break;
					default:
						throw new IOException();
				}
			}

			aBundle.put(key, value, fieldType);
		}

		return aBundle;
	}


	private Object readSequence(final int aCollectionType, final int aValueType) throws IOException
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

		Object sequence;

		if (aCollectionType == FieldType.ARRAYLIST)
		{
			sequence = new ArrayList(length);
		}
		else if (aCollectionType == FieldType.ARRAY)
		{
			sequence = Array.newInstance(FieldType.getPrimitiveType(aValueType), length);
		}
		else
		{
			sequence = Array.newInstance(FieldType.getPrimitiveType(aValueType), length, 0);
		}

		for (int i = 0; i < length; i++)
		{
			Object value;

			if (flags == null || flags[i])
			{
				if (aCollectionType == FieldType.MATRIX)
				{
					value = readSequence(FieldType.ARRAY, aValueType);
				}
				else
				{
					value = readValue(aValueType);
				}
			}
			else
			{
				value = null;
			}

			if (aCollectionType == FieldType.ARRAYLIST)
			{
				((List)sequence).add(value);
			}
			else
			{
				Array.set(sequence, i, value);
			}
		}

		mInput.align();

		return sequence;
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
				byte[] buf = new byte[mInput.readVar32()];
				if (mInput.read(buf) != buf.length)
				{
					throw new IOException("Unexpected end of stream");
				}
				return UTF8.decodeUTF8(buf, 0, buf.length);
			case FieldType.DATE:
				return new Date(mInput.readVar64S());
			case FieldType.BUNDLE:
				return readBundle(new Bundle());
			default:
				throw new IOException("Unsupported field type: " + aValueType);
		}
	}
}
