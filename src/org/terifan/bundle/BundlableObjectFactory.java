package org.terifan.bundle;


public interface BundlableObjectFactory<T extends Bundlable>
{
	T newInstance();
}
