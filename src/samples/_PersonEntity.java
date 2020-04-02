package samples;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


public class _PersonEntity implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String mName;
	private Date mBirthdate;
	private int mLength;
	private double mWeight;


	public _PersonEntity(String aName, Date aBirthdate, int aLength, double aWeight)
	{
		this.mName = aName;
		this.mBirthdate = aBirthdate;
		this.mLength = aLength;
		this.mWeight = aWeight;
	}


	public String getName()
	{
		return mName;
	}


	public void setName(String aName)
	{
		this.mName = aName;
	}


	public Date getBirthdate()
	{
		return mBirthdate;
	}


	public void setBirthdate(Date aBirthdate)
	{
		this.mBirthdate = aBirthdate;
	}


	public int getLength()
	{
		return mLength;
	}


	public void setLength(int aLength)
	{
		this.mLength = aLength;
	}


	public double getWeight()
	{
		return mWeight;
	}


	public void setWeight(double aWeight)
	{
		this.mWeight = aWeight;
	}


	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 23 * hash + Objects.hashCode(this.mName);
		hash = 23 * hash + Objects.hashCode(this.mBirthdate);
		hash = 23 * hash + this.mLength;
		hash = 23 * hash + (int)(Double.doubleToLongBits(this.mWeight) ^ (Double.doubleToLongBits(this.mWeight) >>> 32));
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
		final _PersonEntity other = (_PersonEntity)obj;
		if (this.mLength != other.mLength)
		{
			return false;
		}
		if (Double.doubleToLongBits(this.mWeight) != Double.doubleToLongBits(other.mWeight))
		{
			return false;
		}
		if (!Objects.equals(this.mName, other.mName))
		{
			return false;
		}
		if (!Objects.equals(this.mBirthdate, other.mBirthdate))
		{
			return false;
		}
		return true;
	}
}
