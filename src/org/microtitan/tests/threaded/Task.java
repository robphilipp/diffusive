/*
 * Copyright 2012 Robert Philipp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.microtitan.tests.threaded;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.microtitan.diffusive.annotations.Diffusive;

public class Task implements Callable< Result >, Serializable {
	
	private static final long serialVersionUID = 9046316268426010492L;

	private final int id;
	private final int numInnerLoops;
	
	public Task( final int id, final int numLooops )
	{
		this.id = id;
		this.numInnerLoops = numLooops;
	}

	@Diffusive
	@Override
	public Result call()
	{
		double result = 0;
		for( int i = 0; i < 10_000; ++i )
		{
//			for( int j = 0; j < numInnerLoops; ++j )
//			{
//				result += i / ( j + 1 );
//			}
			result += loop( new int[] {numInnerLoops, i} );
		}
		return new Result( result, numInnerLoops, id );
	}
	
//	@Diffusive
//	public double loop( final int numLoops, final int currentLoop )
	public double loop( final int numLoops[] )
	{
		double result = 0;
		for( int i = 0; i < numLoops[0]; ++i )
		{
			result += numLoops[1] / ( i + 1 );
		}
		return result;
	}

}
