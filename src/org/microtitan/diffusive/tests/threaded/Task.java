package org.microtitan.diffusive.tests.threaded;

import java.util.concurrent.Callable;

import org.microtitan.diffusive.annotations.Diffusive;

public class Task implements Callable< Result > {
	
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
	
	@Diffusive
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
