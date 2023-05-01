package quick.chat.utils;

import javax.servlet.ServletException;

import quick.chat.db_io.MySQL;

public class Temporals
{
	static MySQL mysql = new MySQL();
	
	public static void set( String uuid, String type, String value ) throws ServletException
	{
		String id = mysql.simpleQuery("SELECT ID FROM TEMP WHERE UUID_USUARIO='"+ uuid + "' AND TIPO='" + type + "'" );
		
		String command;
		
		if ( id == null )
		{
			command = "INSERT INTO TEMP (UUID_USUARIO, TIPO, VALOR) \r\n VALUES( '" + uuid + "','" + type + "','" + value + "' )";
		}
		else
		{
			command = "UPDATE TEMP SET UUID_USUARIO ='" + uuid + "', TIPO='" + type + "', VALOR='" + value + "' WHERE ID=" + id;		
		}
		
		synchronized ( mysql )
		{
			mysql.executeCommand(command);
			
			if (mysql.getLastError() != null)
			{	
				throw new ServletException("SQL Error:" + mysql.getLastError() + " - " + command);
			}
		}
	}
	
	public static String get( String uuid, String type ) throws ServletException
	{
		String result = null;
		
		String query = "SELECT VALOR FROM TEMP WHERE UUID_USUARIO='" + uuid + "' AND TIPO='" + type + "'";
		
		synchronized ( mysql )
		{
			result = mysql.simpleQuery(query);
			
			if (mysql.getLastError() != null)
			{	
				throw new ServletException("SQL Error:" + mysql.getLastError() + " - " + query);
			}
		}
		
		return result;
	}
}
