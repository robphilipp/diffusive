package org.microtitan.diffusive.diffuser.senders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObejctStreamSender {

	
	public static void main( String[] args )
	{
		final TestClassA a = new TestClassA( new TestClassB( "test_class_b" ) );
		
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
	}
}
