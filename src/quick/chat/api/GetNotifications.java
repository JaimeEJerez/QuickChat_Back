package quick.chat.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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
import com.pojo.ChatMessageCore;
import com.tcp.JSONResponse;
import com.tcp.TraceListener;

import quick.chat.history.HistoryManager;
import quick.chat.utils.Util;

/**
 * Servlet implementation class GetChatContent
 */
@WebServlet("/GetNotifications")
public class GetNotifications extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static final HistoryManager 	historyManager 		= Globals.getHistoryManager();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetNotifications() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		TraceListener.println( "GetChatContent.doPost()" );

	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 					gson 		= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 		osw 		= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		Vector<ChatMessageCore> resultVect 	= new Vector<ChatMessageCore>();
		
	    String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
	    	TraceListener.println( "SecurityToken" );
	    	
	    	gson.toJson( JSONResponse.not_success( 1000, "Invalid SecurityToken" ), osw );
	    	
	    	osw.flush();
	    	
	    	return;
	    }
		
		LinkedTreeMap<String, String> paramMap;
		try
		{
			paramMap = Util.getParamMap( request, gson );
		} 
		catch (IOException e1)
		{
			TraceListener.println( "IOException:" + e1.getMessage() );

			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			TraceListener.println( "IOException:" + e1.getMessage() );

			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}
		
		String registryUID 	= paramMap.get("registryUID");

		if ( registryUID == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{					
			historyManager.retriveMessagesFromHistory( null, "NOTIFICSYS", registryUID, new HistoryManager.RetriveEntriesEventHandler() 
			{
				@Override
				public boolean handleEvent(ChatMessageCore message)
				{					
					if ( message != null && message.getContent() != null )
					{
						resultVect.add( message );
					}
					return true;
				}
			} );

			JSONResponse posP = JSONResponse.success( resultVect );
			response.setStatus( HttpServletResponse.SC_OK );
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}

}
