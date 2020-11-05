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


	/**
	 * Sets the output produced by a writeExternal method to an Array. The output can only be set once.
	 */
	public Array array(Object... aValues)
	{
		if (mContainer != null)
		{
			throw new IllegalStateException("Output already set");
		}
		mContainer = Array.from(aValues);
		return (Array)mContainer;
	}


	/**
	 * Sets the output produced by a writeExternal method to an Array. The output can only be set once.
	 */
	public Array array(Array aArray)
	{
		if (mContainer != null)
		{
			throw new IllegalStateException("Output already set");
		}
		mContainer = aArray;
		return aArray;
	}


	/**
	 * Sets the output produced by a writeExternal method to a Bundle. The output can only be set once.
	 */
	public Bundle bundle(Bundle aBundle)
	{
		if (mContainer != null)
		{
			throw new IllegalStateException("Output already set");
		}
		mContainer = aBundle;
		return aBundle;
	}


	/**
	 * Sets the output produced by a writeExternal method to a Bundle. The output can only be set once.
	 */
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
