package org.microtitan.diffusive.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.microtitan.diffusive.diffuser.restful.resources.RestfulDiffuserManagerResource;
import org.microtitan.diffusive.diffuser.restful.server.RestfulDiffuserServer;

/**
 * Tag annotation that signifies that a method is intended to provide configuration for
 * diffusive servers. For example, it could provide the end-points that the {@link RestfulDiffuserServer}
 * places into a repository of strategies used by the {@link RestfulDiffuserManagerResource} when
 * creating new diffusers.
 * 
 * @author Robert Philipp
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface DiffusiveServerConfiguration
{
}
