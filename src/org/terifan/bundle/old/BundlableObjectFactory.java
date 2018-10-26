package org.terifan.bundle.old;


/**
 * Create a new instance of T
 */
public interface BundlableObjectFactory<T extends Bundlable>
{
	T newInstance();
}
