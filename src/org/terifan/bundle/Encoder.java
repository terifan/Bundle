package org.terifan.bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;


public interface Encoder
{
	byte[] marshal(Bundle aBundle) throws IOException;


	void marshal(Bundle aBundle, ByteBuffer aBuffer) throws IOException;


	void marshal(Bundle aBundle, OutputStream aOutputStream) throws IOException;
}