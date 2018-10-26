package org.terifan.bundle;


public interface BundlableValue<T> extends BundlableType
{
	void readExternal(T aValue);

	T writeExternal();
}
