package org.terifan.bundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import sun.rmi.runtime.Log;


public class BinaryDecoder implements Decoder
{
	private TreeMap<Integer,String> mKeys;
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
		mKeys = new TreeMap<>();
		mInput = new BitInputStream(aInputStream);

		return readBundle(aBundle);
	}


	private Bundle readBundle(Bundle aBundle) throws IOException
	{
		String[] keys = readBundleKeys();

		for (String key : keys)
		{
			FieldType fieldType = decodeFieldType();
			Object value;

			if (fieldType == FieldType.NULL)
			{
				value = null;
			}
			else if (fieldType == FieldType.EMPTY_LIST)
			{
				value = new ArrayList();
			}
			else if (mInput.readBit() == 0)
			{
				value = readValue(fieldType);
			}
			else if (mInput.readBit() == 0)
			{
				value = readList(fieldType);
			}
			else if (fieldType == FieldType.BYTE && mInput.readBit() == 0)
			{
				value = readByteArray(fieldType);
			}
			else if (fieldType == FieldType.BYTE)
			{
				value = readByteMatrix(fieldType);
			}
			else if (mInput.readBit() == 0)
			{
				value = readArray(fieldType);
			}
			else
			{
				value = readMatrix(fieldType);
			}

			aBundle.put(key, value);
		}

		return aBundle;
	}


	private Object readArray(FieldType aFieldType) throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException, NegativeArraySizeException
	{
		ArrayList list = readList(aFieldType);
		Object value = Array.newInstance(aFieldType.getPrimitiveType(), list.size());
		for (int i = 0; i < list.size(); i++)
		{
			Array.set(value, i, list.get(i));
		}

		return value;
	}


	private Object readMatrix(FieldType aFieldType) throws IOException, ArrayIndexOutOfBoundsException, IllegalArgumentException, NegativeArraySizeException
	{
		int dim = mInput.readVariableInt(3, 4, false);
		Object value = Array.newInstance(aFieldType.getPrimitiveType(), dim, 0);
		for (int j = 0; j < dim; j++)
		{
			if (mInput.readBit() == 0)
			{
				ArrayList list = readList(aFieldType);
				Object arr = Array.newInstance(aFieldType.getPrimitiveType(), list.size());
				for (int i = 0; i < list.size(); i++)
				{
					Array.set(arr, i, list.get(i));
				}
				Array.set(value, j, arr);
			}
			else
			{
				Array.set(value, j, null);
			}
		}

		return value;
	}


	private Object readByteArray(FieldType aFieldType) throws NegativeArraySizeException, ArrayIndexOutOfBoundsException, IllegalArgumentException, IOException
	{
		Object value = new byte[mInput.readVariableInt(3, 4, false)];
		mInput.align();
		mInput.read((byte[])value);

		return value;
	}


	private Object readByteMatrix(FieldType aFieldType) throws NegativeArraySizeException, ArrayIndexOutOfBoundsException, IllegalArgumentException, IOException
	{
		Object value;

		if (mInput.readBit() == 0)
		{
			int rows = mInput.readVariableInt(3, 4, false);
			int cols = mInput.readVariableInt(3, 4, false);
			value = Array.newInstance(aFieldType.getPrimitiveType(), rows, cols);
			mInput.align();
			for (int j = 0; j < rows; j++)
			{
				mInput.read(((byte[][])value)[j]);
			}
		}
		else
		{
			int dim = mInput.readVariableInt(3, 4, false);
			value = Array.newInstance(aFieldType.getPrimitiveType(), dim, 0);
			for (int j = 0; j < dim; j++)
			{
				if (mInput.readBit() == 0)
				{
					byte[] arr = new byte[mInput.readVariableInt(3, 4, false)];
					mInput.align();
					mInput.read(arr);
					Array.set(value, j, arr);
				}
				else
				{
					Array.set(value, j, null);
				}
			}
		}

		return value;
	}


	private FieldType decodeFieldType() throws IOException
	{
		switch ((int)mInput.readBits(2))
		{
			case 0b00: return FieldType.DECODER_ORDER[0];
			case 0b01: return FieldType.DECODER_ORDER[1];
			case 0b10: return FieldType.DECODER_ORDER[2];
			default:
				switch ((int)mInput.readBits(3))
				{
					case 0b000: return FieldType.DECODER_ORDER[3];
					case 0b001: return FieldType.DECODER_ORDER[4];
					case 0b010: return FieldType.DECODER_ORDER[5];
					case 0b011: return FieldType.DECODER_ORDER[6];
					case 0b100: return FieldType.DECODER_ORDER[7];
					case 0b101: return FieldType.DECODER_ORDER[8];
					case 0b110: return FieldType.DECODER_ORDER[9];
					default:
						switch (mInput.readBit())
						{
							case 0b0: return FieldType.DECODER_ORDER[10];
							default:
								switch (mInput.readBit())
								{
									case 0b0: return FieldType.DECODER_ORDER[11];
									default: return FieldType.DECODER_ORDER[12];
								}
						}
				}
		}
	}


	private String[] readBundleKeys() throws IOException
	{
		int keyCount = mInput.readVariableInt(3, 0, false);

		String[] keys = new String[keyCount];
		ArrayList<int[]> newKeys = new ArrayList<>();

		for (int i = 0; i < keyCount; i++)
		{
			if (mInput.readBit() == 0)
			{
				keys[i] = mKeys.get((int)mInput.readBitsInRange(mKeys.size()));
			}
			else
			{
				newKeys.add(new int[]{i, (int)mInput.readVariableInt(3, 0, false)});
			}
		}

		if (newKeys.size() > 0)
		{
			mInput.align();

			for (int[] entry : newKeys)
			{
				mKeys.put(mKeys.size(), keys[entry[0]] = readString(entry[1]));
			}
		}

		return keys;
	}


	private ArrayList readList(FieldType aFieldType) throws IOException
	{
		int flags = mInput.readVariableInt(3, 0, true);
		boolean hasNulls = flags < 0;
		int len = Math.abs(flags);

		ArrayList list = new ArrayList(len);

		for (int i = 0; i < len; i++)
		{
			Object value;

			if (hasNulls && mInput.readBit() == 1)
			{
				value = null;
			}
			else
			{
				value = readValue(aFieldType);
			}

			list.add(value);
		}

		return list;
	}


	private Object readValue(FieldType aFieldType) throws IOException
	{
		switch (aFieldType)
		{
			case BOOLEAN:
				return mInput.readBit() == 1;
			case BYTE:
				return (byte)mInput.read();
			case SHORT:
				return (short)mInput.readVariableInt(3, 0, true);
			case CHAR:
				return (char)mInput.readVariableInt(3, 0, false);
			case INT:
				return mInput.readVariableInt(3, 1, true);
			case LONG:
				return mInput.readVariableLong(7, 0, true);
			case FLOAT:
				return Float.intBitsToFloat(mInput.readVariableInt(7, 0, false));
			case DOUBLE:
				return Double.longBitsToDouble(mInput.readVariableLong(7, 0, false));
			case STRING:
				int len = mInput.readVariableInt(3, 0, false);
				mInput.align();
				return readString(len);
			case DATE:
				return new Date(mInput.readVariableLong(7, 0, false));
			case BUNDLE:
				return readBundle(new Bundle());
			default:
				throw new IOException("Unsupported field type: " + aFieldType);
		}
	}


	private String readString(int aLength) throws IOException
	{
		byte[] buf = new byte[aLength];

		if (mInput.read(buf) != buf.length)
		{
			throw new IOException("Unexpected end of stream");
		}

		return Convert.decodeUTF8(buf, 0, aLength);
	}
}
