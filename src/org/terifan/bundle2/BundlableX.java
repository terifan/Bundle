package org.terifan.bundle2;


public interface BundlableX extends BundlableTypeX
{
	void readExternal(BundleX aBundle);

	void writeExternal(BundleX aBundle);
}
