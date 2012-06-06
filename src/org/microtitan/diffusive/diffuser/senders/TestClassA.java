package org.microtitan.diffusive.diffuser.senders;

import java.io.Serializable;

public class TestClassA implements Serializable {

	private static final long serialVersionUID = -3933571635703044427L;

	private TestClassB reference;
	
	public TestClassA( final TestClassB reference )
	{
		this.reference = reference;
	}
	
	public String toString()
	{
		return "A: " + reference.toString();
	}
}
