package quick.chat.contacts_manager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.managers.MesageManager;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;
import com.pojo.chatContent.NotifMsg;
import com.pojo.chatContent.TextMsg;
import com.tcp.Semaphore;
import com.tcp.TraceListener;
import com.tcp.WebsocketServer;

import quick.chat.history.HistoryManager;
import quick.chat.history.HistoryManager.RetriveEntriesEventHandler;
import quick.chat.utils.Util;

public class ContactsManager
{
	private static final Hashtable<String,Vector<Contact>> contactsTable = new Hashtable<String,Vector<Contact>>();
	
	private static final String 			pattern 			= "dd-M-yyyy hh:mm";
	private static final SimpleDateFormat 	simpleDateFormat 	= new SimpleDateFormat(pattern);

	private static final Semaphore			semaphore			= Semaphore.getSingleton();
	private static final MesageManager		mesageService		= MesageManager.getSingleton();
	private static final HistoryManager 	historyManager 		= Globals.getHistoryManager();
	
	public static class Contact
	{
		public Contact(String uuID, String displayName)
		{
			this.uuID 			= uuID;
			this.displayName 	= displayName;
		}

		public String uuID;
		public String displayName;
	}

	static public Vector<String> stractTagsFromText(String text)
	{
		Vector<String> tags = new Vector<String>();

		int indx0 = 0;

		for (;;)
		{
			int indx1 = text.indexOf("@", indx0);

			if (indx1 > -1)
			{
				int indx2 = text.indexOf(" ", indx1);

				if (indx2 == -1)
				{
					indx2 = text.length();
				}

				if (indx2 > indx1)
				{
					tags.add(text.substring(indx1 + 1, indx2).trim());
				} else
				{
					indx2 = text.length() - 1;
				}

				indx0 = indx2;
			} else
			{
				break;
			}
		}

		return tags;
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

	static public Vector<Contact> registryUsersContacts( String registryUID, String sqToquen ) throws Exception
	{
		String urlStr =  Globals.kGetQCContactsURLprod != null ? Globals.kGetQCContactsURLprod :  Globals.kGetQCContactsURLtest ;
		
		TraceListener.println( "getUsersContacts(" + registryUID + "," + sqToquen + ") > " + urlStr );
		
		URL url = new URL( urlStr );
		
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		
		// KTXXXXXXXX k=Kind [G=Group,S=Single], T=Type, XXXXXXXX=ID
		
		String rKind 			= registryUID.substring(0, 1);
		String rType 			= registryUID.substring(1, 2);
		int    rID   	 		= Integer.parseInt( registryUID.substring(2), 16 );  
		
		String jsonInputString 	= "{\"kind\":" + rKind + ",\"type\": \"" + rType + "\", \"registryID\": \"" + rID + "\", \"sqToquen\": \"" + sqToquen + "\"}"; 
		
		OutputStream 	os 		= con.getOutputStream();
	    byte[] 			input 	= jsonInputString.getBytes("utf-8");
	    os.write(input, 0, input.length);			
		
		Gson	gson	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		
		LinkedTreeMap<String, Object> paramMap = Util.getParamMap( con.getInputStream(), gson, false );
		
		Boolean success = (Boolean)paramMap.get("success");
		
		Vector<Contact> contacts = new Vector<Contact>();
		
		//TraceListener.println( "getUsersContacts-success=" + success );
		
		if ( success )
		{
			@SuppressWarnings("unchecked")
			ArrayList<LinkedTreeMap<String,String>> payload = (ArrayList<LinkedTreeMap<String,String>>)paramMap.get("payload");
						
			for ( LinkedTreeMap<String,String> contact : payload )
			{				
				String uKind		= contact.get("uKind");
				String uType		= contact.get("uType");
				String uID			= contact.get("uID");
				String displayName 	= contact.get("displayName");
				
				if ( uKind == null || uType.length() != 1 )
				{
					throw new Exception( "Bad or null uType");
				}
				
				if ( uType == null || uType.length() != 1 )
				{
					throw new Exception( "Bad or null uType");
				}
				
				if ( !isNumeric( uID ) )
				{
					throw new Exception( "Bad or null uID");
				}
				
				if ( displayName == null || displayName.isEmpty() )
				{
					throw new Exception( "Bad or null displayName");
				}

				String contactCode = uKind + uType + String.format("%08X", Integer.valueOf(uID));
				
				TraceListener.println( "getUsersContacts-contactCode=" + contactCode + "name" + displayName );
				
				Contact c = new Contact( contactCode, displayName );
				
				contacts.add( c );
			}
		}
		else
		{
			@SuppressWarnings("unchecked")
			LinkedTreeMap<String,String> errorMap = (LinkedTreeMap<String,String>)paramMap.get("error");
			
			String message = errorMap.get("message");
			
			throw new Exception( message );
		}
		
		contactsTable.put( registryUID, contacts );
		
		return contacts;
	}

	static public Vector<Contact> getUsersContacts( String registryUID, boolean reloadCache ) throws Exception
	{
		if ( reloadCache )
		{
			return registryUsersContacts( registryUID, "602d544c-5219-42dc-8e46-883de0de7613" );
		}
		else
		{
			TraceListener.println( "getUsersContacts(" + registryUID + ", From contactsTable )" );

			Vector<Contact> contactVect = contactsTable.get( registryUID );
			
			if ( contactVect == null || contactVect.isEmpty() )
			{
				contactVect = registryUsersContacts( registryUID, "602d544c-5219-42dc-8e46-883de0de7613" );
			}
			
			return contactVect;
		}
	}

	public static String sendMessageToTags(	String text, 
											String senderName, 
											String senderID,
											String groupName,
											String groupID,
											long   msgID,
											long   time ) throws Exception
	{
    	String dateTime = simpleDateFormat.format( time);

		Vector<String> tags = ContactsManager.stractTagsFromText(text);

		if (tags.size() > 0)
		{
			Vector<Contact> contacts = ContactsManager.getUsersContacts( senderID, false );

			Hashtable<String, Contact> contactsHT = new Hashtable<String, Contact>();
			
			for ( Contact c : contacts )
			{
				contactsHT.put( c.displayName.toUpperCase(), c );
			}
			
			for (String tag : tags)
			{
				Contact contact = contactsHT.get(tag.toUpperCase());

				if (contact != null)
				{
					text = text.replace("@" + tag, "<b>@" + tag + "</b>");
					
					NotifMsg notifMsg = new NotifMsg( 0, "Usted ha sido mencionado por " + senderName + " en el grupo '" + groupName + "' a las " + dateTime, groupName, groupID, String.valueOf(msgID) );
					
					ChatMessageCore chatMessage = new ChatMessageCore( notifMsg, MessageType.kSingleUser, "NOTIFICSYS", "NOTIFICSYS", MessageType.kSingleUser, contact.displayName, contact.uuID, System.currentTimeMillis());

					//TraceListener.println("mesageService.addIncomingMessage:\r\n" + chatMessage.toString());

					mesageService.addIncomingMessage( contact.uuID, chatMessage, false );

					semaphore.doNotify( contact.uuID );

					//historyManager.addMessage2History(chatMessage);
				}
			}
		}

		return text;
	}

	public static Vector<Map<String, String>> getEnrichedContacts( String registryUID, String sqToquen ) throws Exception
	{
		final  	Vector<Map<String, String>> resultVect 	= new  Vector<Map<String, String>>();
	
    	Vector<Contact> contacts = registryUsersContacts( registryUID, sqToquen );
						
		for  ( Contact c : contacts )
		{				
			if ( c.uuID.equalsIgnoreCase(registryUID) )
			{
				continue;
			}
			
			ChatMessageCore	chatMessage = historyManager.retriveLastMessagesFromHistory( registryUID, c.uuID );
			
			String 	time 		= String.valueOf( chatMessage == null ? "0" : chatMessage.getTime() );
			String  elapsed		= Util.calcElapsedTime( chatMessage == null ? 0 : chatMessage.getTime() );
			String 	mesage 		= chatMessage == null ? "" : chatMessage.getMsgTxt();
			boolean isOnline 	= WebsocketServer.isOnline( c.uuID );
			boolean isActive	= false;
		
			Map<String, String> 	resultMap 	= new HashMap<String, String>();
			
			resultMap.put("type", "disccusion");
			resultMap.put("userID", c.uuID);
			resultMap.put("userName", c.displayName);
			resultMap.put("time", time);
			resultMap.put("message", mesage);
			resultMap.put("isOnline", isOnline?"true":"false");
			resultMap.put("isActive", isActive?"true":"false");
			resultMap.put("elapsed", elapsed );
			
			resultVect.add(resultMap);
		}
		
		
		return resultVect;
	}
	
	public static abstract class ContactHandler 
	{
		public abstract boolean retribeHistoryMessage( ChatMessageCore	chatMessage );
	}
	
	public static void getContactsWithHistory( 	String registryUID, 
												String registryName, 
												Hashtable<String,Long> userMessageTable, 
												ContactHandler contactHandler ) throws Exception
	{
		TraceListener.println( "getContactsWithHistory(" + registryUID + ")" );

    	Vector<Contact> contacts = registryUsersContacts( registryUID, null );
						
    	if ( contacts != null )
    	{
			for  ( Contact c : contacts )
			{				
				if ( c.uuID.equalsIgnoreCase(registryUID))
				{
					continue;
				}

				Long startMessageID = null;
				
				if ( userMessageTable.containsKey( c.uuID ) )
				{
					long aux = userMessageTable.get( c.uuID );
					
					startMessageID = Long.valueOf( aux );
				}
								
				historyManager.retriveMessagesFromHistory( startMessageID, registryUID, c.uuID, new RetriveEntriesEventHandler()
				{
					@Override
					public boolean handleEvent(ChatMessageCore message)
					{
						return contactHandler.retribeHistoryMessage( message );
					}
				});
				
				if ( startMessageID == null )
				{
					char senderMessateType = c.uuID.startsWith("G") ? MessageType.kGroupUser : MessageType.kSingleUser;
					
					TextMsg	txtMessage = null;
					
					if ( senderMessateType == MessageType.kGroupUser )
					{
						txtMessage = new TextMsg( 0, "Bienvenido al grupo " + c.displayName );
					}
					else
					{
						txtMessage = new TextMsg( 0, "Hola, soy " + c.displayName );
					}
					
					txtMessage.setChatContentClass( "quick_chat.adapters.chat.TextMessage" );

					ChatMessageCore chatMessage = new ChatMessageCore( 	txtMessage,
																		senderMessateType,
																		c.displayName,
																		c.uuID,
																		MessageType.kSingleUser,
																		registryName,
																		registryUID,
																		System.currentTimeMillis() );
					
					historyManager.addMessage2History(chatMessage, null);
										
					contactHandler.retribeHistoryMessage(chatMessage);
				}
			}
    	}
	}

	public static boolean isInMyContacts( String registryUID, String contactID )
	{
		Vector<Contact> contacts = contactsTable.get( registryUID );
		
		for ( Contact c : contacts )
		{
			if ( c.uuID.equalsIgnoreCase(contactID) )
			{
				return true;
			}
		}
		
		return false;
	}
}
