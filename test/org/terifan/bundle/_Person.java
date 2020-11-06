package org.terifan.bundle;

import java.util.Date;
import java.util.Objects;


public class _Person implements Bundlable
{
	private String mName;
	private Date mBirthdate;
	private int mHeight;
	private double mWeight;


	public _Person(String aName, Date aBirthdate, int aHeight, double aWeight)
	{
		mName = aName;
		mBirthdate = aBirthdate;
		mHeight = aHeight;
		mWeight = aWeight;
	}


	public String getName()
	{
		return mName;
	}


	public void setName(String aName)
	{
		mName = aName;
	}


	public Date getBirthdate()
	{
		return mBirthdate;
	}


	public void setBirthdate(Date aBirthdate)
	{
		mBirthdate = aBirthdate;
	}


	public int getHeight()
	{
		return mHeight;
	}


	public void setHeight(int aHeight)
	{
		mHeight = aHeight;
	}


	public double getWeight()
	{
		return mWeight;
	}


	public void setWeight(double aWeight)
	{
		mWeight = aWeight;
	}


	@Override
	public void readExternal(BundlableInput aInput)
	{
		Bundle in = aInput.bundle();
		mName = in.getString("name");
		mHeight = in.getInt("height");
		mWeight = in.getDouble("weight");
		mBirthdate = in.getSerializable("birtdate", Date.class);
	}


	@Override
	public void writeExternal(BundlableOutput aOutput)
	{
		aOutput.bundle()
			.putString("name", mName)
			.putNumber("height", mHeight)
			.putNumber("weight", mWeight)
			.putSerializable("birthdate", mBirthdate)
			;
	}


	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 23 * hash + Objects.hashCode(mName);
		hash = 23 * hash + Objects.hashCode(mBirthdate);
		hash = 23 * hash + mHeight;
		hash = 23 * hash + (int)(Double.doubleToLongBits(mWeight) ^ (Double.doubleToLongBits(mWeight) >>> 32));
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
		final _Person other = (_Person)obj;
		if (mHeight != other.mHeight)
		{
			return false;
		}
		if (Double.doubleToLongBits(mWeight) != Double.doubleToLongBits(other.mWeight))
		{
			return false;
		}
		if (!Objects.equals(mName, other.mName))
		{
			return false;
		}
		if (!Objects.equals(mBirthdate, other.mBirthdate))
		{
			return false;
		}
		return true;
	}
}
