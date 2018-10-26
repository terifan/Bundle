package org.terifan.bundle;


public interface Bundlable extends BundlableType
{
	void readExternal(Bundle aBundle);

	void writeExternal(Bundle aBundle);
}
