package org.microtitan.diffusive.tests;

import java.io.Serializable;

public class TestClassA implements Serializable {

	private static final long serialVersionUID = -3933571635703044427L;

	private TestClassB reference;
	
	public TestClassA( final TestClassB reference )
	{
		this.reference = reference;
	}
	
//	public TestClassA()
//	{
//		reference = new TestClassB();
//	}
	
	public String toString()
	{
		return "A: " + reference.toString() + "; serialVersionUID: " + serialVersionUID;
	}
}
