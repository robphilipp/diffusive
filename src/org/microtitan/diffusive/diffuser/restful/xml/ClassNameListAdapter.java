package org.microtitan.diffusive.diffuser.restful.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ClassNameListAdapter extends XmlAdapter< List< String >, List< Class< ? > > > {
	
	private static final ClassNameAdapter CLASS_NAME_ADAPTER = new ClassNameAdapter();

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public List< Class< ? > > unmarshal( final List< String > classNames ) throws Exception
	{
		final List< Class< ? > > classList = new ArrayList<>();
		for( String className : classNames )
		{
			classList.add( CLASS_NAME_ADAPTER.unmarshal( className ) );
		}
		return classList;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public List< String > marshal( final List< Class< ? > > classes )
	{
		final List< String > nameList = new ArrayList<>();
		for( Class< ? > clazz : classes )
		{
			nameList.add( CLASS_NAME_ADAPTER.marshal( clazz ) );
		}
		return nameList;
	}
}
