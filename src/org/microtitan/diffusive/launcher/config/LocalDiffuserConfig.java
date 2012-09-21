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
package org.microtitan.diffusive.launcher.config;

import org.microtitan.diffusive.annotations.DiffusiveConfiguration;
import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.LocalDiffuser;

public class LocalDiffuserConfig {

	@DiffusiveConfiguration
	public static void configure()
	{
		// load the diffuser repository
		KeyedDiffuserRepository.getInstance().setDiffuser( new LocalDiffuser() );
	}
}
