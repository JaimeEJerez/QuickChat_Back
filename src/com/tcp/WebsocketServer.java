package com.tcp;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import com.cedarsoftware.util.io.JsonWriter;
import com.globals.Globals;
import com.managers.MesageManager;
import com.managers.Repository;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;
import com.pojo.chatContent.AudioMsg;
import com.pojo.chatContent.DocumentMsg;
import com.pojo.chatContent.StaticImageMsg;
import com.pojo.chatContent.TextMsg;

import quick.chat.api.UploadTemporalFile;
import quick.chat.contacts_manager.ContactsManager;
import quick.chat.contacts_manager.ContactsManager.Contact;
//import quick.chat.history.HistoryManager;
//import quick.chat.history.HistoryManager;
import quick.chat.stress.ServerCounters;
import quick.chat.utils.Util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Enumeration;
import java.util.UUID;
import java.util.Vector;

import javax.imageio.ImageIO;
//import java.util.logging.Logger;

public class WebsocketServer extends WebSocketServer
{
	//private static final HistoryManager historyManager 		= Globals.getHistoryManager();
	private static final MesageManager	mesageService		= MesageManager.getSingleton();

	private static WebsocketServer 					self 				= null;
	private static Semaphore						semaphore			= Semaphore.getSingleton();

	private ServerCounters 							counters 			= null;

	//private static final Logger LOGGER = Logger.getLogger( WebsocketServer.class.getName() );

    public static boolean isOnline( String uuid ) 
    {
    	return semaphore.isOnline(uuid);
    }
    
    public static int getNservers()
    {
    	return semaphore.getTable().size();
    }
    
	class ServerThread extends Thread
	{
		WebSocket 					conn 				= null;
		String 						registryUID 		= null;
		String						token				= null;
		
    	Vector<String> receptorList = new Vector<String>();
    	        
		public ServerThread( 	String 						registryUID,
								String						token,
								WebSocket 					conn )
		{
			TraceListener.println( "" );
			TraceListener.println( "ServerThread( " + registryUID + " " + token + ")" );
				
			this.registryUID 	= registryUID.toUpperCase();
			this.token			= token;
			this.conn 			= conn;
		}
		
		@Override
		public void run()
		{			
			if ( registryUID == null || registryUID.length() != 10 || ( !registryUID.startsWith("G") && !registryUID.startsWith("S") ) )
			{
				TraceListener.println( "Invalid registryUID:" + registryUID + " in WebsocketServer " + conn.getRemoteSocketAddress().toString() );

				try
				{
					Thread.sleep( 30000 );
				} catch (InterruptedException e)
				{}
				
				conn.close();
				
				return;
			}
			
			if ( semaphore.isOnline(registryUID) )
			{
				try
				{
					for ( int i=0; i<7; i++ )
					{
						TraceListener.println( "WebsocketServer.ServerThread.run() " + registryUID + " THIS_USER_IS_ONLINE" );

						Thread.sleep( 1000 );
						
						if ( !semaphore.isOnline(registryUID) )
						{
							break;
						}
					}
				} catch (InterruptedException e) {}
									
				if ( semaphore.isOnline(registryUID) )
				{
					conn.send( "THIS_USER_IS_ONLINE" );
					
					registryUID = null;
					
					return;
				}
			}
			
			TraceListener.println( "WebsocketServer.ServerThread.run() " + registryUID );
			
			counters.addOutThreads();
			
			semaphore.wait4notify(registryUID);
			
			semaphore.openConection( registryUID, this );
			
			String old_onlineStr = "";
        	
			try
			{
				Vector<Contact>  contacts = ContactsManager.getUsersContacts( registryUID, true );
				
				receptorList.clear();
				
				for ( Contact c : contacts )
				{					
					if ( c.uuID.startsWith("G") )
					{
						receptorList.add( c.uuID );
					}
				}
				
				receptorList.add( registryUID );
							
				while ( conn.isOpen() )
				{
					int messagesCount = 0;
					
					for ( String receptor : receptorList)
					{
						if ( receptor == null )
						{
							continue;
						}
						
						String reseptionKey = receptor.startsWith("G") ? receptor + "_" + registryUID : receptor;
						
						try
						{				    		    	
							ChatMessageCore chatMessage = mesageService.peekMessage( reseptionKey );
							
							if ( chatMessage != null )
							{
								TraceListener.println( "mesageService.peekMessage()" );

								messagesCount++;
								
								if ( conn.isOpen() )
								{															
									try
									{
										String jsonStr = JsonWriter.objectToJson( chatMessage );
										 
										conn.send( "OBJECT:" + jsonStr );
										
										counters.addOutCount();
									}
									finally
									{
										mesageService.removeMessage( reseptionKey );
									}
								}
							}
						} 
						catch (IOException e)
						{
							TraceListener.printException( "IOException:", e );
							
							counters.addOutErrors( e.getMessage() );
														
							conn.close();
						}
					}
					
					if ( messagesCount == 0 )
					{
						if ( conn.isOpen() )
						{
							String new_onlineStr = "";
							
							Enumeration<Contact> contactEnum = contacts.elements();
							
							while ( contactEnum.hasMoreElements() )
							{
								Contact c 		= contactEnum.nextElement();
								String 	state 	=  c.uuID + ":" + isOnline( c.uuID );
								new_onlineStr 	= new_onlineStr.isEmpty() ? state : new_onlineStr + "," + state;
							}
							
							if ( !new_onlineStr.isEmpty() && !new_onlineStr.equals(old_onlineStr) )
							{
								TraceListener.println( "WebsocketServer.conn.send('ONLINE');" );
								
								old_onlineStr = new_onlineStr;
								
								conn.send( "ONLINE=" + new_onlineStr );
							}
							else
							{
								TraceListener.println( "WebsocketServer.conn.send('TIME');" );
								
								conn.send( "TIME" );
							}
							
							semaphore.wait4notify( registryUID );
						}
					}
				}
				
			} 
			catch (Exception e1)
			{
				TraceListener.printException( "Exception:", e1 );
			}

			semaphore.closeConection( registryUID );
			
			counters.removeOutThreads();
			
			TraceListener.println( "WebsocketServer. bye bye " + registryUID );
		}
	}
	

    public WebsocketServer( ServerCounters counters ) 
    {
        super(new InetSocketAddress(Globals.kWebsocketServerPort));
        
        TraceListener.println( "WebsocketServer()" );
        
        this.counters = counters;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) 
    {
    	TraceListener.println("WebsocketServer.Open( " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + " )" + this );
    	
    	counters.addInThreads();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) 
    {
    	TraceListener.println( "WebsocketServer.Close( " + reason + " )" );
    	
    	counters.removeInThreads();
    	
    	conn.close();
    }
    
 
    @Override
    public void onMessage(WebSocket conn, String message) 
    {
    	TraceListener.println("Message from client: " + message);
    	
        JSONObject jsonObject = new JSONObject( message );
        
        String type = jsonObject.getString("type");
        
        if ( type.equalsIgnoreCase("TEST") )
        {
        	conn.send( "Hello Test " + System.currentTimeMillis() );
        }
        
        if ( type.equalsIgnoreCase("CONNECT") )
        {
        	TraceListener.println( "WebsocketServer Conecting" );
        	        	
        	String registryUID 		= jsonObject.getString("registryUID");
        	
			if ( registryUID == null || registryUID.length() != 10 || ( !registryUID.startsWith("G") && !registryUID.startsWith("S") ) )
			{
				TraceListener.println( "Invalid registryUID:" + registryUID + " in WebsocketServer " + conn.getRemoteSocketAddress().toString() );

				try
				{
					Thread.sleep( 30000 );
				} catch (InterruptedException e)
				{}
				
				conn.close();
				
				return;
			}

        	String token = jsonObject.getString("token");
        	
        	ServerThread serverThread = new ServerThread( registryUID, token, conn );
        	
        	serverThread.start();

            conn.send( "Hello" );
                        
            TraceListener.println( "Connected()" );
        }
        
        if ( type.equalsIgnoreCase("AUDIO_MSG") )
        {
        	final String formatHead = "base64,";
        	
        	TraceListener.println( "AUDIO_MSG" );

        	String 		audioData 		= jsonObject.getString("audioData");
        	char 		receiverType	= jsonObject.getString("receiverType").charAt(0);
        	String 		receiverID		= jsonObject.getString("receiverID");
        	String 		senderName		= jsonObject.getString("senderName");
        	String 		senderID		= jsonObject.getString("senderID");
        	char 		senderType		= jsonObject.getString("senderType").charAt(0);
        	String 		receiverName	= jsonObject.getString("receiverName");

        	int indx = audioData.indexOf( formatHead );
        	
        	if ( indx < 0 )
        	{
        		conn.send( "ALERT:Incompatible Audio format.." );
        		
        		TraceListener.println( "ALERT:Incompatible Audio format.." );
        	}
        	else
        	{
	        	audioData = audioData.substring( indx + formatHead.length() );
	        	
	        	//TraceListener.println( "audioType:" + audioType  );
	        	//TraceListener.println( "audioSize:" + audioSize );	        	
	        	
	        	byte[] decodedBytes = null;
	        	
	        	try
	        	{
	        		decodedBytes = Base64.getDecoder().decode(audioData);
	        		
					UUID 	uuid 		= UUID.randomUUID();
					String 	audioName 	= uuid.toString() + ".mp4";
		
					AudioMsg	audioMessage = new AudioMsg( 0, audioName, 1, null );
					
					audioMessage.setChatContentClass( "quick_chat.adapters.chat.AudioMessage" );

		        	try
					{
						Repository.write( senderID, audioName, "AUDIO", decodedBytes );
						
						ChatMessageCore cmc = MesageManager.sendMessage(audioMessage,
																		senderType,
																		senderName,
														   				senderID,
														   				receiverType,
														   				receiverName,
														   				receiverID,
														   				false);
					   	
						String jsonStr = JsonWriter.objectToJson( cmc );

						conn.send( "OBJ_AKN:" + jsonStr );
					} 
		        	catch (IOException e)
					{
		        		conn.send( "ALERT" + ": INTERNAL ERROR - " + e.getMessage() );
		        		
		        		counters.addInErrors( e.getMessage() );
		        		
		        		TraceListener.printException( "IOException:", e );
					}

	        	}
	        	catch ( Exception e )
	        	{
	        		conn.send( "ALERT" + ": INTERNAL ERROR - " + e.getMessage() );
	        		
	        		counters.addInErrors( e.getMessage() );
	        		
	        		TraceListener.printException( "Exception:", e );
	        	}
        	}
        }
        
        if ( type.equalsIgnoreCase("DOCUMENT_MSG") )
        {
        	TraceListener.println( "DOCUMENT_MSG" );
        	
        	String  	document2send 		= jsonObject.getString("file2send").trim();
            		    		
        	char 		receiverType	= jsonObject.getString("receiverType").charAt(0);
        	String 		receiverID		= jsonObject.getString("receiverID");
        	String 		senderName		= jsonObject.getString("senderName");
        	String 		senderID		= jsonObject.getString("senderID");
        	char 		senderType		= jsonObject.getString("senderType").charAt(0);
        	String 		receiverName	= jsonObject.getString("receiverName");
        	
    		File 	srcFile = new File( Globals.tempDirectory + File.separator + senderID + File.separator + document2send );

    		if ( srcFile.exists() )
    		{
    			try
				{					
	    			File dstFile = new File( Repository.calcDir(senderID, "DOCUMENT"), document2send );
	    			
	    			if (!dstFile.exists())
	    			{
	    				Util.createDirectoryTree( dstFile.getAbsolutePath() );	
	    			}

	    			Files.move( srcFile.toPath(), dstFile.toPath() , StandardCopyOption.REPLACE_EXISTING );
    			
	    			String fileSrc = Globals.kServer_API_URL + "DocumentsRepositoryRaw?userID=" + senderID + "&documentID=" + document2send;

	    			DocumentMsg	documentMsg = new DocumentMsg( 0, fileSrc, document2send );
    			
	    			documentMsg.setChatContentClass( "quick_chat.adapters.chat.DocumentMessage" );
    			
    				ChatMessageCore cmc = MesageManager.sendMessage(documentMsg,
			    		   											senderType,
										    		   				senderName,
										    		   				senderID,
										    		   				receiverType,
										    		   				receiverName,
										    		   				receiverID,
										    		   				false);

    				String jsonStr = JsonWriter.objectToJson( cmc );

					conn.send( "OBJ_AKN:" + jsonStr );
				} 
				catch (IOException e)
				{
					counters.addInErrors( e.getMessage() );
					
					TraceListener.printException( "IOException:", e );
				}
    		}
        }

        
        if ( type.equalsIgnoreCase("IMAGE_MSG") )
        {
        	TraceListener.println( "IMAGE_MSG" );
        	
        	String  	image2send 		= jsonObject.getString("file2send").trim();
            		    		
        	char 		receiverType	= jsonObject.getString("receiverType").charAt(0);
        	String 		receiverID		= jsonObject.getString("receiverID");
        	String 		senderName		= jsonObject.getString("senderName");
        	String 		senderID		= jsonObject.getString("senderID");
        	char 		senderType		= jsonObject.getString("senderType").charAt(0);
        	String 		receiverName	= jsonObject.getString("receiverName");
        	
    		File 	srcFile = new File( Globals.tempDirectory + File.separator + senderID + File.separator + image2send );

    		if ( srcFile.exists() )
    		{
    			BufferedImage image;
				try
				{
					image = ImageIO.read(srcFile);
					
	    			int h 	= image.getWidth();
	    			int v 	= image.getHeight();

					double hypo = Math.hypot( h , v );
			        double fact = 64.0 / hypo;
			        
			        BufferedImage 	smallImage 		= image;
			        String 			smallHexImage 	= null;
			        
			        if ( fact < 1.0 )
			        {
				        h = (int)Math.round( h*fact );
				        v = (int)Math.round( v*fact );
				        
				        smallImage = UploadTemporalFile.resizeImage( image, h, v );
			        }
			        
			        ByteArrayOutputStream baos = new ByteArrayOutputStream();
			        
			        ImageIO.write( smallImage, "jpg", baos );
			        
			        baos.flush();
			        
			        byte[] imgeBytes = baos.toByteArray();
			        
			        StringBuilder sb = new StringBuilder( imgeBytes.length * 2 );
			        
			        for ( byte b : imgeBytes )
			        {
			            sb.append(  String.format("%02X", b) );
			        }

			        smallHexImage = sb.toString();
			        
			        baos.close();
			        smallImage.flush();

	    			File dstFile = new File( Repository.calcDir(senderID, "STATIC_IMAGE"), image2send );
	    			
	    			if (!dstFile.exists())
	    			{
	    				Util.createDirectoryTree( dstFile.getAbsolutePath() );	
	    			}
	    			
	    			String fileSrc = Globals.kServer_API_URL + "ImagesRepositoryRaw?userID=" + senderID + "&imageUUID=" + image2send;

	    			Files.move( srcFile.toPath(), dstFile.toPath() , StandardCopyOption.REPLACE_EXISTING );
    			
    				StaticImageMsg	staticImageMsg = new StaticImageMsg( 0, fileSrc, "" , h*2, v*2, smallHexImage  );
    			
    				staticImageMsg.setChatContentClass( "quick_chat.adapters.chat.StaticImageMessage" );
    			
    				ChatMessageCore cmc = MesageManager.sendMessage(staticImageMsg,
			    		   											senderType,
										    		   				senderName,
										    		   				senderID,
										    		   				receiverType,
										    		   				receiverName,
										    		   				receiverID,
										    		   				false);
    		   	
    				String jsonStr = JsonWriter.objectToJson( cmc );

					conn.send( "OBJ_AKN:" + jsonStr );
				} 
				catch (IOException e)
				{
					counters.addInErrors( e.getMessage() );
					
					TraceListener.printException( "IOException:", e );
				}
    		}
        }
        
        if ( type.equalsIgnoreCase("TEXT_MSG") )
        {
        	TraceListener.println( "TEXT_MSG" );
        	
        	String  	text 			= jsonObject.getString("textMessage");
        	        	
        	char 		receiverType	= jsonObject.getString("receiverType").charAt(0);
        	String 		receiverID		= jsonObject.getString("receiverID");
        	String 		senderName		= jsonObject.getString("senderName");
        	String 		senderID		= jsonObject.getString("senderID");
        	char 		senderType		= jsonObject.getString("senderType").charAt(0);
        	String 		receiverName	= jsonObject.getString("receiverName");

			TextMsg	txtMessage = new TextMsg( 0, text );
			
			txtMessage.setChatContentClass( "quick_chat.adapters.chat.TextMessage" );
			
			if ( !ContactsManager.isInMyContacts( senderID, receiverID ) )
			{
				conn.send( "ALERT: This receiverID :" + receiverID + " is not in you contacts list." );
			}
			else
			{
				ChatMessageCore cmc = MesageManager.sendMessage(txtMessage,
					   											senderType,
												   				senderName,
												   				senderID,
												   				receiverType,
												   				receiverName,
												   				receiverID,
												   				false);
				
				if ( receiverType == MessageType.kGroupUser )
				{
					try
					{
						String changedTxt = ContactsManager.sendMessageToTags( 	txtMessage.getText(), 
																				senderName, 
																				senderID, 
																				receiverName, 
																				receiverID, 
																				cmc.getId(), 
																				cmc.getTime() );
						
						txtMessage.setText( changedTxt );
					} 
					catch (IOException e)
					{
						counters.addInErrors( e.getMessage() );
						
						TraceListener.printException( "IOException:", e );
					} 
					catch (Exception e)
					{
						counters.addInErrors( e.getMessage() );
						
						TraceListener.printException( "Exception:", e );
					}
				}
				
				String jsonStr = JsonWriter.objectToJson( cmc );
	
				conn.send( "OBJ_AKN:" + jsonStr );
			}
		}
        
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) 
    {
    	TraceListener.println( "WebsocketServer.ERROR: " + ex.getMessage());
    }

	@Override
	public void onStart()
	{
		TraceListener.println( "WebsocketServer.onStart()" );
	}

	public static void doStart()
	{
		TraceListener.println( "WebsocketServer.doStart()" );
		
		if ( self == null ) 
		{
			(self = new WebsocketServer( ServerCounters.getSingleton() )).start();
			
			self.setTcpNoDelay( true );
		}
		
	}

	public static void doStop()
	{
		TraceListener.println( "WebsocketServer.doStop()" );
		
		if ( self != null ) 
		{
			try
			{
				self.stop();
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
