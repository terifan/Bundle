package org.terifan.bundle;

import java.io.Serializable;


public class Preview implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Bundle mProperties;
	private byte[] mContent;


	public Preview(Bundle aProperties, byte[] aContent)
	{
		mProperties = aProperties;
		mContent = aContent;
	}


	public Bundle getProperties()
	{
		return mProperties;
	}


	public void setProperties(Bundle aProperties)
	{
		this.mProperties = aProperties;
	}


	public byte[] getContent()
	{
		return mContent;
	}


	public void setContent(byte[] aContent)
	{
		this.mContent = aContent;
	}
}
