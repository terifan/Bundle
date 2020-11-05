package org.terifan.bundle;


public interface Bundlable
{
	void readExternal(BundleInput aBundle);

	void writeExternal(BundleOutput aBundle);
}
