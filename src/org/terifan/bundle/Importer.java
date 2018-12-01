package org.terifan.bundle;

import java.util.function.Function;


/**
 * Implementations of this interface are used to create non-bundlable class instances from Bundles.
 * 
 * <p><code>
 * Bundle in = new Bundle("{\"r\":64,\"g\":128,\"b\":255}");<br/>
 * Importer<Color> importer = b-&gt;new Color(b.getInt("r"), b.getInt("g"), b.getInt("b"));<br/>
 * Color color = in.newInstance(Color.class, importer);<br/>
 * </code></p>
 * 
 * @param <T> 
 *   type of class this Exporter can export
 */
@FunctionalInterface
public interface Importer<T> extends Function<Bundle,T>
{
}
