package org.microtitan.diffusive.launcher.config;

import org.microtitan.diffusive.diffuser.KeyedDiffuserRepository;
import org.microtitan.diffusive.diffuser.LocalDiffuser;

public class LocalDiffuserRepositoryConfig {

	public static void configure()
	{
		// load the diffuser repository
		KeyedDiffuserRepository.getInstance().setDiffuser( new LocalDiffuser() );
	}
}
