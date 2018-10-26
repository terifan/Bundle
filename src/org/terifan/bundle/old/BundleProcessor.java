package org.terifan.bundle.old;


public interface BundleProcessor<T extends Bundle>
{
	void process(Bundle aBundle);
}
