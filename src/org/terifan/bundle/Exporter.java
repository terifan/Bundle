package org.terifan.bundle;


/**
 * Implementations of this interface are used to export non-bundlable classes to Bundles.
 * 
 * <p><code>
 * Color color = new Color(64,128,255);<br/>
 * Exporter<Color> exporter = (b,c)-&gt;b.putNumber("r", c.getRed()).putNumber("g",c.getGreen()).putNumber("b",c.getBlue());<br/>
 * Bundle in = Bundle.of(color, exporter);<br/>
 * </code></p>
 * 
 * @param <T> 
 *   type of class this Exporter can export
 */
@FunctionalInterface
public interface Exporter<T>
{
	void convert(Bundle aBundle, T aValue);
}
