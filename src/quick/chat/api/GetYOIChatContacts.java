package quick.chat.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

import quick.chat.utils.Util;

/**
 * Servlet implementation class GetYOIChatContacts
 */
@WebServlet("/GetYOIChatContacts")
public class GetYOIChatContacts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetYOIChatContacts() 
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
		TraceListener.println( "GetYOIChatContacts -- doPost()" );

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
		String sqToquen		= paramMap.get("sqToquen");

		TraceListener.println( "" );
		TraceListener.println( "GetYOIChatContacts -- kindStr   :" + kindStr );
		TraceListener.println( "GetYOIChatContacts -- typeStr   :" + typeStr );
		TraceListener.println( "GetYOIChatContacts -- registryID:" + registryID );
		TraceListener.println( "GetYOIChatContacts -- sqToquen  :" + sqToquen );

		if ( kindStr == null || (kindStr.equals("G") && kindStr.equals("S")) )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null kind" ), osw ); 
		}
		else
		if ( typeStr == null || typeStr.length() != 1 )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null type" ), osw ); 
		}
		else
		if ( sqToquen == null || sqToquen.length() < 7 )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null sqToquen" ), osw ); 
		}
		else
		if ( !isNumeric( registryID ) )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters error bad or null registryID" ), osw ); 
		}
		else
		{			//referentes
			@SuppressWarnings("unused")
			char kind 	= paramMap.get("kind").charAt(0);
			@SuppressWarnings("unused")
			char type 	= paramMap.get("type").charAt(0);

			String 	uType 	= null;
			URL 	url 	= null;
			
			switch ( type )
			{
				case 'R':// Referente 
					url = new URL("https://backend-dev.yoifirst.com/api/users/my_agent/");
					uType = "A";
					break;
				case 'A':// Agente
					url = new URL("https://backend-dev.yoifirst.com/api/users/my_referrers/");
					uType = "R";
					break;
				case 'I':// Invercionista
					
					break;
			}
			
			TraceListener.println( "" );
			TraceListener.println( "GetYOIChatContacts -- url:"  + url.toString() );
			TraceListener.println( "GetYOIChatContacts -- Token:"  + sqToquen );
			
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestProperty("Accept", "application/json");
			http.setRequestProperty("Authorization", "Bearer " + sqToquen );
				
			int responseCode = http.getResponseCode();
			
			if ( responseCode == 200 )
			{
				try
				{
					InputStream is = http.getInputStream();
					
					Vector<LinkedTreeMap<String, Object>> paramMapVect = Util.getParamMapArray( is, gson, false );
				
					for ( LinkedTreeMap<String, Object> pMap : paramMapVect )
					{
						Hashtable<String,String> contact = new Hashtable<String,String>(4);

						int id 				= ((Double)pMap.get("id")).intValue();
						String displayName 	= (String)pMap.get("username");
				        
						TraceListener.println( "" );
						TraceListener.println( "GetYOIChatContacts -- uKind      : S" );
						TraceListener.println( "GetYOIChatContacts -- uType      : "  + uType );
						TraceListener.println( "GetYOIChatContacts -- uID        : "  + id );
						TraceListener.println( "GetYOIChatContacts -- displayName: "  + displayName );
						
				        contact.put( "uKind", 		 "S" );
						contact.put( "uType", 		 uType   );
						contact.put( "uID", 		 String.valueOf(id) );
						contact.put( "displayName",  displayName );
						
						resultVect.add(contact);
					}
					
					TraceListener.println( "" );
					TraceListener.println( "GetYOIChatContacts -- END" );
					
					JSONResponse posP = JSONResponse.success( resultVect );
					
					response.setStatus( HttpServletResponse.SC_OK );
					
				    gson.toJson( posP, osw );
				} 
				catch (Exception e)
				{
					e.printStackTrace();
					gson.toJson( JSONResponse.not_success( 1701, e.getMessage() ), osw );
				}
			}
			else
			{
				gson.toJson( JSONResponse.not_success( 1701, url.toString() + " responseCode=" + responseCode ), osw );
			}
			
			http.disconnect();			
		    
		    osw.flush();
		}
	}

}
