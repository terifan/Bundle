package org.terifan.bundle;

import java.io.IOException;


public interface Bundlable
{
	void readExternal(Bundle aBundle) throws IOException;

	void writeExternal(Bundle aBundle) throws IOException;
}
