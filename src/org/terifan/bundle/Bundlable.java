package org.terifan.bundle;


public interface Bundlable<T extends Container>
{
	void readExternal(T aBundle);

	void writeExternal(T aBundle);
}
