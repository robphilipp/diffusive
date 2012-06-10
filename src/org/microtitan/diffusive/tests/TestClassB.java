package org.microtitan.diffusive.tests;

import java.io.Serializable;

public class TestClassB implements Serializable {

	private static final long serialVersionUID = -1668240037058987563L;

	private String message;
	
//	public TestClassB( final String message )
//	{
//		this.message = message;
//	}
	
//	public TestClassB()
//	{
//		message = "";
//	}
	
	public void setMessage( final String message )
	{
		this.message = message;
	}
	
	public String toString()
	{
		return "B: message=" + message;
	}
}
