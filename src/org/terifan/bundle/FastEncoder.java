package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;


public class FastEncoder implements Encoder
{
	private static final char DATE = 'D';
	private static final char F = 'f';
	private static final char C = 'c';
	private static final char SH = 's';
	private static final char BY = 'b';
	private static final char X = 'x';
	private static final char L = 'l';
	private static final char D = 'd';
	private static final char I = 'i';
	private static final char B = 'B';
	private static final char S = 'S';
	private static final char NULL = 'n';
	private static final char OBJ = 'O';
	private static final char ARRAY = 'A';
	private static final char LIST = 'L';


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
		aBuffer.put(marshal(aBundle));
	}


	@Override
	public void marshal(Bundle aBundle, OutputStream aOutputStream) throws IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(aOutputStream);

		oos.writeInt(aBundle.size());

		for (String key : aBundle.keySet())
		{
			oos.writeUTF(key);
			writeValue(oos, aBundle.get(key), true);
		}

		oos.flush();
	}


	private void writeValue(ObjectOutput aOut, Object aValue, boolean aWriteType) throws IOException
	{
		char typeOf = typeOf(aValue);

		if (typeOf != 0 && aWriteType)
		{
			aOut.write(typeOf);
		}

		switch (typeOf)
		{
			case S:
				aOut.writeUTF((String)aValue);
				break;
			case X:
				aOut.writeBoolean((Boolean)aValue);
				break;
			case BY:
				aOut.writeByte((Byte)aValue);
				break;
			case SH:
				aOut.writeShort((Short)aValue);
				break;
			case C:
				aOut.writeChar((Character)aValue);
				break;
			case I:
				aOut.writeInt((Integer)aValue);
				break;
			case L:
				aOut.writeLong((Long)aValue);
				break;
			case F:
				aOut.writeFloat((Float)aValue);
				break;
			case D:
				aOut.writeDouble((Double)aValue);
				break;
			case B:
				((Bundle)aValue).writeExternal(aOut);
				break;
			case DATE:
				aOut.writeLong(((Date)aValue).getTime());
				break;
			case NULL:
				break;
			default:
				if (aValue instanceof ArrayList)
				{
					writeList(aOut, (ArrayList)aValue);
				}
				else if (aValue.getClass().isArray())
				{
					writeArray(aOut, aValue);
				}
				else
				{
					aOut.write(OBJ);
					aOut.writeObject(aValue);
				}

				break;
		}
	}


	private void writeArray(ObjectOutput aOut, Object aValue) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IOException
	{
		int len = Array.getLength(aValue);
		aOut.write(ARRAY);
		aOut.writeInt(len);
		if (len > 0)
		{
			char type = typeOf(Array.get(aValue, 0));
			aOut.write(type);
			for (int i = 0; i < len; i++)
			{
				writeValue(aOut, Array.get(aValue, i), false);
			}
		}
	}


	private void writeList(ObjectOutput aOut, ArrayList aList) throws IOException
	{
		aOut.write(LIST);
		aOut.writeInt(aList.size());
		if (aList.size() > 0)
		{
			char type = typeOf(aList.get(0));
			aOut.write(type);
			for (Object o : aList)
			{
				writeValue(aOut, o, false);
			}
		}
	}


	private char typeOf(Object aValue)
	{
		if (aValue instanceof String)
		{
			return S;
		}
		else if (aValue instanceof Bundle)
		{
			return B;
		}
		else if (aValue instanceof Integer)
		{
			return I;
		}
		else if (aValue instanceof Double)
		{
			return D;
		}
		else if (aValue instanceof Long)
		{
			return L;
		}
		else if (aValue instanceof Boolean)
		{
			return X;
		}
		else if (aValue instanceof Byte)
		{
			return BY;
		}
		else if (aValue instanceof Short)
		{
			return SH;
		}
		else if (aValue instanceof Character)
		{
			return C;
		}
		else if (aValue instanceof Float)
		{
			return F;
		}
		else if (aValue instanceof Date)
		{
			return DATE;
		}

		return 0;
	}
}