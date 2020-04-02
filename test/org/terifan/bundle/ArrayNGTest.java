package org.terifan.bundle;

import java.awt.Point;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class ArrayNGTest
{
	@Test
	public void testSingleArrayMixedTypes() throws IOException
	{
		Array in = new Array().add("one").add(1).add(3.14).add(true).add(null);
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[\"one\",1,3.14,true,null]");
	}


	@Test
	public void testSingleEmptyArray() throws IOException
	{
		Array in = new Array();
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[]");
	}


	@Test
	public void testSingleShortArraySingleTypes() throws IOException
	{
		Array in = new Array().add(1);
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[1]");
	}


	@Test
	public void testAddNull() throws IOException
	{
		Array in = new Array().add(null);

		assertNull(in.get(0));
	}


	@Test
	public void testOfNull() throws IOException
	{
		Array in = Array.of(null);

		assertNull(in.get(0));
	}


	@Test
	public void testArrayOf1Vararg() throws IOException
	{
		Array in = Array.of(1,2,3,4);
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[1,2,3,4]");
	}


	@Test
	public void testArrayOf1Object() throws IOException
	{
		int[] arr = {1,2,3,4};
		Array in = Array.of(arr);
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[1,2,3,4]");
	}


	@Test
	public void testArrayOf2Vararg() throws IOException
	{
		Array in = Array.of(new int[][]{{1,2},{3,4}});
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[1,2],[3,4]]");
	}


	@Test
	public void testArrayOf2Object() throws IOException
	{
		int[][] arr = {{1,2},{3,4}};
		Array in = Array.of(arr);
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[1,2],[3,4]]");
	}


	@Test
	public void testArrayOf3Vararg() throws IOException
	{
		Array in = Array.of(new int[][][]{{{1,2},{3,4}},{{5,6},{7,8}}});
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[[1,2],[3,4]],[[5,6],[7,8]]]");
	}


	@Test
	public void testArrayOf3Object() throws IOException
	{
		int[][][] arr = {{{1,2},{3,4}},{{5,6},{7,8}}};
		Array in = Array.of(arr);
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[[1,2],[3,4]],[[5,6],[7,8]]]");
	}


	@Test
	public void testArrayOfDifferentTypes() throws IOException
	{
		Array in = Array.of(new int[2], new boolean[2], "hello");
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[0,0],[false,false],\"hello\"]");
	}


	@Test
	public void testMultiArray1() throws IOException
	{
		Array in = new Array().add(new Array().add(new Array().add(1,2,3)));
		byte[] data = in.marshal();
		Array out = new Array().unmarshal(data);

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[[1,2,3]]]");
	}


	@Test
	public void testMultiArray2() throws IOException
	{
		Array odd = Array.of(1,3,5,7);
		Array even = Array.of(2,4,6,8);
		Array in = Array.of(odd, even);

		Array out = new Array().unmarshal(in.marshal());

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[1,3,5,7],[2,4,6,8]]");
	}


	@Test
	public void testArrayOfList() throws IOException
	{
		List<Integer> list = Arrays.asList(1,2,3,4);

		Array in = Array.of(list);

		Array out = new Array().unmarshal(in.marshal());

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[1,2,3,4]");
	}


	@Test
	public void testArrayOfListOfArray() throws IOException
	{
		List<int[]> list = Arrays.asList(new int[]{1,2}, new int[]{3,4});

		Array in = Array.of(list);

		Array out = new Array().unmarshal(in.marshal());

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[1,2],[3,4]]");
	}


	@Test
	public void testSerializable() throws IOException
	{
		Point point = new Point(0,0);

		Array out = Array.of(point);

		Point in = out.getSerializable(Point.class, 0);

		assertEquals(point, in);
//		assertEquals(out.marshalJSON(true), "[\"rO0ABXNyAA5qYXZhLmF3dC5Qb2ludLbEinI0fsgmAgACSQABeEkAAXl4cAAAAAAAAAAA\"]");
	}


	@Test
	public void testBinary() throws IOException
	{
		byte[] data = "binarydata".getBytes();

		Array out = new Array().putBinary(0, data);

		byte[] in = out.getBinary(0);

		assertEquals(data, in);
		assertEquals(out.marshalJSON(true), "[\"YmluYXJ5ZGF0YQ==\"]");
	}


	@Test
	public void testAddArray() throws IOException
	{
		Array out = new Array().add(new int[]{1,2,3});

		assertEquals(out.marshalJSON(true), "[1,2,3]");
	}


	@Test
	public void testAddList() throws IOException
	{
		Array out = new Array().add(Arrays.asList(1,2,3));

		assertEquals(out.marshalJSON(true), "[1,2,3]");
	}


	@Test
	public void testAddStream() throws IOException
	{
		Array out = new Array().add(Stream.of(1,2,3));

		assertEquals(out.marshalJSON(true), "[1,2,3]");
	}


	@Test
	public void testArrayOfStream() throws IOException
	{
		Array out = Array.of(Stream.of(1,2,3));

		assertEquals(out.marshalJSON(true), "[1,2,3]");
	}


	@Test
	public void testArrayOfStream3() throws IOException
	{
		Array out = Array.of(Stream.of(Stream.of(1,2),Stream.of(3,4),Stream.of(5,6)));

		assertEquals(out.marshalJSON(true), "[[1,2],[3,4],[5,6]]");
	}
}
