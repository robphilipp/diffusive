/*
 * Copyright 2013 Robert Philipp
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
package org.microtitan.tests.montecarlo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cube whose "lower left-hand side" sits on the origin.
 * 
 * @author Robert Philipp
 */
public class Cube {

	private final List< Double > dimensions;
	
	/**
	 * Constructor for an n-dimensial cube, where n is the size of the specified dimensions list
	 * @param dimensions The list of dimensions. For example, a 3-d unit cube would be ( 1, 1, 1 ). 
	 */
	public Cube( final List< Double > dimensions )
	{
		this.dimensions = new ArrayList<>();
		for( double dimension : dimensions )
		{
			this.dimensions.add( Math.abs( dimension ) );
		}
	}
	
	/**
	 * Constructor for an n-dimensial cube, where n is the size of the specified dimensions list
	 * @param dimensions The list of dimensions. For example, a 3-d unit cube would be ( 1, 1, 1 ). 
	 */
	public Cube( final Double...dimensions )
	{
		this( Arrays.asList( dimensions ) );
	}

	/**
	 * @return The number of dimensions. For example, a 3-d cube would return 3
	 */
	public int getDimension()
	{
		return dimensions.size();
	}
	
	/**
	 * Returns the dimension for the specified dimension. Confused? Yeah so am I. Suppose you have a
	 * 3-d cube specified by ( pi, 2pi, 6pi). Then the first dimension would be pi, and the third
	 * dimension, 6pi.
	 * @param dimension The number of the dimension for which to return the dimension.
	 * @return The dimension of the specified dimension
	 */
	public double getDimension( final int dimension )
	{
		return dimensions.get( dimension );
	}
	
	/**
	 * @return a list of the dimensions.
	 */
	public List< Double > getDimensions()
	{
		return new ArrayList< Double >( dimensions );
	}
	
	/**
	 * Returns true if the specified point falls within this cube; false otherwise
	 * @param point The point to test
	 * @return true if the specified point falls within this cube; false otherwise
	 */
	public boolean isInside( final Double...point )
	{
		return isInside( Arrays.asList( point ) );
	}
	
	/**
	 * Returns true if the specified point falls within this cube; false otherwise
	 * @param point The point to test
	 * @return true if the specified point falls within this cube; false otherwise
	 */
	public boolean isInside( final List< Double > point )
	{
		boolean isInside = false;
		if( point.size() == dimensions.size() && !dimensions.isEmpty() )
		{
			isInside = true;
			for( int i = 0; i < dimensions.size(); ++i )
			{
				if( point.get( i ) > dimensions.get( i ) )
				{
					isInside = false;
					break;
				}
			}
		}
		return isInside;
	}
	
	/**
	 * Returns true if the specified cube is entirely within this cube; otherwise false. Really, this is just asking
	 * whether a point is inside the cube, because all cubes have on corner on the origin.
	 * @param cube The cube to test for containment
	 * @return true if the specified cube is entirely within this cube; otherwise false
	 */
	public boolean isContained( final Cube cube )
	{
		return isInside( cube.dimensions );
	}
	
	/**
	 * @return The n-dimensional volume of the cube.
	 */
	public double getVolume()
	{
		double volume = ( dimensions.size() > 0 ? 1 : 0 );
		for( Double dimension : dimensions )
		{
			volume *= dimension;
		}
		return volume;
	}
}
