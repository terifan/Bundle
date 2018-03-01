package org.terifan.bundle;


@FunctionalInterface
public interface BundleVisitor
{
	default void entering(Bundle aParentBundle, String aKey, Bundle aChildBundle)
	{
	}

	default void leaving(Bundle aParentBundle, String aKey, Bundle aChildBundle)
	{
	}

	Object process(Bundle aBundle, String aKey, Object aValue);
}
