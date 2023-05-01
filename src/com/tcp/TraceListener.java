package com.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

import com.globals.Globals;



public class TraceListener extends Thread
{
	private static final  	int 			port 			= Globals.kDebugListenerPort;
	public  static 			TraceListener 	tcpListener		= null;
	
	public static 			String			lastError 		= "";
	public static 			boolean			running 		= false;
	public static 			ServerSocket 	socket			= null;
		
	private static			CopyOnWriteArrayList<DebugTracer> 	serversArray 	= new CopyOnWriteArrayList<DebugTracer>();
	public  static 			LinkedBlockingQueue<String> 		queue 			= new LinkedBlockingQueue<String>();

	public static void printJSON( String title, JSONObject json )
	{
		String str = json.toJSONString();
		
		str = str.replace("{", "{\r\n  ").replace("}", "\r\n}").replace("\",\"", "\"\r\n  \"");

		synchronized ( queue )
		{
			queue.add( "\r\n" + title + "\r\n" + str + "\r\n" );
		}
	}
	
	public static void printException( String text, Exception e ) 
	{
		e.printStackTrace();
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		String[] trace = (sw.toString()).split("at ");
		
		for ( String s : trace )
		{
			println( s.trim() );
		}
	}
	
	public static void println( String txt )
	{
		System.out.println( txt );
		
		synchronized ( queue )
		{
			queue.add( txt + "\r" );
			
			if ( queue.size() > 64 )
			{
				queue.remove();
			}
		}
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
					
				( new Thread( new DebugTracer( socket, connectionSocket, serversArray, queue ) ) ).start();
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
			
			for ( DebugTracer s : serversArray )
			{
				s.setQuit();
			}
		}
		
		return result;
	}
	
	public static String doStart()
	{		
		if ( !running )
		{
			tcpListener = new TraceListener();
			 		
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
	
	public static String getLastError()
	{
		return lastError;
	}

	public static void report(PrintWriter w)
	{
		w.println( "\r\nDebugListener" );
		
		w.println( "LastError:" + lastError );
		
		w.println( "Is running:" + running );
		
		w.println( "Listenning in port:" + socket.getLocalPort() );
		
		w.println(  serversArray.size() + " debug server(s) running..." );
		
		DebugTracer.report( w );
	}
}
