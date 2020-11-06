package org.terifan.bundle;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;


public class PathEvaluationNGTest
{
	@Test
	public void testSomeMethod() throws IOException
	{
//		String json = "{'companyName': 'the company', 'employeeList': [{'name': 'adam', 'startDate': '1997-04-08', 'favoriteColors': [1,2]}, {'name': 'bertil', 'startDate': '1998-10-12', 'favoriteColors': [4,2]}, {'name': 'ceasar', 'startDate': '2004-01-22', 'favoriteColors': [3]}], 'favoriteColors':[0x000000, 0xff0000, 0x00ff00, 0x0000ff, 0xffff00]}";
//
//		Bundle bundle = new Bundle().unmarshalJSON(json);
//
//		byte[] data = bundle.marshal();
//
//		Bundle decoded = new Bundle().unmarshal(data, new PathEvaluation("employeeList", 1));
//
//		System.out.println(decoded);
//
//		assertEquals(decoded.marshalJSON(true), "{\"employeeList\":[{\"name\":\"bertil\",\"startDate\":\"1998-10-12\",\"favoriteColors\":[4,2]}]}");
//

		byte[] data = new Bundle().putBundle("a", new Bundle().putString("b", "B").putString("c", "C")).marshal();
		Bundle b = new Bundle().unmarshal(data, new PathEvaluation("a","c"));
		System.out.println(b);
	}
}
