package org.terifan.bundle.bundle_test;

import java.io.IOException;
import org.terifan.bundle.BinaryDecoder;
import org.terifan.bundle.BinaryEncoder;
import org.terifan.bundle.Bundlable;
import org.terifan.bundle.Bundle;
import org.terifan.bundle.TextEncoder;


public class Test
{
	public static void main(String ... args)
	{
		try
		{
			Bundle bundle = new Bundle();

			bundle.putIntMatrix("a", new int[][]{{1,2,3},null,{4,5},{}});
			bundle.putByteMatrix("b", new byte[][]{{1,2,3},null,{4,5},{}});
			bundle.putByteMatrix("c", new byte[][]{{1,2,3},{4,5,6}});
			bundle.putIntMatrix("d", new int[][]{{1,2,3},{4,5,6}});
			bundle.putIntMatrix("e", new int[0][0]);

			String text = new TextEncoder().marshal(bundle);
			System.out.println(text);

			byte[] buf = new BinaryEncoder().marshal(bundle);
			hexDump(buf);
			Bundle unmarshaled = new BinaryDecoder().unmarshal(buf);

//			Bundle unmarshaled = new TextDecoder().unmarshal(text);

			System.out.println(new TextEncoder().marshal(unmarshaled));
			System.out.println(new TextEncoder().marshal(unmarshaled).equals(text));


//			Item out = new Item();
//			out.a = 6984;
//			out.s = "test";
//			out.sub = new SubItem();
//			out.sub.a = 987;
//			out.writeExternal(bundle);
//
//			Item in = new Item();
//			in.readExternal(bundle);
//
//			System.out.println(out.a == in.a);
//			System.out.println(out.s.equals(in.s));
//			System.out.println(out.sub.a == in.sub.a);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	public static void hexDump(byte[] aBuffer)
	{
		int LW = 32;

		StringBuilder binText = new StringBuilder("");
		StringBuilder hexText = new StringBuilder("");

		for (int row = 0, i = 0; i < aBuffer.length; row++)
		{
			hexText.append(String.format("%04d: ", row * LW));

			int padding = 3 * LW + LW / 8;

			for (int j = 0; j < LW && i < aBuffer.length; j++, i++)
			{
				int c = 255 & aBuffer[i];

				hexText.append(String.format("%02x ", c));
				binText.append(Character.isISOControl(c) ? '.' : (char)c);
				padding -= 3;

				if ((j & 7) == 7)
				{
					hexText.append(" ");
					padding--;
				}
			}

			for (int j = 0; j < padding; j++)
			{
				hexText.append(" ");
			}

			System.out.println(hexText.append(binText).toString());

			binText.setLength(0);
			hexText.setLength(0);
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