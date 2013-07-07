package org.microtitan.diffusive.containers;

/**
 * @author Robert Philipp
 *
 * Defines the interface for a copyable object. A copy of the object <code>x</code> must meets the following criteria
 * <ol>
 * 	<li>The expressions <code>x.getCopy() != x</code> evaluates as <code>true</code></li>
 * 	<li>The expressions <code>x.getCopy().equals( x )</code> evaluates as <code>true</code></li>
 * 	<li>The expressions <code>x.getCopy().getClass() == x.getClass()</code> evaluates as <code>true</code></li>
 * </ol>
 */
public interface Copyable< T > {

    /**
     * Creates and returns a copy of the object <code>x</code> that meets the following criteria
     * <ol>
     * 	<li>The expressions <code>x.getCopy() != x</code> evaluates as <code>true</code></li>
     * 	<li>The expressions <code>x.getCopy().equals( x )</code> evaluates as <code>true</code></li>
     * 	<li>The expressions <code>x.getCopy().getClass() == x.getClass()</code> evaluates as <code>true</code></li>
     * </ol>
     * @return a copy of the object that meets the above criteria
     */
    T getCopy();

}
