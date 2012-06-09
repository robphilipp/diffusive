package org.microtitan.diffusive.diffuser.senders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.freezedry.persistence.XmlPersistence;

public class ObjectStreamSender {

	
	
	public static void main( String[] args )
	{
		DOMConfigurator.configure( "log4j.xml" );
		Logger.getRootLogger().setLevel( Level.DEBUG );
		
		final TestClassB b = new TestClassB();
		b.setMessage(  "test_class_bee" );
		final TestClassA a = new TestClassA( b );
		
		try( ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( "test_a" ) ); 
			 ObjectInputStream in = new ObjectInputStream( new FileInputStream( "test_a" ) ) )
		{
			out.writeObject( a );
			out.close();
			
			Object readA = in.readObject();
			in.close();
			
			System.out.println( readA.toString() );
		}
		catch( IOException | ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final XmlPersistence xmlPersistence = new XmlPersistence();
		try
		{
			xmlPersistence.write( a, new FileWriter( "test_a.xml" ) );
			
			TestClassA fdA = xmlPersistence.read( TestClassA.class, new FileReader( "test_a.xml" ) );
			
			System.out.println( fdA.toString() );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
