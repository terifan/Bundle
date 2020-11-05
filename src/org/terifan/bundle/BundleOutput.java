package org.terifan.bundle;


public class BundleOutput
{
	private Container mContainer;


	BundleOutput()
	{
	}


	Container getContainer()
	{
		return mContainer;
	}


	public void array(Object... aValues)
	{
		if (mContainer != null)
		{
			throw new IllegalStateException("Output already set");
		}
		mContainer = Array.from(aValues);
	}


	public void bundle(Bundle aBundle)
	{
		if (mContainer != null)
		{
			throw new IllegalStateException("Output already set");
		}
		mContainer = aBundle;
	}


	public Bundle bundle()
	{
		if (mContainer != null)
		{
			throw new IllegalStateException("Output already set");
		}
		mContainer = new Bundle();
		return (Bundle)mContainer;
	}
}
