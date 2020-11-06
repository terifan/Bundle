package org.terifan.bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.terifan.bundle.BinaryConstants.*;


/**
 * var32 header (container type, version)
 * var32 length
 * nb    a single bundle or array
 *
 * [bundle]
 *    var32 key count
 *    [keys]
 *      int8 key (zero terminated utf8)
 *      var32 type
 *        [if string,array,bundle,binary]
 *          var32 length
 *          nb    value
 *        [else]
 *          nb    value
 * [array]
 *    var32 header (element count, single type, has null, is single type)
 *    [for-each-element]
 *      [if array has nulls]
 *        int8 null bitmap (one byte read every eight elements)
 *      [if array not single type]
 *        var32 type
 *      [if string,array,bundle,binary]
 *        var32 length
 *        nb    value
 *      [else]
 *        nb    value
 */
class BinaryEncoder
{
	public BinaryEncoder()
	{
	}


	public byte[] marshal(Container aContainer) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (VLCOutputStream output = new VLCOutputStream(baos))
		{
			byte[] data;
			if (aContainer instanceof Bundle)
			{
				output.writeVar32(CONTAINER_BUNDLE | VERSION);

				data = writeBundle((Bundle)aContainer);
			}
			else
			{
				output.writeVar32(CONTAINER_ARRAY | VERSION);

				data = writeArray((Array)aContainer);
			}

			output.writeVar32(data.length);
			output.write(data);
		}

		return baos.toByteArray();
	}


	private byte[] writeBundle(Bundle aBundle) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		VLCOutputStream output = new VLCOutputStream(baos);

		output.writeVar32(aBundle.size());

		for (String key : aBundle.keySet())
		{
			UTF8.encodeUTF8Z(key, output);

			Object value = aBundle.get(key);

			output.write(writeValue(value, true));
		}

		return baos.toByteArray();
	}


	private byte[] writeArray(Array aSequence) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		VLCOutputStream output = new VLCOutputStream(baos);

		int elementCount = aSequence.size();
		boolean singleType = true;
		boolean hasNull = false;
		Class type = null;

		for (Object value : aSequence)
		{
			if (value == null)
			{
				hasNull = true;
			}
			else if (type == null)
			{
				assertSupportedType(value);

				type = value.getClass();
			}
			else if (singleType)
			{
				assertSupportedType(value);

				singleType = type == value.getClass();
			}

			if (!singleType && hasNull)
			{
				break;
			}
		}

		output.writeVar32((elementCount << (singleType ? 2 + 5 : 2)) + (singleType ? TYPES.get(type) << 2 : 0) + (hasNull ? 2 : 0) + (singleType ? 1 : 0));

		for (int i = 0; i < elementCount; i+=8)
		{
			if (hasNull)
			{
				int nullBits = 0;
				for (int j = 0; j < 8 && i+j < elementCount; j++)
				{
					if (aSequence.get(i+j) == null)
					{
						nullBits |= 1 << j;
					}
				}
				output.writeInt8(nullBits);
			}

			for (int j = 0; j < 8 && i+j < elementCount; j++)
			{
				Object value = aSequence.get(i+j);

				if (value != null)
				{
					byte[] data = writeValue(value, !singleType);
					output.write(data);
				}
			}
		}

		return baos.toByteArray();
	}


	private byte[] writeValue(Object aValue, boolean aIncludeType) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		VLCOutputStream output = new VLCOutputStream(baos);

		int type;
		if (aValue == null)
		{
			type = NULL;
			aValue = "dummy";
		}
		else
		{
			type = TYPES.get(aValue.getClass());
		}

		if (aIncludeType)
		{
			output.writeInt8(type);
		}

		switch (type)
		{
			case NULL:
				break;
			case BOOLEAN:
				output.writeInt8((Boolean)aValue ? 1 : 0);
				break;
			case BYTE:
				output.writeInt8(0xff & (Byte)aValue);
				break;
			case SHORT:
				output.writeVar32S((Short)aValue);
				break;
			case INT:
				output.writeVar32S((Integer)aValue);
				break;
			case LONG:
				output.writeVar64S((Long)aValue);
				break;
			case FLOAT:
				output.writeInt32(Float.floatToIntBits((Float)aValue));
				break;
			case DOUBLE:
				output.writeInt64(Double.doubleToLongBits((Double)aValue));
				break;
			case STRING:
			case BUNDLE:
			case ARRAY:
			case BINARY:
				byte[] buf;

				switch (type)
				{
					case STRING:
						buf = UTF8.encodeUTF8((String)aValue);
						break;
					case BUNDLE:
						buf = writeBundle((Bundle)aValue);
						break;
					case ARRAY:
						buf = writeArray((Array)aValue);
						break;
					case BINARY:
						buf = (byte[])aValue;
						break;
					default:
						throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
				}

				output.writeVar32(buf.length);
				output.write(buf);
				break;
			default:
				throw new IllegalArgumentException("Unsupported type: " + aValue.getClass());
		}

		return baos.toByteArray();
	}
}
