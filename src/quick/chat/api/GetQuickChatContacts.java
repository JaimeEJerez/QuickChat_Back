package quick.chat.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.tcp.JSONResponse;
import com.tcp.TraceListener;

import quick.chat.db_io.MySQL;
import quick.chat.utils.Util;

/**
 * Servlet implementation class GetQuickChatContacts
 */
@WebServlet("/GetQuickChatContacts")
public class GetQuickChatContacts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetQuickChatContacts() 
    {
        super();
    }

    public static boolean isNumeric(String strNum) 
    {
        if (strNum == null) 
        {
            return false;
        }
        try 
        {
            Integer.parseInt(strNum);
        } 
        catch (NumberFormatException nfe) 
        {
            return false;
        }
        
        return true;
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		TraceListener.println( "GetQuickChatContacts -- doPost()" );

	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 					gson 		= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 		osw 		= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		Vector<Hashtable<String, String>> resultVect 	= new Vector<Hashtable<String, String>>();
		
		LinkedTreeMap<String, String> paramMap;
		try
		{
			paramMap = Util.getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			TraceListener.printException( e1.getMessage(), e1 );

			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );

			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			TraceListener.printException( e1.getMessage(), e1 );

			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );

			osw.flush();
			return;
		}
		
		if ( paramMap == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Empty JSON parameters." ), osw ); 
		
			osw.flush();
			return;
		}
		
		String kindStr 		= paramMap.get("kind");
		String typeStr 		= paramMap.get("type");
		String registryID 	= paramMap.get("registryID");
		@SuppressWarnings("unused")
		String sqToquen		= paramMap.get("sqToquen");

		TraceListener.println( "GetQuickChatContacts -- BEGIN " + kindStr + " " + typeStr + " " + registryID );
		
		if ( kindStr == null || (kindStr.equals("G") && kindStr.equals("S")) )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null kind" ), osw ); 
		}
		else
		if ( typeStr == null || typeStr.length() != 1 )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null type" ), osw ); 
		}
		if ( !isNumeric( registryID ) )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null registryID" ), osw ); 
		}
		else
		{			
			char kind 	= paramMap.get("kind").charAt(0);
			@SuppressWarnings("unused")
			char type 	= paramMap.get("type").charAt(0);

			MySQL mysql = new MySQL();
	
			try
			{
				if ( kind == 'G' )// Group contacts
				{
					// En este caso retorna los contactos que pertenecen a este grupo
										
					String query = 	"SELECT USERS.TYPE, USERS.ID, USERS.DISPLAY_NAME\r\n"
									+ "FROM CHAT_GROUPS\r\n"
									+ "INNER JOIN USERS\r\n"
									+ "ON CHAT_GROUPS.USER_EMAIL = USERS.EMAIL AND CHAT_GROUPS.GROUP_ID=" + registryID;
					
					String[] sqlResponse = mysql.simpleAQuery( query  );
		
					if ( mysql.getLastError() != null )
					{
						TraceListener.println( "GetQuickChatContacts -- mysql.Error:" + mysql.getLastError() + " " + query );
					}
					
					for ( int i =0; i< sqlResponse.length; i+=3 )
					{
						String uType 		= sqlResponse[i];
						String uID 			= sqlResponse[i+1];
						String displayName 	= sqlResponse[i+2];
						
						{
							Hashtable<String,String> contacts = new Hashtable<String,String>(4);
	
							contacts.put( "uKind", 		 "S" );
							contacts.put( "uType", 		 uType   );
							contacts.put( "uID", 		 uID );
							contacts.put( "displayName", displayName );
							
							resultVect.add(contacts);
						}
					}
				}
				if ( kind == 'S' )// Single contact
				{				
					{
						Hashtable<String,String> contacts = new Hashtable<String,String>(4);
	
						contacts.put( "uKind", 		 "G" );
						contacts.put( "uType", 		 "U" 	);
						contacts.put( "uID", 		 "1" 	);
						contacts.put( "displayName", "All Users" );
						
						resultVect.add(contacts);
					}
					
					// En este caso debe retornar los contactos de este usuario, pero por simplicidad retorna todos los contactos 
					// menos el mismo
					
					String[] sqlResponse = mysql.simpleAQuery( "SELECT TYPE, ID, DISPLAY_NAME FROM USERS;" );
		
					if (sqlResponse.length > 0)
					{
						for ( int i = 0; i < sqlResponse.length; i += 3 )
						{
							String uType 		= sqlResponse[i];
							String uID 			= sqlResponse[i+1];
							String displayName 	= sqlResponse[i+2];
							
							if ( !registryID.equals(uID) )
							{
								Hashtable<String,String> contacts = new Hashtable<String,String>(4);

								contacts.put( "uKind", 		 "S" );
								contacts.put( "uType", 		 uType 	);
								contacts.put( "uID", 		 uID 	);
								contacts.put( "displayName", displayName );
								
								resultVect.add( contacts );
							}
						}
					}
				}
			} 
			finally
			{
				mysql.disconect();
			}
	
			TraceListener.println( "GetQuickChatContacts -- END" );
			
			JSONResponse posP = JSONResponse.success( resultVect );
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		    
		    osw.flush();
		}
	}
}
