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
package org.microtitan.diffusive.diffuser.restful.server.config;

import org.microtitan.diffusive.diffuser.strategy.RandomDiffuserStrategyConfigXml;
import org.microtitan.diffusive.diffuser.strategy.RandomWeightedDiffuserStrategyConfigXml;

/**
 * Defines the available strategies for configuration of the {@link org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer}. These are
 * used by the command-line options parser for generating configuration files. They do NOT represent
 * the only available strategies. You are free to add your own and add them to the configuration files.
 * 
 * @author Robert Philipp
 */
public enum StrategyType {
	
	RANDOM( "random", "random_diffuser_strategy.xml", RandomDiffuserStrategyConfigXml.class ),
	RANDOM_WEIGHTED( "random_weighted", "random_weighted_diffuser_strategy.xml", RandomWeightedDiffuserStrategyConfigXml.class );
	
	private String strategyType;
	private String fileName;
	private Class< ? > clazz;
	private StrategyType( final String strategyType, final String fileName, final Class< ? > clazz )
	{
		this.strategyType = strategyType;
		this.fileName = fileName;
		this.clazz = clazz;
	}
	
	public String getName()
	{
		return strategyType;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public String getClassName()
	{
		return clazz.getName();
	}

	public static StrategyType getStrategyType( final String strategyType )
	{
		for( StrategyType type : values() )
		{
			if( type.getName().equals( strategyType ) )
			{
				return type;
			}
		}
		return null;
	}
}

