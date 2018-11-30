package org.terifan.bundle;


@FunctionalInterface
public interface Converter<T>
{
	BundlableValue convert(T aValue);
}
