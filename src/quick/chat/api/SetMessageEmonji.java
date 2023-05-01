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

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.gaborcselle.persistent.PersistentQueue;
import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.managers.MesageManager;
import com.pojo.ChatMessageCore;
import com.tcp.JSONResponse;
import com.tcp.Semaphore;
import com.tcp.TraceListener;

import quick.chat.contacts_manager.ContactsManager;
import quick.chat.contacts_manager.ContactsManager.Contact;
import quick.chat.db_io.MySQL;
import quick.chat.history.HistoryManager;
import quick.chat.utils.Util;

/**
 * Servlet implementation class DeleteMessage
 */
@WebServlet("/SetMessageEmonji")
public class SetMessageEmonji extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static final HistoryManager 	historyManager 		= Globals.getHistoryManager();
	private static final MesageManager 		mesageManager		= MesageManager.getSingleton();
	private static final Semaphore			semaphore			= Semaphore.getSingleton();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetMessageEmonji() 
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

		String senderID 	= paramMap.get("senderID");
		String recipiID 	= paramMap.get("recipiID");
		String messageID 	= paramMap.get("messageID");
		String whoReacts 	= paramMap.get("whoReacts");
		Object emonjiTxt 	= paramMap.get("emonji");
		
		if ( whoReacts == null || senderID == null || recipiID == null || messageID == null || emonjiTxt == null )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{			
			long 	mess_id = Long.valueOf( messageID );
			int 	emon_ix = ((Double)emonjiTxt ).intValue();
			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				Vector<Contact> senderContacts = ContactsManager.getUsersContacts( senderID, false );
				
				if ( senderContacts == null || senderContacts.size() == 0 )
				{
					posP = JSONResponse.not_success( 1003, "There is no user with that ID=" + recipiID );
				}
				else
				{					
					try
					{
						TraceListener.println( "mesageService.replaceMessagesInFromHistory:()" );
										
						final ChatMessageCore[] 	chatMessageArr = new ChatMessageCore[1];
						final String[] 				jsonMessageArr = new String[1];
						
						String query = "SELECT ID, REACTION FROM REACTIONS WHERE WHO_REACTS=\"" + whoReacts + "\" AND MESG_OWNER=\"" + senderID + "\" AND MESSAGE_ID=\"" + mess_id + "\"";
						
						final String[] 	responseArr 		= mySQL.simpleAQuery(query);
						final int		REACTION_ID			= responseArr.length == 0 ? -1 : Integer.valueOf( responseArr[0] );
						final int 		previous_reactionN	= responseArr.length == 0 ? -1 : Integer.valueOf( responseArr[1] );						
						
						historyManager.replaceMessagesFromHistory( senderID, recipiID, mess_id, new HistoryManager.ReplaceEntryEvent()
						{
							@Override
							public String action(String entry)
							{
								ChatMessageCore chatMessage = (ChatMessageCore)JsonReader.jsonToJava( entry );
																
								if ( previous_reactionN >= 0 )
								{
									chatMessage.removeReaction(previous_reactionN);
									
									if ( previous_reactionN != emon_ix )
									{
										chatMessage.addReaction( emon_ix );
									}
								}
								else
								{
									chatMessage.addReaction( emon_ix );
								}
								
								chatMessageArr[0] = chatMessage;
								
								String jsonStr = JsonWriter.objectToJson( chatMessage );

								jsonMessageArr[0] = jsonStr;

								return jsonStr;
							}
						});
						
						if ( chatMessageArr[0] == null )
						{
							posP = JSONResponse.not_success( 003, "Very rare error 002" );
						}
						else
						{
							if ( REACTION_ID >= 0 )
							{
								if ( previous_reactionN != emon_ix )
								{
									String command = "UPDATE REACTIONS SET REACTION=" + emon_ix + " WHERE ID=" + REACTION_ID;
									
									mySQL.executeCommand(command);
								}
								else
								{
									String command = "DELETE FROM REACTIONS WHERE ID=" + REACTION_ID;
									
									mySQL.executeCommand(command);
								}
							}
							else
							{
								String command = "INSERT INTO REACTIONS ( MESG_OWNER, WHO_REACTS, MESSAGE_ID, REACTION ) "
												+ "VALUES ("
												+ "\"" + senderID + "\","
												+ "\"" + whoReacts + "\","
												+ "\"" + messageID + "\","
												+ emon_ix + " )";
								
								mySQL.executeCommand(command);
							}
							
							if ( recipiID.startsWith("G") )
							{
								ChatMessageCore chatMessage = chatMessageArr[0];
												
								//List the group participants
					        	Vector<Contact> 	contacts = ContactsManager.getUsersContacts( recipiID, false );
	
								if (contacts != null)
								{
									for ( Contact c : contacts )
									{
										String userID 	= c.uuID;
										
										if ( !semaphore.isOnline(userID) )
										{
											continue;
										}
	
										if ( !whoReacts.equalsIgnoreCase(userID) )
										{				
											String reseptionKey = recipiID + "_" + userID;
	
											PersistentQueue<String> messagesQueue = mesageManager.getMessageQueue( reseptionKey );
	
											String jsonStr = JsonWriter.objectToJson( chatMessage);
	
											messagesQueue.add( jsonStr );
	
											semaphore.doNotify( userID );
										}
									}
								}
							}
							else
							{
								PersistentQueue<String> messagesQueue = mesageManager.getMessageQueue( senderID );
								
								String jsonStr = JsonWriter.objectToJson( chatMessageArr[0] );
								
								messagesQueue.add( jsonStr );
								
								semaphore.doNotify( senderID );
							};
	
							posP = JSONResponse.success( jsonMessageArr[0] );
						}
					} 
					catch (IOException e1)
					{
						TraceListener.println( "IOException:" + e1.getMessage() );

						e1.printStackTrace();
						
						posP = JSONResponse.not_success( 003, "IOException:" + e1.getMessage() );
					}
					
				}
				
			} catch (Exception e)
			{
				TraceListener.println( "IOException:" + e.getMessage() );

				e.printStackTrace();
				
				posP = JSONResponse.not_success( 003, "IOException:" + e.getMessage() );
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
