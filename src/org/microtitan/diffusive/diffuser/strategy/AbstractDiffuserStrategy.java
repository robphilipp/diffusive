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
package org.microtitan.diffusive.diffuser.strategy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.microtitan.diffusive.Constants;


/**
 * Abstract class that deals with the {@link List} of end-point {@link URI}. Implementing classes
 * need to implement the {@link #getEndpoints()} method from the {@link DiffuserStrategy} interface.
 * 
 * @author Robert Philipp
 */
public abstract class AbstractDiffuserStrategy implements DiffuserStrategy {

	private List< URI > endpoints;
	private List< Double > weights;
	private Double weightSum = Double.NaN;
	
	/**
	 * Constructs a {@link DiffuserStrategy} that holds the specified list of end-points
	 * with all the weights set to 1.0
	 * @param endpoints The list of end-points to which to diffuse methods.
	 */
	protected AbstractDiffuserStrategy( final List< URI > endpoints )
	{
		this.endpoints = endpoints;
		
		// create the list of weights and set them all to 1.0
		weights = new ArrayList<>();
		for( int i = 0; i < endpoints.size(); ++i )
		{
			weights.add( 1.0 );
		}
	}
	
	/**
	 * Constructs a {@link DiffuserStrategy} that holds the specified list of end-points
	 * with the specified weights
	 * @param endpoints The map of end-points (values) and their associated weights (keys)
	 * to which to diffuse methods.
	 */
	protected AbstractDiffuserStrategy( final Map< URI, Double > endpoints )
	{
		this.endpoints = new ArrayList<>( endpoints.size() );
		this.weights = new ArrayList<>( endpoints.size() );
		for( Map.Entry< URI, Double > entry : endpoints.entrySet() )
		{
			this.endpoints.add( entry.getKey() );
			this.weights.add( entry.getValue() );
		}
	}
	
	/**
	 * Copy constructor
	 * @param strategy The {@link AbstractDiffuserStrategy} to copy
	 */
	protected AbstractDiffuserStrategy( final AbstractDiffuserStrategy strategy )
	{
		this.endpoints = new ArrayList<>( strategy.endpoints );
		this.weights = new ArrayList<>( strategy.weights );
		this.weightSum = Double.NaN;
	}

	
	/**
	 * @return The number of end-points helds by this strategy
	 */
	public final int getNumEndpoints()
	{
		return endpoints.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy#getEndpointList()
	 */
	@Override
	public final List< URI > getEndpointList()
	{
		return endpoints;
	}

	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy#setEndpointList(java.util.List)
	 */
	@Override
	public final void setEndpointList( final List< URI > endpoints )
	{
		this.endpoints.clear();
		this.weights.clear();
		for( URI uri : endpoints )
		{
			addEndpoint( uri );
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy#appendEndpointList(java.util.List)
	 */
	@Override
	public final void appendEndpoints( final List< URI > endpoints )
	{
		for( URI uri : endpoints )
		{
			addEndpoint( uri );
		}
	}
	
	/**
	 * Returns the end-point with the specified index
	 * @param index The index for which to return the end-point
	 * @return the end-point with the specified index
	 */
	public final URI getEndpoint( final int index )
	{
		return endpoints.get( index );
	}
	
	/**
	 * Adds an end-point to the list of end-points, with a weight of 1.0
	 * @param endpoint The {@link URI} of the end-point to add
	 * @return true if the end-point was added successfully; false otherwise
	 */
	public final boolean addEndpoint( final URI endpoint )
	{
		return addEndpoint( endpoint, 1.0 );
	}
	
	/**
	 * Adds an end-point to the list of end-points, with the specified weight
	 * @param endpoint The {@link URI} of the end-point to add
	 * @param weight The weight of the end-point
	 * @return true if the end-point was added successfully; false otherwise
	 */
	public final boolean addEndpoint( final URI endpoint, final double weight )
	{
		final boolean isAdded = endpoints.add( endpoint );
		if( isAdded )
		{
			weights.add( weight );
			
			// invalidate the sum of the weights
			weightSum = Double.NaN;
		}
		
		return isAdded;
	}
	
	/**
	 * Returns the weight of the end-point with the specified index
	 * @param index The index of the end-point for which to return the weight
	 * @return the weight of the end-point with the specified index
	 */
	public final double getWeight( final int index )
	{
		return weights.get( index );
	}
	
	/**
	 * Returns the weight of the specified end-point
	 * @param endpoint The end-point for which to return the weight
	 * @return the weight of the specified end-point
	 */
	public final double getWeight( final URI endpoint )
	{
		final int index = endpoints.indexOf( endpoint );
		return weights.get( index );
	}
	
	/**
	 * @return the sum of all the weights
	 */
	public final double getWeightSum()
	{
		if( Double.isNaN( weightSum ) )
		{
			double sum = 0.0;
			for( double weight : weights )
			{
				sum += weight;
			}
			weightSum = sum;
		}
		return weightSum;
	}
	
	/**
	 * Removes the specified end-point from the list of end-points used by this strategy
	 * @param endpoint The end-point to remove
	 * @return true if the end-point was removed; false otherwise
	 */
	public final boolean removeEndpoint( final URI endpoint )
	{
		// find the index of the end-point so that we can remove the associated weight
		final int index = endpoints.indexOf( endpoint );
		
		// remove the end-point, and if successful, remove the weight
		final boolean isRemoved = endpoints.remove( endpoint );
		if( isRemoved )
		{
			weights.remove( index );
			
			// invalidate the weight sum
			weightSum = Double.NaN;
		}
		return isRemoved;
	}
	
	/**
	 * Removes the end-point at the specified index
	 * @param index The index of the end-point to remove
	 * @return true if the end-point was removed; false otherwise
	 */
	public final URI removeEndpoint( final int index )
	{
		final URI removedUri = endpoints.remove( index );
		weights.remove( index );
		
		// invalidate the weight sum
		weightSum = Double.NaN;

		return removedUri;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.microtitan.diffusive.diffuser.strategy.DiffuserStrategy#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return ( endpoints == null || endpoints.isEmpty() );
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "Client Endpoints: " + Constants.NEW_LINE );
		final double weightSum = getWeightSum();
		for( int i = 0; i < endpoints.size(); ++i )
		{
			final double weight = weights.get( i );
			buffer.append( "  " + endpoints.get( i ).toString() + ": " );
			buffer.append( String.format( "%6.4f", weight ) );
			buffer.append( " (" + String.format( "%6.4f", weight / weightSum ) );
			buffer.append( ")" );
			buffer.append( Constants.NEW_LINE );
		}
		buffer.append( "Sum of Weights: " + weightSum + Constants.NEW_LINE );
		return buffer.toString();
	}
}
