package com.managers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.gaborcselle.persistent.PersistentQueue;
import com.globals.Globals;
import com.pojo.ChatContent;
import com.pojo.ChatMessageCore;
import com.tcp.Semaphore;
import com.tcp.TraceListener;

import quick.chat.contacts_manager.ContactsManager;
import quick.chat.contacts_manager.ContactsManager.Contact;
import quick.chat.history.HistoryManager;
import quick.chat.utils.Util;

public class MesageManager extends Thread 
{
	final static SimpleDateFormat 					sdf 				= new SimpleDateFormat( "EEE, dd/MM/yy hh:mm:ss aa" );
	static 			MesageManager 					self 				= null;
	private static Semaphore						semaphore			= Semaphore.getSingleton();
	private static MesageManager					mesageService		= MesageManager.getSingleton();
	private static final HistoryManager 			historyManager 		= Globals.getHistoryManager();

	private	static String 										queueDirectory  	= null;
	
	private static ConcurrentHashMap<String, PersistentQueue<String>> 	messagesQueueMap 	= null;
	
		
	public static synchronized MesageManager getSingleton()
	{
		return self==null ? new MesageManager(): self;
	}
	
	private MesageManager()
	{
		self = this;
	}
	
	private void init() throws IOException
	{
		if ( messagesQueueMap == null )
		{
			queueDirectory 		= Util.createDirectoryTree( Globals.rootDirectory + File.separator + "PersistentQueue" );
			
			//Util.createDirectoryTree( Globals.rootDirectory + File.separator + "TextMesageManager" );
			
			messagesQueueMap	= new ConcurrentHashMap<String, PersistentQueue<String>>(10000);
		}
	}
	
	public PersistentQueue<String> getMessageQueue( String userID ) throws IOException
	{		
		init();
		
		if ( userID == null || userID.isEmpty() )
		{
			System.out.println( "userID == null" );
		}
		
		PersistentQueue<String> messagesQue = (PersistentQueue<String>) messagesQueueMap.get( userID );
		
		if (messagesQue == null) 
		{
			String filePath = queueDirectory + File.separatorChar + userID ;

			messagesQue = new PersistentQueue<String>( filePath );
			
			messagesQueueMap.put( userID, messagesQue );
			
			//TraceListener.println( "new messagesQueueMap(" + userID + ")" );
		}		
		
		return messagesQue;
	}

	public void removeMessage( String userID ) throws IOException 
	{		
		init();
		
		PersistentQueue<String> messagesQueue = getMessageQueue( userID );
		
		if ( !messagesQueue.isEmpty())
		{
			messagesQueue.remove();
		}
		
		//TraceListener.println( "departedMessage(" + userID + ")" );
	}

	
	public void addIncomingMessage( String receiverID, ChatMessageCore chatMessage, boolean send2self ) throws Exception 
	{
		//TraceListener.println( "addIncomingMessage(" + receiverID + ")" );
		
		init();
		
		if ( receiverID != null )
		{
			receiverID = receiverID.toUpperCase();
			
			if ( receiverID.startsWith( "G" ) ) 
			{
				//TraceListener.println( "addIncomingMessage-GR" );
				
				String senderID = chatMessage.getSenderID();
				
				Vector<Contact> contacts = ContactsManager.getUsersContacts( receiverID, false );

				if (contacts != null)
				{
					for ( Contact c : contacts )
					{
						String userID 	= c.uuID;
						
						if ( userID.startsWith("G") )
						{
							continue;
						}
							
						if ( !send2self && senderID.equalsIgnoreCase( userID ) )
						{
							continue;
						}
						
						if ( !semaphore.isOnline( userID ) )
						{
							continue;
						}
						
						String reseptionKey = receiverID + "_" + userID;

						PersistentQueue<String> messagesQueue = getMessageQueue( reseptionKey );

						if ( messagesQueue != null )
						{
							String jsonStr = JsonWriter.objectToJson( chatMessage);

							if ( jsonStr != null )
							{
								TraceListener.println( "messagesQueue.add(" + reseptionKey + ")" );
								
								messagesQueue.add( jsonStr );
	
								semaphore.doNotify( userID );
							}
							else
							{
								TraceListener.println( "Very rare error 0001" );
							}
						}
					}
				}
			}
			else
			{
				if ( receiverID.startsWith( "SX" ) || semaphore.isOnline( receiverID ) )
				{
					PersistentQueue<String> messagesQueue = getMessageQueue( receiverID );
					
					String jsonStr = JsonWriter.objectToJson( chatMessage);
					
					messagesQueue.add( jsonStr );
					
					semaphore.doNotify( receiverID );
				}
				else
				{
					System.out.println( receiverID + " is not ONLINE" );
				}
			};
		}
	}

	public ChatMessageCore peekMessage( String userID ) throws IOException 
	{
		userID.toUpperCase();
		
		init();
		
		ChatMessageCore  chatMessage = null;
		
		PersistentQueue<String> messagesQueue = getMessageQueue( userID );
		
		if ( !messagesQueue.isEmpty())
		{
			String jsonStr = messagesQueue.peek();
			
			if ( jsonStr == null )
			{
				TraceListener.println( "peekMessage null ->" + userID );
			}
			else
			{
				if ( jsonStr.equalsIgnoreCase("null") ) 
				{
					messagesQueue.remove();
				}
				else
				{
					chatMessage = (ChatMessageCore)JsonReader.jsonToJava( jsonStr );
				}
			}
		}
				
		return chatMessage;
	}

	public ConcurrentHashMap<String, PersistentQueue<String>> getMessagesQueueMap() 
	{
		return messagesQueueMap;
	}

	public void report(PrintWriter w)
	{
		w.append( "\r\nTextMesageManager\r\n" );
		
		w.append( "Messages Queue Map Size=" + messagesQueueMap.size() + ", " + MesageManager.sdf.format( new Date() ) + "\r\n" );
		
		
		/*Enumeration<String> keys = messagesQueueMap.keys();
		
		while ( keys.hasMoreElements() )
		{
			String 									receiverID 				= keys.nextElement();
			PersistentQueue<ChatMessageCore> 	messagesCollection 	= messagesQueueMap.get(receiverID);
			
			 Iterator<ChatMessageCore> messagesIterator = messagesCollection.
			
			w.append( "\r\n***************************************\r\nreceiverID:" + receiverID + "\r\n" );
			
			while ( messagesIterator.hasNext() )
			{
				ChatMessageCore message = messagesIterator.next();
				
				w.append( message.toString() + "\r\n" );
			}

		}*/
	}

	public static ChatMessageCore sendMessage( ChatContent	message,
												char 		senderType,
												String 		senderName,
												String 		senderID,
												char 		receiverType,
												String 		receiverName,
												String 		receiverID,
												boolean 	send2self )
	{
	
		ChatMessageCore chatMessage = new ChatMessageCore(message, senderType, senderName, senderID, receiverType, receiverName, receiverID, System.currentTimeMillis());

		return sendMessage( chatMessage, send2self );
	}
	
	
	public static ChatMessageCore sendMessage( ChatMessageCore chatMessage, boolean send2self )
	{
		//TraceListener.println( "MesageManager.sendMessage()" );
		//counters.addInCount();

		try
		{
			historyManager.addMessage2History( chatMessage, null );

			mesageService.addIncomingMessage(chatMessage.getReceiverID(), chatMessage, send2self);
		} 
		catch (IOException e)
		{
			e.printStackTrace();

			TraceListener.println("IOException:" + e.getMessage());

			e.printStackTrace();
			
			//counters.addInErrors( e.getMessage() );
		} 
		catch (Exception e)
		{
			e.printStackTrace();

			TraceListener.println("IOException:" + e.getMessage());

			//counters.addInErrors( e.getMessage() );
			
			e.printStackTrace();
		} 

		return chatMessage;
	}



}