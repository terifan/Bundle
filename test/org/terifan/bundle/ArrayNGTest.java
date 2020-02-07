package org.terifan.bundle;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class ArrayNGTest
{
	@Test
	public void placeholder() throws IOException
	{
	}

//	@Test
//	public void testSingleArrayMixedTypes() throws IOException
//	{
//		Array in = new Array().add("one").add(1).add(3.14).add(true).add(null);
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[\"one\",1,3.14,true,null]");
//	}
//
//
//	@Test
//	public void testSingleEmptyArray() throws IOException
//	{
//		Array in = new Array();
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[]");
//	}
//
//
//	@Test
//	public void testSingleShortArraySingleTypes() throws IOException
//	{
//		Array in = new Array().add(1);
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[1]");
//	}
//
//
//	@Test(expectedExceptions = IllegalArgumentException.class)
//	public void testArrayException() throws IOException
//	{
//		new Array().add(new int[100]);
//	}
//
//
//	@Test
//	public void testMultiDimArrayOf() throws IOException
//	{
//		Array in = Array.of((Object)new int[][]{{1,2},{3,4}}); // notice cast to Object
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[[1,2],[3,4]]");
//	}
//
//
//	@Test
//	public void testMultiDimArrayMergeOf() throws IOException
//	{
//		Array in = Array.of(new int[][]{{1,2},{3,4}});
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[1,2,3,4]");
//	}
//
//
//	@Test
//	public void testArrayOf() throws IOException
//	{
//		Array in = Array.of(1,2,3,4);
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[1,2,3,4]");
//	}
//
//
//	@Test
//	public void testArrayOf2() throws IOException
//	{
//		Array in = Array.of(new int[]{1,2,3,4});
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[1,2,3,4]");
//	}
//
//
//	@Test
//	public void testSingleLongArraySingleTypes() throws IOException
//	{
//		Array in = Array.of(new int[2], new boolean[2], "hello");
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[0,0,false,false,\"hello\"]");
//	}
//
//
//	@Test
//	public void testMultiArray() throws IOException
//	{
//		Array in = new Array().add(new Array().add(new Array().add(1,2,3)));
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[[[1,2,3]]]");
//	}
//
//
//	@Test
//	public void testMultiArrayLarge() throws IOException
//	{
//		Array in = new Array().add(new Array().add(new Array().add(1,2,3), new Array().add(4,5,6))).add(new Array().add(new Array().add(7,8,9), new Array().add(10,11,12)));
//		byte[] data = in.marshal();
//		Array out = new Array().unmarshal(data);
//
//		assertEquals(out, in);
//		assertEquals(out.marshal(), in.marshal());
//		assertEquals(out.marshalJSON(true), in.marshalJSON(true));
//		assertEquals(out.marshalJSON(true), "[[[1,2,3],[4,5,6]],[[7,8,9],[10,11,12]]]");
//	}
}
