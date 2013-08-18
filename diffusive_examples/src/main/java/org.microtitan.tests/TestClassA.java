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
package org.microtitan.tests;

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
