package com.tcp;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.function.BiConsumer;

import com.managers.MesageManager;
import com.managers.Repository;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;
import com.pojo.chatContent.TextMsg;

import quick.chat.contacts_manager.ContactsManager;
import quick.chat.stress.ServerCounters;


public class TCPReceiveServer implements Runnable
{
	private boolean 				quit 					= false;
	private Socket 					connectionSocket		= null;
	private DataInputStream 		dis 					= null;
	private DataOutputStream 		dos						= null;
	private String					registryID 				= null;
	private String					registryMail 			= null;
	private ServerCounters 			counters 				= null;
	
	private static final Hashtable<String,TCPReceiveServer> 	receiversSrvTable 	= new Hashtable<String,TCPReceiveServer>();	
	
    long lastTime = 0;

    public static void quitAll()
    {
    	receiversSrvTable.forEach( new BiConsumer<String,TCPReceiveServer>()
		{
			@Override
			public void accept(String arg0, TCPReceiveServer arg1)
			{
				arg1.quit = true;
			}
		});
    }
    
	public static class ServiceAgent
	{
		String registryName = null;
		String name			= null;
		
		public ServiceAgent( String registryName, String name )
		{
			this.registryName = registryName;
			this.name = name;
		}

		public String getRegistryName()
		{
			return registryName;
		}
		
		public void setRegistryName(String registryName)
		{
			this.registryName = registryName;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}
	
	public TCPReceiveServer(ServerSocket listenerSocket, Socket connectionSocket, ServerCounters counters )
	{
		this.connectionSocket 	= connectionSocket;
		this.counters 			= counters;
	}
        
	@Override
	public void run()
	{
		//TraceListener.println( "TCPReceiveServer -> WELLCOME" );
		
		counters.addInThreads();
		
		try
		{
			while ( !quit )
			{
				try
				{ 
					connectionSocket.setSoTimeout( 30000 );
		
					dos = new DataOutputStream( connectionSocket.getOutputStream() );
					dis = new DataInputStream( new BufferedInputStream( connectionSocket.getInputStream() ) );
		
					dos.writeUTF( "WELLCOME TO QuickChat TCP Receive Server V1.7" );
					dos.flush();
					
					registryID 			= dis.readUTF().toUpperCase();
					
					if ( registryID.length() != 10 || ( !registryID.startsWith("G") && !registryID.startsWith("S") )  )
					{
						TraceListener.println( "Invalid registryUID:" + registryID + " TCPReceiveServer " + connectionSocket.getInetAddress() );

						Thread.sleep( 30000 );
						
						registryID = null;
						
						connectionSocket.close();
						
						return;
					}

					registryMail 		= dis.readUTF();
									
					receiversSrvTable.put( registryID, this );
													
					while ( !quit )
					{				
						String 	command = dis.readUTF();
													
						if ( command.equalsIgnoreCase("BINARY"))
						{
							String name 	= dis.readUTF();
							String type 	= dis.readUTF();
							int    length 	= dis.readInt();
							
							byte[] buff = new byte[length];
							
							dis.readFully( buff );
							
							Repository.write( registryID, name, type, buff );
						}
						
						if ( command.equalsIgnoreCase("QUIT") ) 
						{
							quit = true;
						}
						
						if ( command.equalsIgnoreCase("TIME") ) 
						{					
							dos.writeUTF("AKN");
							
							dos.flush();
						}
														
						if ( command.equalsIgnoreCase("MSG") ) 
						{
							ChatMessageCore chatMessage 	= (ChatMessageCore)ChatMessageCore.fromJSON( dis );
							String 			receiverID 		= chatMessage.getReceiverID();
							char 			receiverType 	= chatMessage.getReceiverType();
							String 			senderID 		= chatMessage.getSenderID();
							String 			senderName 		= chatMessage.getSenderName();
							String 			receiverName 	= chatMessage.getReceiverName();
																			
							ChatMessageCore cmc = MesageManager.sendMessage( chatMessage, false );
							
							if ( receiverType == MessageType.kGroupUser		&& 
								 chatMessage.getContent() instanceof TextMsg )
							{
								TextMsg textMsg = (TextMsg)chatMessage.getContent();
								
							    String changedTxt = ContactsManager.sendMessageToTags( 	textMsg.getText(), 
							    														senderName, 
							    														senderID, 
							    														receiverName, 
							    														receiverID, 
							    														cmc.getId(), 
							    														cmc.getTime()  );
							    
							    textMsg.setText(changedTxt);
							}

							counters.addInCount();
							
							dos.writeUTF( "AKN:" + cmc.getId() );
							dos.flush();
						}
					}
				}
				catch (IOException e)
				{
					counters.addInErrors( e.toString() );
										
					TraceListener.printException( registryID, e);
										
					quit = true;
				} 
				catch (ClassNotFoundException e)
				{
					counters.addInErrors( e.toString()  );
										
					TraceListener.printException( registryID, e);
													
					quit = true;
				}
			
				finally
				{
					receiversSrvTable.remove( registryID );
					
					if ( connectionSocket != null && !connectionSocket.isClosed() )
					{
						connectionSocket.close();
					}
				}
				
				try
				{
					Thread.sleep( 1000 );
				} 
				catch (InterruptedException e)
				{}
			}
		}
		catch (Exception e)
		{
			TraceListener.printException( registryID, e);
						
			counters.addInErrors( e.toString() );
		}
		
		finally
		{
			counters.removeInThreads();
		}
		
		try
		{
			Thread.sleep( 1000 );
		} 
		catch (InterruptedException e)
		{}
	}

	//String rcvrName = mySql.simpleQuery( "SELECT APODO FROM USUARIOS WHERE UUID=\"" + rcvrID + "\"" );
	
	//ChatMessageCore msg = (ChatMessageCore)chatMessage.clone();
	//msg.setSenderType( MessageType.kSingleUser );
	//msg.setSenderID( receiverID );
	//msg.setSenderName( receiverName );
	//msg.setReceiverID( rcvrID );
	//msg.setReceiverName( rcvrName );
	//msg.setReceiverType( MessageType.kSingleUser );
	

	public Socket getConnectionSocket()
	{
		return connectionSocket;
	}

	public void setConnectionSocket(Socket connectionSocket)
	{
		this.connectionSocket = connectionSocket;
	}

	public String getRegistryIDName()
	{
		return registryID;
	}

	public void setRegistryName(String registryName)
	{
		this.registryID = registryName;
	}

	public static Hashtable<String, TCPReceiveServer> getReceivesTable()
	{
		return receiversSrvTable;
	}
	
	public static TCPReceiveServer getServerByIDCode( String idCode )
	{
		return receiversSrvTable.get( idCode );
	}

	public long getLastTime()
	{
		return lastTime;
	}

	public void setLastTime(long lastTime)
	{
		this.lastTime = lastTime;
	}


	public String getRegistryMail()
	{
		return registryMail;
	}
	

}
