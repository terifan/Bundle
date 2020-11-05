package org.terifan.bundle;


public class BundleInput
{
	private Container mContainer;


	public BundleInput(Container aContainer)
	{
		mContainer = aContainer;
	}


	public Array array()
	{
		return (Array)mContainer;
	}


	public Bundle bundle()
	{
		return (Bundle)mContainer;
	}
}
