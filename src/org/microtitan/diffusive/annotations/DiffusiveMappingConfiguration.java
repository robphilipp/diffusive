package org.microtitan.diffusive.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation the identifies a method as a method that configures an end-point resolver.
 * 
 * @author Robert Philipp
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface DiffusiveMappingConfiguration
{
}
