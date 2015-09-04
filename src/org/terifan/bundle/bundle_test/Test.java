package org.terifan.bundle.bundle_test;

import java.io.IOException;
import org.terifan.bundle.Bundlable;
import org.terifan.bundle.Bundle;


public class Test
{
	public static void main(String ... args)
	{
		try
		{
			Bundle bundle = new Bundle();

			Item out = new Item();
			out.a = 6984;
			out.s = "test";
			out.sub = new SubItem();
			out.sub.a = 987;
			out.writeExternal(bundle);

			Item in = new Item();
			in.readExternal(bundle);

			System.out.println(out.a == in.a);
			System.out.println(out.s.equals(in.s));
			System.out.println(out.sub.a == in.sub.a);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}


class Item implements Bundlable
{
	int a;
	String s;
	SubItem sub;


	@Override
	public void readExternal(Bundle aBundle) throws IOException
	{
		a = aBundle.getInt("a");
		s = aBundle.getString("s");
		sub = new SubItem();
		aBundle.getBundlable("item", sub);
	}


	@Override
	public void writeExternal(Bundle aBundle) throws IOException
	{
		aBundle.putInt("a", a);
		aBundle.putString("s", s);
		aBundle.putBundlable("item", sub);
	}
}


class SubItem implements Bundlable
{
	int a;


	@Override
	public void readExternal(Bundle aBundle) throws IOException
	{
		aBundle.putInt("a", a);
	}


	@Override
	public void writeExternal(Bundle aBundle) throws IOException
	{
		a = aBundle.getInt("a");
	}
}