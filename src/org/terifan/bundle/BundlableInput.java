package org.terifan.bundle;


/**
 * Provides input to a Bundlable readExternal implementation.
 */
public class BundlableInput
{
	private Container mContainer;


	BundlableInput(Container aContainer)
	{
		mContainer = aContainer;
	}


	/**
	 * The readExternal implementation must call the appropriate method. Calling this method when serialized value is a Bundle will result
	 * in an exception.
	 *
	 * @return
	 *   the input provided to an readExternal method as an Array.
	 * @throws IllegalArgumentException
	 *   when the BundlableInput is wrong type
	 */
	public Array array() throws IllegalArgumentException
	{
		if (mContainer instanceof Bundle)
		{
			throw new IllegalArgumentException("This BundlableInput contains a Bundle, ensure the readExternal method use the same operations as writeExternal.");
		}
		return (Array)mContainer;
	}


	/**
	 * The readExternal implementation must call the appropriate method. Calling this method when serialized value is an Array will result
	 * in an exception.
	 *
	 * @return
	 *   the input provided to an readExternal method as a Bundle.
	 * @throws IllegalArgumentException
	 *   when the BundlableInput is wrong type
	 */
	public Bundle bundle() throws IllegalArgumentException
	{
		if (mContainer instanceof Array)
		{
			throw new IllegalArgumentException("This BundlableInput contains an Array, ensure the readExternal method use the same operations as writeExternal.");
		}
		return (Bundle)mContainer;
	}
}
