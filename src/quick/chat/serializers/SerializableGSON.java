package quick.chat.serializers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;


public class SerializableGSON implements java.io.Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6601178228400823279L;

	private static final String kHEAD = "SOBJ";
	
	public Object clone()
	{
        Object obj=null;
        try
        {
            obj=super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            System.out.println(" no se puede duplicar");
        }
        return obj;
    }
	
	public static SerializableGSON fromByteArr( byte[] buff ) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream 	bais 	= new ByteArrayInputStream( buff );
		ObjectInputStream 		ois 	= new ObjectInputStream( bais );
		
		SerializableGSON retValue = (SerializableGSON)ois.readObject();
		
		ois.close();

		return retValue;
	}
	
	public static SerializableGSON fromJSON( DataInputStream dis ) throws IOException, ClassNotFoundException
	{
		SerializableGSON retValue = null;
		
		String head = dis.readUTF();

		if ( head.equalsIgnoreCase( kHEAD ))
		{
			String jsonStr = dis.readUTF();
						
			retValue = (SerializableGSON)JsonReader.jsonToJava( jsonStr );
		}
		
		return retValue;
	}
		
	public void toJSON( DataOutputStream dos ) throws IOException
	{
		dos.writeUTF( kHEAD );
		
		String jsonStr = JsonWriter.objectToJson( this );
		
		dos.writeUTF( jsonStr );
	}

	public byte[] toByteArr() throws IOException
	{
		ByteArrayOutputStream 	bos = new ByteArrayOutputStream();
		
		ObjectOutputStream 		oos	= new ObjectOutputStream( bos );
		
		oos.writeObject( this );
		
		oos.flush();
		
		byte[] buff = bos.toByteArray();
		
		oos.close();
		
		return buff;
	}
	
}
