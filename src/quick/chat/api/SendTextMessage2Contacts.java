package quick.chat.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.managers.MesageManager;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;
import com.pojo.chatContent.TextMsg;
import com.tcp.JSONResponse;
import com.tcp.Semaphore;
import com.tcp.TraceListener;

import quick.chat.db_io.MySQL;
import quick.chat.history.HistoryManager;
import quick.chat.utils.Util;

/**
 * Servlet implementation class SendMessage
 */
@WebServlet("/SendTextMessage2Contacts")
public class SendTextMessage2Contacts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	private static final MesageManager		mesageService		= MesageManager.getSingleton();
	private static final HistoryManager 	historyManager 		= Globals.getHistoryManager();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendTextMessage2Contacts() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		TraceListener.println( "SendTextMessage.doPost()" );

	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
	    String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
	    	TraceListener.println( "SecurityToken" );
	    	
	    	gson.toJson( JSONResponse.not_success( 1000, "Invalid SecurityToken" ), osw );
	    	
	    	osw.flush();
	    	
	    	return;
	    }

	    LinkedTreeMap<String,Object> paramMap;
		try
		{
			paramMap = Util.getParamMap( request.getInputStream(), gson, false );
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
		
		String messageTxt 								= (String)paramMap.get("textMessage");
		String senderID 								= (String)paramMap.get("senderID");
		String senderName 								= (String)paramMap.get("senderName");
		@SuppressWarnings("unchecked")
		ArrayList<LinkedTreeMap<String,String>> contacts	= (ArrayList<LinkedTreeMap<String,String>>)paramMap.get("contacts");

		if ( messageTxt == null || senderID == null || senderName == null || contacts == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{			
			MySQL mySQL = new MySQL();
			
			int 			successCount 	= 0;
			int 			errorsCount 	= 0;
			String 			lastError		= null;
			
			try
			{
				
				for ( LinkedTreeMap<String,String> contact : contacts )
				{					
					String recipi_displayName = contact.get( "userName" );
					String recipiID			  = contact.get( "userID" );

					TextMsg	txtMessage = new TextMsg( 0, messageTxt );

					txtMessage.setChatContentClass( "quick_chat.adapters.chat.TextMessage" );

					ChatMessageCore chatMessage = new ChatMessageCore( 	txtMessage,
																		MessageType.kSingleUser,
																		senderName ,
																		senderID,
																		MessageType.kSingleUser,
																		recipi_displayName,
																		recipiID,
																		System.currentTimeMillis() );

					try
					{
						TraceListener.println( "mesageService.addIncomingMessage:\r\n" + chatMessage.toString() );

						historyManager.addMessage2History( chatMessage, null );
						
						mesageService.addIncomingMessage( recipiID, chatMessage, true );
												
						Semaphore.getSingleton().doNotify(recipiID);
						
						successCount++;
					} 
					catch (IOException e1)
					{
						errorsCount++;
						
						TraceListener.printException( "IOException:",e1 );
						
						lastError = e1.getMessage();
					} 
					catch (Exception e1)
					{
						errorsCount++;
						
						TraceListener.printException( "IOException:",e1 );
						
						lastError = e1.getMessage();
					}
				}
			}
			finally
			{
				mySQL.disconect();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
			Hashtable<String,String> result = new Hashtable<String,String>();
			
			result.put( "successCount", String.valueOf( successCount ) );
			result.put( "errorsCount", String.valueOf( errorsCount ) );
			
			if ( lastError != null )
			{
				result.put( "lastError", lastError );
			}
			
		    gson.toJson( JSONResponse.success(result), osw );
		}
	    
	    osw.flush();		
	}

}
