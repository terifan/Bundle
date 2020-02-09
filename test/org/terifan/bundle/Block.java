package org.terifan.bundle;

import java.io.Serializable;
import java.util.Arrays;


public class Block implements Serializable
{
	private final static long serialVersionUID = 1L;

	byte[] data;


	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 41 * hash + Arrays.hashCode(this.data);
		return hash;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final Block other = (Block)obj;
		if (!Arrays.equals(this.data, other.data))
		{
			return false;
		}
		return true;
	}
}
