package com.tcp;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;
import java.util.function.BiConsumer;

import com.managers.MesageManager;
import com.pojo.ChatMessageCore;

import quick.chat.contacts_manager.ContactsManager;
import quick.chat.contacts_manager.ContactsManager.Contact;
import quick.chat.contacts_manager.ContactsManager.ContactHandler;
//import quick.chat.history.HistoryManager;
import quick.chat.stress.ServerCounters;


public class TCPSendServer implements Runnable
{
	boolean					quit					= false;
	Socket 					connectionSocket		= null;
	ServerSocket 			listenerSocket			= null;
	DataInputStream 		dis 					= null;
	DataOutputStream 		dos						= null;
	String					registryUID 			= null;
	String					registryName 			= null;
	long 					lastTime 				= 0;
	long					lastActi				= -1;
	
	//private static final HistoryManager historyManager 		= Globals.getHistoryManager();
	private static final MesageManager	mesageManager		= MesageManager.getSingleton();
	
	private			ServerCounters 		counters 			= null;

	private static 	Semaphore			semaphore			= Semaphore.getSingleton();

    public static void quitAll()
    {
    	Hashtable<String, Semaphore> sendersSrvTable = semaphore.getTable();
    	
    	sendersSrvTable.forEach( new BiConsumer<String,Semaphore>()
		{
			@Override
			public void accept(String key, Semaphore semaphore)
			{
				Object serverObject = semaphore.getServerObject();
				
				if ( serverObject instanceof TCPSendServer )
				{
					((TCPSendServer)serverObject).quit = true;
				}
			}
	
		});
    }

	public TCPSendServer(ServerSocket listenerSocket, Socket connectionSocket, ServerCounters counters )
	{
		this.listenerSocket   	= listenerSocket;
		this.connectionSocket 	= connectionSocket;
		this.counters			= counters;
	}
    
	@Override
	public void run()
	{				
	    Vector<String> 			receptorList			= new Vector<String>();
		
		try
		{ 
			connectionSocket.setSoTimeout( 30000 );
			
			dos = new DataOutputStream( connectionSocket.getOutputStream() );
			
			BufferedInputStream bis =  new BufferedInputStream( connectionSocket.getInputStream() ) ;

			dis = new DataInputStream( bis );

			dos.writeUTF( "WELLCOME TO QuickChat TCP Send Server V1.7" );
			dos.flush();
			
			registryUID 	= dis.readUTF().toUpperCase();
			
			if ( registryUID.length() != 10 || ( !registryUID.startsWith("G") && !registryUID.startsWith("S") )  )
			{
				TraceListener.println( "Invalid registryUID:" + registryUID + " TCPSendServer " + connectionSocket.getInetAddress() );

				Thread.sleep( 30000 );
				
				registryUID = null;
				
				connectionSocket.close();
				
				return;
			}
			
			counters.addOutThreads();
			
			registryName	= dis.readUTF();
			
			if ( !registryUID.startsWith( "SX" ) )
			{
				for ( int i=0; i<7; i++ )
				{
					if ( !semaphore.isOnline(registryUID) )
					{
						break;
					}
					Thread.sleep( 1000 );
				}
				
				/*
				if ( semaphore.isOnlineFromOtherServer(registryUID, this) )
				{
					dos.writeUTF("THIS_USER_IS_ONLINE");
					
					registryUID = null;
					
					TraceListener.println( "THIS_USER_IS_ONLINE:" + registryUID + " -> bye bye" );
					
					return;
				}
				*/
				
				{
					TraceListener.println( "BEGIN_HISTORY" );

					dos.writeUTF("BEGIN_HISTORY");
					dos.flush();
					
					Hashtable<String,Long> userMessageTable = new Hashtable<String,Long>();
					
					String HISTORY_STATUS_BEGIN = dis.readUTF();
					
					if ( HISTORY_STATUS_BEGIN.equalsIgnoreCase("HISTORY_STATUS_BEGIN") )
					{
						TraceListener.println( "HISTORY_STATUS_BEGIN" );
						
						@SuppressWarnings("unused")
						String command;
						
						while ( (command = dis.readUTF()).equalsIgnoreCase("ITEM") )
						{
							String 	userUUID 	= dis.readUTF();
							long 	msgeUUID 	= dis.readLong();
							
							userMessageTable.put(userUUID, msgeUUID);
						}
						
						TraceListener.println( command );
					}
										
					TraceListener.println( "getContactsWithHistory" );
					ContactsManager.getContactsWithHistory( registryUID, registryName, userMessageTable, new ContactHandler()
					{
						@Override
						public boolean retribeHistoryMessage(ChatMessageCore chatMessage)
						{
							try
							{
								//chatMessage.setId(0);
								dos.writeUTF("MSG");
								chatMessage.toJSON( dos );
								dos.flush();
								return true;
							} 
							catch (IOException e)
							{
								TraceListener.printException("retribeHistoryMessage()", e);
								return false;
							}
						}
					});
					
					TraceListener.println( "END_HISTORY" );
					
					dos.writeUTF("END_HISTORY");
					dos.flush();
				}
				
				Vector<Contact> contacts = ContactsManager.getUsersContacts( registryUID, false );
				
				if ( contacts != null )
				{
					for ( Contact c : contacts )
					{
						if ( c.uuID.startsWith( "G" ) )
						{
							receptorList.add(c.uuID);
						}
					}
				}
			}
			
			receptorList.add(registryUID);
				
			semaphore.wait4notify(registryUID);
			
			semaphore.openConection( registryUID, this );
											
			lastTime = System.currentTimeMillis() + 3000;
									
			while ( !quit )
			{
				int messagesCount = 0;
				
				int available = bis.available();

				if ( available >= 5 )
				{
					String command = dis.readUTF();
					
					if ( command.equalsIgnoreCase("QUIT") )
					{
						quit = true;
						break;
					}
				}
				
				for ( String receptorID : receptorList )
				{
					if ( receptorID == null )
					{
						continue;
					}
										
					String reseptionKey = receptorID.startsWith("G") ? receptorID + "_" + registryUID : receptorID;

					if ( registryUID.equalsIgnoreCase( "SM00000002") )
					{
						if ( reseptionKey.equalsIgnoreCase( "GV00000008_SM00000002" ) )
						{
							System.out.println( "GV00000008_SM00000002" );
						}
					}
					
					ChatMessageCore chatMessage = mesageManager.peekMessage( reseptionKey );
							
					if ( chatMessage != null )
					{
						messagesCount++;
						
						lastTime = System.currentTimeMillis();
						
						boolean succsess 	= false;
						String 	akn 		= "";
						String	stage		= "";
						
						try
						{
							stage = "snd";
							dos.writeUTF("MSG");
							chatMessage.toJSON( dos );
							dos.flush();
							stage = "rcv";
							akn = dis.readUTF();
							succsess = akn.equalsIgnoreCase("AKN");
						}
						catch (IOException e)
						{								
							counters.addOutErrors( e.toString()  );
							
							mesageManager.removeMessage( reseptionKey );
							
							TraceListener.printException( stage + " " + e.getMessage(), e);
							
							Thread.sleep(1000);
							quit = true;
							break;
						}
						finally
						{
							if ( succsess )
							{
								mesageManager.removeMessage( reseptionKey );
								
								counters.addOutCount();
								
								//System.out.println( "addOutCount:" + counters.getOutCount() );
							}
							else
							{
								counters.addOutRetries();
							}
						}
					}
				}
				
				if ( messagesCount == 0 && !quit  )
				{
					if ( System.currentTimeMillis() - lastTime >= 7000L )
					{
						dos.writeUTF( "TIME" );
						dos.flush();
						lastTime = System.currentTimeMillis();
						//dis.readUTF();
					}
					
					semaphore.wait4notify( registryUID );
				}
			}
		}
		catch (Exception e)
		{			
			counters.addOutErrors( e.toString()  );
								
			TraceListener.printException( e.getMessage(), e);
						
			quit = true;
		} 
	
		finally
		{			
			if ( registryUID != null )
			{
				counters.removeOutThreads();
				
				semaphore.closeConection( registryUID );
			}

			//TraceListener.println( "TCPSendServer " + registryUID + " -> bye bye" );
			
			if ( connectionSocket != null )
			{
				try
				{
					connectionSocket.close();
				} catch (IOException e)
				{}
			}
			
			try
			{
				Thread.sleep( 1000 );
			} catch (InterruptedException e1)
			{}
		}
			
	}
	
	
}
