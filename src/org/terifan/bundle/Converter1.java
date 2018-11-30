package org.terifan.bundle;


@FunctionalInterface
public interface Converter1<T>
{
	void convert(Bundle aBundle, T aValue);
}
