package com.tcp;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.managers.MesageManager;

import quick.chat.stress.ServerCounters;



public class ContextListener implements ServletContextListener 
{	

    public void contextInitialized(ServletContextEvent event) 
    {
		MesageManager.getSingleton();
    	
    	TraceListener.doStart();
    	
    	try
		{
			Thread.sleep( 1000 );
		} catch (InterruptedException e)
		{}
    	
    	WebsocketServer.doStart();
    	TCPReceiveListener.doStart( ServerCounters.getSingleton() );
    	TCPSendListener.doStart( ServerCounters.getSingleton() );
    	
    }

    public void contextDestroyed(ServletContextEvent event) 
    {
    	TraceListener.println( "\r\n setActiveDriver( contextDestroyed )" );

    	WebsocketServer.doStop();
    	TCPReceiveListener.doStop();
    	TCPSendListener.doStop();
    	
    	try
		{
			Thread.sleep( 1000 );
		} catch (InterruptedException e)
		{}

    	TraceListener.doStop();
    }
    
    
}