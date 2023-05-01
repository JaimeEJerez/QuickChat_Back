package quick.chat.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
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

import quick.chat.contacts_manager.ContactsManager;
import quick.chat.utils.Util;

/**
 * Servlet implementation class GetContacts
 */
@WebServlet("/GetEnrichedContacts")
public class GetEnrichedContacts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetEnrichedContacts() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		TraceListener.println( "GetEnrichedContacts.doPost()" );

	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
		/*
	    String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
	    	TraceListener.println( "SecurityToken" );
	    	
	    	gson.toJson( JSONResponse.not_success( 1000, "Invalid SecurityToken" ), osw );
	    	
	    	osw.flush();
	    	
	    	return;
	    }
		 */
		
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

			gson.toJson( JSONResponse.not_success( 1702, e1.getMessage() ), osw );

			osw.flush();
			return;
		}
		
		String registryUID 	= paramMap.get("registryUID");
		String sqToquen 	= paramMap.get("sqToquen");

		if ( registryUID == null || registryUID.length() != 10 || ( !registryUID.startsWith("G") && !registryUID.startsWith("S") ) )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{								  
			Vector<Map<String, String>> discussionsMapVect;
			
			try
			{
				discussionsMapVect = ContactsManager.getEnrichedContacts( registryUID, sqToquen );
				
				JSONResponse posP = JSONResponse.success( discussionsMapVect );

				response.setStatus( HttpServletResponse.SC_OK );
				
			    gson.toJson( posP, osw );

			} 
			catch (Exception e)
			{
				String message = e.getMessage();
				
				TraceListener.printException( message, e );

				gson.toJson( JSONResponse.not_success( 1703, message ), osw );
			}
		}
	    
	    osw.flush();		


	}

}

