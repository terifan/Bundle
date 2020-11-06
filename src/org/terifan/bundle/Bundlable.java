package org.terifan.bundle;


public interface Bundlable
{
	void readExternal(BundlableInput aBundle);

	void writeExternal(BundlableOutput aBundle);
}
