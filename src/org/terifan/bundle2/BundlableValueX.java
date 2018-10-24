package org.terifan.bundle2;


public interface BundlableValueX<T>
{
	void readExternal(T aValue);

	T writeExternal();
}
