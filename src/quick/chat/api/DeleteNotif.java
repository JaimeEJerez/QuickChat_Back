package quick.chat.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.pojo.ChatMessageCore;
import com.tcp.JSONResponse;
import com.tcp.Semaphore;
import com.tcp.TraceListener;

import quick.chat.db_io.MySQL;
import quick.chat.history.HistoryManager;
import quick.chat.utils.Util;

/**
 * Servlet implementation class DeleteMessage
 */
@WebServlet("/DeleteNotif")
public class DeleteNotif extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static final HistoryManager 	historyManager 		= Globals.getHistoryManager();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteNotif() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		TraceListener.println( "SetMessageEmonji.doPost()" );

	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
	    /*String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
	    	TraceListener.println( "SecurityToken" );
	    	
	    	gson.toJson( JSONResponse.not_success( 1000, "Invalid SecurityToken" ), osw );
	    	
	    	osw.flush();
	    	
	    	return;
	    }*/

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

		String userID 	= paramMap.get("userID");
		String notifID 	= paramMap.get("notifID");
		
		if ( userID == null || notifID == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{			
			userID 	= userID.trim();
			notifID = notifID.trim();

			long mID = Long.valueOf( notifID );
			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP = null;
			
			try
			{
				TraceListener.println( "mesageService.DeleteNotif()" );
								
				historyManager.replaceMessagesFromHistory( null, "NOTIFICSYS_" + userID, mID, new HistoryManager.ReplaceEntryEvent()
				{
					@Override
					public String action(String entry)
					{
						ChatMessageCore chatMessage = (ChatMessageCore)JsonReader.jsonToJava( entry );
														
						chatMessage.setContent(null);
														
						String jsonStr = JsonWriter.objectToJson( chatMessage );

						return jsonStr;
					}
				});
																
				posP = JSONResponse.success( "OK" );
			} 
			catch (IOException e1)
			{
				TraceListener.println( "IOException:" + e1.getMessage() );

				e1.printStackTrace();
				
				posP = JSONResponse.not_success( 003, "IOException:" + e1.getMessage() );
			}
			finally
			{
				mySQL.disconect();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost__(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		TraceListener.println( "DeleteMessage.doPost()" );

	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
	    /*String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
	    	TraceListener.println( "SecurityToken" );
	    	
	    	gson.toJson( JSONResponse.not_success( 1000, "Invalid SecurityToken" ), osw );
	    	
	    	osw.flush();
	    	
	    	return;
	    }*/

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

		String senderID 	= paramMap.get("senderID").trim();
		String recipiID 	= paramMap.get("recipiID").trim();
		String messageID 	= paramMap.get("messageID").trim();

		if ( senderID == null || recipiID == null || messageID == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{			
			long mID = Long.valueOf( messageID );
			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				String query1 = "SELECT DISPLAY_NAME FROM USERS WHERE ID=\"" + Long.parseLong(senderID,16) + "\"";
				String query2 = "SELECT DISPLAY_NAME FROM USERS WHERE ID=\"" + Long.parseLong(recipiID,16) + "\"";

				String sender_displayName 	= mySQL.simpleQuery(query1);
				String recipi_displayName 	= mySQL.simpleQuery(query2);

				if ( sender_displayName == null )
				{
					posP = JSONResponse.not_success( 1003, "There is no user with that ID=" + senderID );
				}
				else
				if ( recipi_displayName == null )
				{
					posP = JSONResponse.not_success( 1003, "There is no user with that ID=" + recipiID );
				}
				else
				{					
					try
					{
						TraceListener.println( "mesageService.replaceMessagesInFromHistory:()" );
										
						final ChatMessageCore[] 	chatMessageArr = new ChatMessageCore[1];
						final String[] 			jsonMessageArr = new String[1];

						historyManager.replaceMessagesFromHistory( senderID, recipiID, mID, new HistoryManager.ReplaceEntryEvent()
						{
							@Override
							public String action(String entry)
							{
								ChatMessageCore chatMessage = (ChatMessageCore)JsonReader.jsonToJava( entry );
																
								chatMessage.setContent(null);
								
								chatMessageArr[0] = chatMessage;
								
								String jsonStr = JsonWriter.objectToJson( chatMessage );

								jsonMessageArr[0] = jsonStr;
								
								return jsonStr;
							}
						});

						//mesageService.addIncomingMessage( recipiID, chatMessageArr[0] );
						
						posP = JSONResponse.success( jsonMessageArr[0] );
						
						Semaphore.getSingleton().doNotify(recipiID);
					} 
					catch (IOException e1)
					{
						TraceListener.println( "IOException:" + e1.getMessage() );

						e1.printStackTrace();
						
						posP = JSONResponse.not_success( 003, "IOException:" + e1.getMessage() );
					}
				}
				
			}
			finally
			{
				mySQL.disconect();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	
	}

}
