package org.terifan.bundle;


public interface BundleProcessor<T extends Bundle>
{
	void process(Bundle aBundle);
}
