package org.terifan.bundle;


public class BundleInput
{
	private Container mContainer;


	BundleInput(Container aContainer)
	{
		mContainer = aContainer;
	}


	/**
	 * Returns the input provided to an readExternal method as an Array.
	 */
	public Array array()
	{
		return (Array)mContainer;
	}


	/**
	 * Returns the input provided to an readExternal method as a Bundle.
	 */
	public Bundle bundle()
	{
		return (Bundle)mContainer;
	}
}
