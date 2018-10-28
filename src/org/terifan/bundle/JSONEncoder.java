package org.terifan.bundle;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;


class JSONEncoder
{
	public JSONEncoder()
	{
	}


	void marshalBundle(StringBuilder aBuilder, Bundle aBundle)
	{
		aBuilder.append("{");

		int size = aBundle.size();

		for (Entry<String, Object> entry : aBundle.entrySet())
		{
			aBuilder.append("\"").append(entry.getKey()).append("\":");

			marshal(aBuilder, entry.getValue());

			if (--size > 0)
			{
				aBuilder.append(",");
			}
		}

		aBuilder.append("}");
	}


	void marshalArray(StringBuilder aBuilder, Array aArray)
	{
		aBuilder.append("[");

		int size = aArray.size();

		for (Object value : aArray)
		{
			marshal(aBuilder, value);

			if (--size > 0)
			{
				aBuilder.append(",");
			}
		}

		aBuilder.append("]");
	}


	private void marshal(StringBuilder aBuilder, Object aValue)
	{
		if (aValue instanceof Bundle)
		{
			marshalBundle(aBuilder, (Bundle)aValue);
		}
		else if (aValue instanceof Array)
		{
			marshalArray(aBuilder, (Array)aValue);
		}
		else
		{
			marshalValue(aBuilder, aValue);
		}
	}


	void marshalValue(StringBuilder aBuilder, Object aValue)
	{
		if (aValue instanceof Date)
		{
			aValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date)aValue);
		}
		else if (aValue instanceof byte[])
		{
			aValue = Base64.getEncoder().encodeToString((byte[])aValue);
		}
		else if (aValue instanceof UUID)
		{
			aValue = aValue.toString();
		}
		else if (aValue instanceof Calendar)
		{
			Calendar calendar = (Calendar)aValue;
			int offset = calendar.getTimeZone().getRawOffset() / 60_000;
			aValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(calendar.getTimeInMillis())) + (offset < 0 ? "-" : "+") + String.format("%02d:%02d", offset / 60, offset % 60);
		}

		if (aValue instanceof String)
		{
			aBuilder.append("\"").append(aValue).append("\"");
		}
		else
		{
			aBuilder.append(aValue);
		}
	}
}
