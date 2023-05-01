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
import com.managers.MesageManager;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;
import com.pojo.chatContent.TextMsg;
import com.tcp.JSONResponse;
import com.tcp.Semaphore;
import com.tcp.TraceListener;

import quick.chat.contacts_manager.ContactsManager;
import quick.chat.contacts_manager.ContactsManager.Contact;
import quick.chat.db_io.MySQL;
import quick.chat.history.HistoryManager;
import quick.chat.utils.Util;

/**
 * Servlet implementation class SendMessage
 */
@WebServlet("/SendTextMessage")
public class SendTextMessage extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	private static final MesageManager		mesageService		= MesageManager.getSingleton();
	private static final HistoryManager 	historyManager 		= Globals.getHistoryManager();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendTextMessage() 
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
		String messageT 	= paramMap.get("textMessage").trim();

		if ( senderID == null || recipiID == null || messageT == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				String 				registryUID = senderID;
				Vector<Contact>  	contacts	= null;
				try
				{
					contacts = ContactsManager.getUsersContacts( registryUID, true );
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				String sender_displayName 	= null;
				String recipi_displayName 	= null;

				if ( contacts != null )
				{
					for ( Contact c : contacts )
					{
						if ( c.uuID.equalsIgnoreCase( senderID ) )
						{
							sender_displayName = c.displayName;
						}
						if ( c.uuID.equalsIgnoreCase( recipiID ) )
						{
							recipi_displayName = c.displayName;
						}
					}
				}

				if ( sender_displayName == null )
				{
					posP = JSONResponse.not_success( 1003, "There is no user with that ID=" + senderID );
				}
				else
				if ( recipi_displayName == null )
				{
					posP = JSONResponse.not_success( 1003, recipiID + " is not in the contats list of" + senderID );
				}
				else
				{					
					TextMsg	txtMessage = new TextMsg( 0, messageT );

					txtMessage.setChatContentClass( "quick_chat.adapters.chat.TextMessage" );

					ChatMessageCore chatMessage = new ChatMessageCore( 	txtMessage,
																		MessageType.kSingleUser,
																		sender_displayName ,
																		senderID,
																		MessageType.kSingleUser,
																		recipi_displayName,
																		recipiID,
																		System.currentTimeMillis() );

					try
					{
						//TraceListener.println( "mesageService.addIncomingMessage:\r\n" + chatMessage.toString() );

						historyManager.addMessage2History( chatMessage, null );
						
						mesageService.addIncomingMessage( recipiID, chatMessage, true );
						
						posP = JSONResponse.success( "Message sent successfully." );
						
						Semaphore.getSingleton().doNotify(recipiID);
					} 
					catch (IOException e1)
					{
						TraceListener.println( "IOException:" + e1.getMessage() );

						e1.printStackTrace();
						
						posP = JSONResponse.not_success( 003, "IOException:" + e1.getMessage() );
					} 
					catch (Exception e1)
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
