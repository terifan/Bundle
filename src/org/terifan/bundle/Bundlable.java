package org.terifan.bundle;

import java.io.IOException;


public interface Bundlable
{
	void readExternal(Bundle aBundle) throws IOException;

	void writeExternal(Bundle aBundle) throws IOException;

	public default <T extends Bundlable> T unmarshal(String aData)
	{
		try
		{
			Bundle bundle = new Bundle().unmarshalPSON("{\"dummy\":" + aData + "}").getBundle("dummy");
			readExternal(bundle);
			return (T)this;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
