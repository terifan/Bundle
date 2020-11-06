package org.terifan.bundle;


/**
 * Provides output to a Bundlable writeExternal implementation.
 */
public class BundlableOutput
{
	private Container mContainer;


	BundlableOutput()
	{
	}


	Container getContainer()
	{
		return mContainer;
	}


	/**
	 * Sets the output produced by a writeExternal method to an Array. The output can only be set once.
	 *
	 * @throws IllegalStateException
	 *   if this or any other method has already been called.
	 */
	public Array array(Object... aValues) throws IllegalStateException
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
	 *
	 * @throws IllegalStateException
	 *   if this or any other method has already been called.
	 */
	public Array array(Array aArray) throws IllegalStateException
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
	 *
	 * @throws IllegalStateException
	 *   if this or any other method has already been called.
	 */
	public Bundle bundle(Bundle aBundle) throws IllegalStateException
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
	 *
	 * @throws IllegalStateException
	 *   if this or any other method has already been called.
	 */
	public Bundle bundle() throws IllegalStateException
	{
		if (mContainer != null)
		{
			throw new IllegalStateException("Output already set");
		}

		mContainer = new Bundle();

		return (Bundle)mContainer;
	}
}
