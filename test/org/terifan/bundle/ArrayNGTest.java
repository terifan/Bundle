package org.terifan.bundle;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
	public void testArrayOfBundlableValues() throws IOException
	{
		Array in = Array.of(new RGB(1, 0, 0), new RGB(0, 1, 0), new RGB(0, 0, 1));

		Array out = new Array().unmarshal(in.marshal());

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[65536,256,1]");
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
	public void testArrayOfBundableValuesSingle() throws IOException
	{
		Array in = Array.of(new Position(1,2,3));

		Array out = new Array().unmarshal(in.marshal());

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[1.0,2.0,3.0]]");
	}


	@Test
	public void testArrayOfBundableValuesArray() throws IOException
	{
		Array in = Array.of(new Position(1,2,3), new Position(4,5,6));

		Array out = new Array().unmarshal(in.marshal());

		assertEquals(out, in);
		assertEquals(out.marshal(), in.marshal());
		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
		assertEquals(out.marshalJSON(true), "[[1.0,2.0,3.0],[4.0,5.0,6.0]]");
	}
}
