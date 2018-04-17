package org.terifan.bundle.dev;

import java.util.ArrayList;
import java.util.Arrays;
import org.terifan.bundle.Bundle;


class BundleGenerator
{
	public static Bundle create()
	{
		return new Bundle()
			.putBoolean("boolean", true)
			.putBooleanArray("booleanArray", true,false,true,false,false,true)
			.putBooleanArrayList("booleanArrayList", new ArrayList<>(Arrays.asList(Boolean.TRUE,Boolean.FALSE,null,Boolean.FALSE,Boolean.FALSE,Boolean.TRUE)))
			.putBooleanMatrix("booleanMatrix", new boolean[][]{{true,false},{false,true},{true,true,true,true},null,{},{false}});
	}
}
