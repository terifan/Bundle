package org.terifan.bundle;


public interface Bundlable
{
	void readExternal(BundlableInput aInput);

	void writeExternal(BundlableOutput aOutput);
}
