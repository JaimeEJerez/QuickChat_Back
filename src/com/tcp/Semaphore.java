package com.tcp;

import java.util.Hashtable;


public class Semaphore
{
	private static Semaphore self 		= null;
	
	private String	id					= null;
	private long	connectionTime		= 0;
	private long	notifyLatency		= 0;
	private long 	notifyTime			= 0;
	private	Object 	server				= null;
	
	private  Hashtable<String,Semaphore> 	connectionsTable 	= null;

	public static synchronized Semaphore getSingleton()
	{
		if ( self == null )
		{
			self = new Semaphore();
		}
				
		return self;
	}

	private Semaphore()
	{
		connectionsTable 		= new Hashtable<String,Semaphore>( 1000 );
	}
	
	public Semaphore( String id, Object server )
	{
		id = id.toUpperCase();
		
		this.id 				= id;
		this.server				= server;
		this.connectionTime 	= System.currentTimeMillis();
	}
		
	public Object getServerObject()
	{
		return server;
	}
	
	public void openConection( String id, Object srverObject )
	{
		id = id.toUpperCase();
		
		TraceListener.println( "Semaphore.openConection(" + id + ")" );

		Semaphore s = new Semaphore( id, srverObject );
		
		connectionsTable.put( id, s );
	}
	
	public void closeConection( String id )
	{		
		id = id.toUpperCase();
		
		TraceListener.println( "Semaphore.closeConection(" + id + ")" );

		this.doNotify( id );
		
		Semaphore sem = connectionsTable.get(id);
		
		if ( sem != null )
		{
			Object srv = sem.getServer();
			
			if ( srv != null )
			{
				if ( srv.hashCode() == this.hashCode() )
				{
					connectionsTable.remove( id );
				}
			}
		}
	}
	
	public void wait4notify( String id )
	{		
		id = id.toUpperCase();
		
		Semaphore s = connectionsTable.get(id);
		
		if ( s == null )
		{
			//TraceListener.println( "Semaphore.wait_start_(" + id + ") ---- NULL" );
			
			try
			{
				Thread.sleep( 1000 );
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			return;
		}

		synchronized(s)
		{
			try
			{
				//TraceListener.println( "Semaphore.wait_start_(" + id + ")" );

				s.wait( 7000 );
				
				//String latencyMSG;
				
				if ( s.notifyTime == 0 )
				{
					//latencyMSG = "";
				}
				else
				{
					notifyLatency = System.currentTimeMillis()-s.notifyTime;
					
					//latencyMSG = " latency:" + notifyLatency;
				}
								
				//TraceListener.println( "Semaphore.wait_finish_(" + id + ")" + latencyMSG );
			
				s.notifyTime = 0;
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	
	public void doNotify( String id )
	{
		id = id.toUpperCase();
		//TraceListener.println( "Semaphore.notifyAll(" + id + ")" );

		Semaphore s = connectionsTable.get(id);
 
		if ( s == null )
		{
			//TraceListener.println( "Semaphore.notifyAll(" + id + ") Not connected" );
			
			return;
		}
		
		//TraceListener.println( "Semaphore.notifyAll(" + id + ") Connected" );
		
		synchronized(s)
		{
			s.notifyTime = System.currentTimeMillis();
			
			s.notifyAll();
		}
	}
	
	public Hashtable<String,Semaphore> getTable()
	{
		return connectionsTable;
	}

	public boolean isOnline( String uid )
	{
		uid = uid.toUpperCase();
		
		return connectionsTable.containsKey( uid );
	}
	
	public boolean isOnlineFromOtherServer( String uid, Object server )
	{
		uid = uid.toUpperCase();
		
		Semaphore sem = connectionsTable.get(uid);
		
		if ( sem != null )
		{
			Object srvr = sem.getServer();
			
			if ( srvr != null && srvr.hashCode() != server.hashCode() )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		return false;
	}

	public String getId()
	{
		return id;
	}

	public long getConnectionTime()
	{
		return connectionTime;
	}

	public long getNotifyLatency()
	{
		return notifyLatency;
	}

	public long getNotifyTime()
	{
		return notifyTime;
	}

	public Object getServer()
	{
		return server;
	}
	
}
