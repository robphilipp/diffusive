package org.microtitan.tests.threaded;

public class Result {

	private final int id;
	private final int numLoops;
	private final double result;
	
	public Result( final double result, final int numLoops, final int id )
	{
		this.result = result;
		this.numLoops = numLoops;
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * @return the numLoops
	 */
	public final int getNumLoops()
	{
		return numLoops;
	}

	/**
	 * @return the result
	 */
	public final double getResult()
	{
		return result;
	}
}
