package com.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.globals.Globals;

import quick.chat.stress.ServerCounters;



public class TCPReceiveListener extends Thread
{
	private static final  	int 				port 		= Globals.kTCPReceiveListenerPort;
	public  static 			TCPReceiveListener 	tcpListener	= null;
	
	public static String		lastError = null;
	public static boolean		running = false;
	public static ServerSocket 	socket;
	
	private ServerCounters counters;
	
	
	TCPReceiveListener( ServerCounters counters )  
	{
		this.counters = counters;
	}
	
	public void run()
	{
		try
		{
			socket = new ServerSocket( port );
				
			running = true;
			
			while(true)
			{
				Socket connectionSocket = socket.accept();
					
				Thread server = new Thread( new TCPReceiveServer( socket, connectionSocket, counters ) );
								
				server.start();
			}
		}
		catch (IOException e)
		{			
			lastError = "TCPListener IOException:" + e.getMessage();
		}
		
		finally
		{
			running = false;
		}
	}

	public static String doStop()
	{
		String result = "INVALID";
		
		if ( tcpListener == null )
		{
			return "tcpListener == null";
		}
		
		try
		{			
			tcpListener.close();
		} 
		catch (UnknownHostException e)
		{
			result = "TCPListener UnknownHostException " + e.getMessage();
		} 
		catch ( SocketException e )
		{
			result = "TCPListener SocketException " + e.getMessage();
		}
		catch (IOException e)
		{
			result = "TCPListener IOException " + e.getMessage();
		}
				
		finally
		{
			running = false;
			TCPReceiveServer.quitAll();
		}
		
		return result;
	}
	
	public static String doStart( ServerCounters counters )
	{		
		if ( !running )
		{
			tcpListener = new TCPReceiveListener( counters );
			 
			tcpListener.setDaemon( true );
			
			tcpListener.start();
			
			for ( int i=0; i<15 && !running; i++ )
			{
				if ( lastError != null )
				{
					return lastError;
				}
				
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{}
			}
		}
		
		return running ? "The server is executing." : "Warnning: the server is NOT executing.";
	}

	public void close() throws IOException
	{
		if ( socket != null )
		{
			socket.close();
			socket = null;
		}
		lastError = null;
	}


}
