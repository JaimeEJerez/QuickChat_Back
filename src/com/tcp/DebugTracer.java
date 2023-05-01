package com.tcp;

//import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
//import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
//import java.util.logging.FileHandler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;

//import com.globals.Globals;

//import quick.chat.utils.Util;



public class DebugTracer implements Runnable
{
	public  LinkedBlockingQueue<String> 		queue 				= null;
	private CopyOnWriteArrayList<DebugTracer> 	serversArray 		= null;
	
	private Socket 								connectionSocket	= null;
	@SuppressWarnings("unused")
	private ServerSocket 						listenerSocket		= null;

	//private static String directPath = Util.createDirectoryTree( Globals.rootDirectory + File.separator + "logs" );
	
	private static String lastError = "";
	
	private boolean quit = false;
		
	public void setQuit()
	{
		quit = true;
	}
	
	public DebugTracer()
	{
	}
	
	
	public DebugTracer(	ServerSocket 						listenerSocket, 
						Socket 								connectionSocket, 
						CopyOnWriteArrayList<DebugTracer> 	serversArray,
						LinkedBlockingQueue<String> 		queue)
	{
		this.listenerSocket   	= listenerSocket;
		this.connectionSocket 	= connectionSocket;
		this.serversArray 		= serversArray;
		this.queue				= queue;
	}

	@Override
	public void run()
	{		
		PrintWriter pr	= null;

		serversArray.add( this );
		
		try
		{
			pr = new PrintWriter( connectionSocket.getOutputStream() );
			
			pr.print("\r\nWellcome to QuickChat DebugTracer V1.09 " + WebsocketServer.getNservers() + "\r\n");			
			
			pr.flush();
			
			while ( !quit )
			{
				String txt = queue.poll();
				
				if ( txt == null )
				{
					try
					{
						Thread.sleep(500);
					} catch (InterruptedException e)
					{}
				}
				else
				{
					pr.println( txt );
					pr.flush();
				}
			}
		} 
		catch (IOException e)
		{
			lastError = "IOException:" + e.getMessage(); 
		} 
		
		finally
		{
			serversArray.remove( this );
			
			pr = null;
			
			try
			{
				connectionSocket.close();
			} catch (IOException e)
			{}
		}
	}

	public static void report(PrintWriter w)
	{
		w.println(  "Last Error:" + lastError );
	}


}
