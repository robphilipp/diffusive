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

public class Result implements Serializable {

	private static final long serialVersionUID = -2534181021265355337L;

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
