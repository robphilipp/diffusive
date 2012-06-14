package org.microtitan.diffusive.diffuser.restful;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class DiffuserCreateResponse extends Response {

	@Override
	public Object getEntity()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultivaluedMap< String, Object > getMetadata()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatus()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public String toString()
	{
		return "this is the response, deal with it!";
	}
}
