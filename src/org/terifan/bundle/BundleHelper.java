package org.terifan.bundle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Map;


public class BundleHelper
{
	public static Bundle toBundle(Point aPoint)
	{
		return aPoint == null ? null : new Bundle().putInt("x", aPoint.x).putInt("y", aPoint.y);
	}


	public static Bundle toBundle(Dimension aDimension)
	{
		return aDimension == null ? null : new Bundle().putInt("width", aDimension.width).putInt("height", aDimension.height);
	}


	public static Bundle toBundle(Rectangle aRectangle)
	{
		return aRectangle == null ? null : new Bundle().putInt("x", aRectangle.x).putInt("y", aRectangle.y).putInt("width", aRectangle.width).putInt("height", aRectangle.height);
	}


	public static String toString(Color aColor)
	{
		if (aColor == null) return null;
		if (aColor.equals(Color.BLACK)) return "BLACK";
		if (aColor.equals(Color.BLUE)) return "BLUE";
		if (aColor.equals(Color.CYAN)) return "CYAN";
		if (aColor.equals(Color.DARK_GRAY)) return "DARK_GRAY";
		if (aColor.equals(Color.GRAY)) return "GRAY";
		if (aColor.equals(Color.GREEN)) return "GREEN";
		if (aColor.equals(Color.LIGHT_GRAY)) return "LIGHT_GRAY";
		if (aColor.equals(Color.MAGENTA)) return "MAGENTA";
		if (aColor.equals(Color.ORANGE)) return "ORANGE";
		if (aColor.equals(Color.PINK)) return "PINK";
		if (aColor.equals(Color.RED)) return "RED";
		if (aColor.equals(Color.WHITE)) return "WHITE";
		if (aColor.equals(Color.YELLOW)) return "YELLOW";

		return String.format("%08x", aColor.getRGB());
	}


	public static ArrayList<Bundle> toBundleArrayList(Map<String, String> aMap)
	{
		if (aMap == null)
		{
			return null;
		}

		ArrayList<Bundle> map = new ArrayList<>();

		for (Map.Entry<String,String> entry : aMap.entrySet())
		{
			map.add(new Bundle().putString("key", entry.getKey()).putString("value", entry.getValue()));
		}

		return map;
	}


	public static Point getPoint(Bundle aBundle, Point aPoint)
	{
		return aBundle == null ? aPoint : new Point(aBundle.getInt("x", 0), aBundle.getInt("y", 0));
	}


	public static Dimension getDimension(Bundle aBundle, Dimension aDimension)
	{
		return aBundle == null ? aDimension : new Dimension(aBundle.getInt("width", 0), aBundle.getInt("height", 0));
	}


	public static Rectangle getRectangle(Bundle aBundle, Rectangle aRectangle)
	{
		return aBundle == null ? aRectangle : new Rectangle(aBundle.getInt("x", 0), aBundle.getInt("y", 0), aBundle.getInt("width", 0), aBundle.getInt("height", 0));
	}


	public static Color getColor(String aString)
	{
		if (aString == null)
		{
			return null;
		}
		switch (aString)
		{
			case "BLACK":
				return Color.BLACK;
			case "BLUE":
				return Color.BLUE;
			case "CYAN":
				return Color.CYAN;
			case "DARK_GRAY":
				return Color.DARK_GRAY;
			case "GRAY":
				return Color.GRAY;
			case "GREEN":
				return Color.GREEN;
			case "LIGHT_GRAY":
				return Color.LIGHT_GRAY;
			case "MAGENTA":
				return Color.MAGENTA;
			case "ORANGE":
				return Color.ORANGE;
			case "PINK":
				return Color.PINK;
			case "RED":
				return Color.RED;
			case "WHITE":
				return Color.WHITE;
			case "YELLOW":
				return Color.YELLOW;
		}

		return new Color(Integer.parseInt(aString, 16));
	}
}
