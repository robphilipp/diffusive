package org.microtitan.diffusive.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD } )
public @interface Diffusive {
	
	public static class Null { } 
}
