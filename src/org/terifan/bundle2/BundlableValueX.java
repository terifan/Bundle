package org.terifan.bundle2;


public interface BundlableValueX<T> extends BundlableTypeX
{
	void readExternal(T aValue);

	T writeExternal();
}
