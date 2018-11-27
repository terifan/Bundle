package org.terifan.bundle;


public class PathEvaluation
{
	private Object[] mPath;
	private int mOffset;


	public PathEvaluation(Object... aPath)
	{
		mPath = aPath;
	}


	PathEvaluation(int aOffset, Object[] aPath)
	{
		mOffset = aOffset;
		mPath = aPath;
	}


	boolean valid(Object aKey)
	{
		return mOffset >= mPath.length || mPath[mOffset].equals(aKey);
	}


	PathEvaluation next(Object aKey)
	{
		return new PathEvaluation(mOffset + 1, mPath);
	}
}
