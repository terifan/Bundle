package org.terifan.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public interface Decoder
{
	Bundle unmarshal(byte[] aBuffer) throws IOException;


	Bundle unmarshal(ByteBuffer aBuffer) throws IOException;


	Bundle unmarshal(InputStream aInputStream) throws IOException;


	Bundle unmarshal(Bundle aBundle, byte[] aBuffer) throws IOException;


	Bundle unmarshal(Bundle aBundle, ByteBuffer aBuffer) throws IOException;


	Bundle unmarshal(Bundle aBundle, InputStream aInputStream) throws IOException;
}
