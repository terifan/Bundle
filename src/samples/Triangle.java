package samples;

import org.terifan.bundle.BundlableValue;


class Triangle implements BundlableValue<Vector[]>
{
	private Vector[] mVerticies;


	public Triangle()
	{
	}


	public Triangle(Vector... aVerticies)
	{
		mVerticies = aVerticies;
	}


	@Override
	public void readExternal(Vector[] aVectors)
	{
		mVerticies = aVectors;
	}


	@Override
	public Vector[] writeExternal()
	{
		return mVerticies;
	}
}
