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

import quick.chat.contacts_manager.ContactsManager;
import quick.chat.contacts_manager.ContactsManager.Contact;
import quick.chat.db_io.MySQL;
import quick.chat.utils.Util;

/**
 * Servlet implementation class SendMessage
 */
@WebServlet("/SendTextMessage2group")
public class SendTextMessage2group extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	private static final MesageManager	mesageService		= MesageManager.getSingleton();
	//private static final HistoryManager historyManager 		= Globals.getHistoryManager();

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendTextMessage2group() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
	    String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
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
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}
		
		String senderID 	= paramMap.get("senderID");
		String groupID 		= paramMap.get("groupID");
		String messageT 	= paramMap.get("textMessage");

		if ( senderID == null || groupID == null || messageT == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		if( !groupID.startsWith("G"))
		{
			gson.toJson( JSONResponse.not_success( 0, "The receptor 'groupID' is not a group." ), osw ); 
		}
		else
		{			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				String sender_displayName 	= null;
				String group_displayName 	= null;

				Vector<Contact> senderContacts = ContactsManager.getUsersContacts( senderID, false );
				
				for ( Contact c : senderContacts )
				{
					if ( c.uuID.equalsIgnoreCase( senderID ) )
					{
						sender_displayName = c.displayName;
					}
					if ( c.uuID.equalsIgnoreCase( groupID ) )
					{
						group_displayName = c.displayName;
					}
				}

				if ( senderID.equalsIgnoreCase("SYSTEM0001") )
				{
					sender_displayName = "Mensage automatico del sistema";
				}
				
				if ( group_displayName == null )
				{
					posP = JSONResponse.not_success( 1003, "This user does not belong to the group " + groupID );
				}
				else
				{				
					TextMsg	txtMessage = new TextMsg( 0, messageT );
	
					txtMessage.setChatContentClass( "quick_chat.adapters.chat.TextMessage" );
					
					ChatMessageCore messageCore = new ChatMessageCore( 	txtMessage,
																		MessageType.kGroupUser,
																		sender_displayName,
																		groupID,
																		MessageType.kSingleUser,
																		group_displayName,
																		groupID,
																		System.currentTimeMillis() );

					mesageService.addIncomingMessage( groupID, messageCore, true );
																									
					posP = JSONResponse.success( "Message sent successfully to group " + group_displayName + ".");
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				
				posP = JSONResponse.not_success( 003, "IOException:" + e.getMessage() );

				e.printStackTrace();
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
