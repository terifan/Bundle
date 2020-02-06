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
		return aPoint == null ? null : new Bundle().putNumber("x", aPoint.x).putNumber("y", aPoint.y);
	}


	public static Bundle toBundle(Dimension aDimension)
	{
		return aDimension == null ? null : new Bundle().putNumber("width", aDimension.width).putNumber("height", aDimension.height);
	}


	public static Bundle toBundle(Rectangle aRectangle)
	{
		return aRectangle == null ? null : new Bundle().putNumber("x", aRectangle.x).putNumber("y", aRectangle.y).putNumber("width", aRectangle.width).putNumber("height", aRectangle.height);
	}


	public static Array toArray(Point aPoint)
	{
		return aPoint == null ? null : new Array(aPoint.x, aPoint.y);
	}


	public static Array toArray(Dimension aDimension)
	{
		return aDimension == null ? null : new Array(aDimension.width, aDimension.height);
	}


	public static Array toArray(Rectangle aRectangle)
	{
		return aRectangle == null ? null : new Array(aRectangle.x, aRectangle.y, aRectangle.width, aRectangle.height);
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


	public static Array toArray(Map<String, ?> aMap)
	{
		if (aMap == null)
		{
			return null;
		}

		Array array = new Array();

		for (Map.Entry<String,?> entry : aMap.entrySet())
		{
			array.add(new Bundle().putString("key", entry.getKey()).set("value", entry.getValue()));
		}

		return array;
	}


	public static Point getPoint(Bundle aBundle, Point aPoint)
	{
		return aBundle == null ? aPoint : new Point(aBundle.getInt("x"), aBundle.getInt("y"));
	}


	public static Dimension getDimension(Bundle aBundle, Dimension aDimension)
	{
		return aBundle == null ? aDimension : new Dimension(aBundle.getInt("width"), aBundle.getInt("height"));
	}


	public static Rectangle getRectangle(Bundle aBundle, Rectangle aRectangle)
	{
		return aBundle == null ? aRectangle : new Rectangle(aBundle.getInt("x"), aBundle.getInt("y"), aBundle.getInt("width"), aBundle.getInt("height"));
	}


	public static Point getPoint(Array aArray, Point aPoint)
	{
		return aArray == null ? aPoint : new Point(aArray.getInt(0), aArray.getInt(1));
	}


	public static Dimension getDimension(Array aArray, Dimension aDimension)
	{
		return aArray == null ? aDimension : new Dimension(aArray.getInt(0), aArray.getInt(1));
	}


	public static Rectangle getRectangle(Array aArray, Rectangle aRectangle)
	{
		return aArray == null ? aRectangle : new Rectangle(aArray.getInt(0), aArray.getInt(1), aArray.getInt(2), aArray.getInt(3));
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
