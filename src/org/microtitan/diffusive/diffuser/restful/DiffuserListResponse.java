package org.microtitan.diffusive.diffuser.restful;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.microtitan.diffusive.diffuser.restful.xml.UriListAdapter;

@XmlRootElement
public class DiffuserListResponse {

	@XmlElement
	@XmlJavaTypeAdapter( UriListAdapter.class )
	private List< URI > diffusers;
	
	public DiffuserListResponse()
	{
		diffusers = new ArrayList<>();
	}
	
	
}
