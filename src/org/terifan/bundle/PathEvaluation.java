package org.terifan.bundle;


/**
 * The PathEvaluation class allows for partial decoding of binary encoded bundles.
 * <p>
 * Example decoding part of a binary encoded bundle:
 * <pre>
 * byte[] data = new Bundle().putBundle("keyA", new Bundle().putString("keyB", "B").putString("keyC", "C")).marshal();
 * Bundle bundle = new Bundle().unmarshal(data, new PathEvaluation("keyA", "keyC"));
 * </pre>
 */
public class PathEvaluation
{
	private Object[] mPath;
	private int mOffset;


	/**
	 * Create a PathEvaluation object that will evaluate which keys to decode.
	 *
	 * @param aPath
	 *   an array of keys that will be included in a decoded Bundle.
	 */
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
