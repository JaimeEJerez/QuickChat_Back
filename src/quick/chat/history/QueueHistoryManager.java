package quick.chat.history;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.globals.Globals;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;

import quick.chat.utils.Util;



public class QueueHistoryManager extends HistoryManager
{	
	private	 String 											queueDirectory  	= null;
	private ConcurrentHashMap<String, HistoryQueue> 			historyQueueMap 	= null;

	public static synchronized HistoryManager getSingleton()
	{
		return self==null ? (self = new QueueHistoryManager()) : self;
	}

	public QueueHistoryManager()
	{
		self = this;
	}

	protected String calcQueueID( String senderID, String receivID )
	{
		String queueID = null;
					
		if ( senderID.startsWith("NOTIFICSYS") )
		{
			queueID = "NOTIFICSYS_" + receivID;
		}
		else
		if ( senderID.compareTo( receivID ) > 0 )
		{
			queueID = senderID + "_" + receivID;
		}
		else
		{
			queueID = receivID + "_" + senderID;
		}
		
		return queueID;
	}

	private void init() throws IOException
	{
		if ( queueDirectory == null )
		{
			queueDirectory 	= Util.createDirectoryTree( Globals.rootDirectory + File.separator + "HistoryQueue" );
					
			historyQueueMap	= new ConcurrentHashMap<String, HistoryQueue>(128);
		}
	}
	
	private HistoryQueue getHistoryQueue( String queueID ) throws IOException
	{		
		init();
		
		HistoryQueue historyQue = (HistoryQueue) historyQueueMap.get( queueID );
		
		if (historyQue == null) 
		{
			String filePath = queueDirectory + File.separatorChar + queueID ;

			historyQue = new HistoryQueue( filePath );
			
			historyQueueMap.put( queueID, historyQue );
		}
		
		return historyQue;
	}
	
	@Override
	public void addMessage2History( ChatMessageCore chatMessage, BeforeToWrite beforeToWrite  ) throws IOException 
	{
		init();
		
		String queueID = null;
		
		if ( chatMessage.getReceiverType() == MessageType.kGroupUser )
		{
			queueID = chatMessage.getReceiverID();
		}
		else
		{
			String senderID = chatMessage.getSenderID();
			String receivID = chatMessage.getReceiverID();
			
			queueID = calcQueueID( senderID, receivID );
		}
		
		if ( queueID != null )
		{
			HistoryQueue historyQueue = getHistoryQueue( queueID );
			
			historyQueue.add( chatMessage, beforeToWrite );
		}
	}
	
	public synchronized ChatMessageCore replaceMessagesFromHistory( String senderID, String receivID, long messageID, ReplaceEntryEvent ree ) throws IOException
	{
		init();
		
		HistoryQueue historyQueue = null;
		
		if ( receivID.startsWith("NOTIFICSYS") )
		{
			historyQueue = getHistoryQueue( receivID );
		}
		else		
		if ( receivID.startsWith("G") )
		{
			historyQueue = getHistoryQueue( receivID );
		}
		else
		{
			String queueID = calcQueueID( senderID, receivID );
			
			historyQueue = getHistoryQueue( queueID );
		}
		
		if ( historyQueue != null )
		{
			historyQueue.replaceEntry( messageID, ree );
		}

		return null;
	}
	
	@Override
	public synchronized ChatMessageCore retriveLastMessagesFromHistory( String senderID, String receivID ) throws IOException
	{
		init();
		
		if ( receivID.startsWith("G") )
		{
			HistoryQueue historyQueue = getHistoryQueue( receivID );
			
			if ( historyQueue != null )
			{
				return historyQueue.getLastElement();
			}
		}
		else
		{
			String queueID = calcQueueID( senderID, receivID );
			
			HistoryQueue historyQueue = getHistoryQueue( queueID );
			
			if ( historyQueue != null )
			{
				return historyQueue.getLastElement();
			}
		}
		
		return null;
	}

	@Override
	public void retriveMessagesFromHistory( Long 	firstUUID, 
											String 	senderID, 
											String 	receivID, 
											RetriveEntriesEventHandler eventHandler ) throws IOException

	{
		init();
		
		if ( receivID.startsWith("G") )
		{
			retriveGroupEntries( firstUUID, receivID, eventHandler );
		}
		else
		{
			retriveSigleEntries( firstUUID, senderID, receivID, eventHandler );
		}
	}

	private synchronized void retriveGroupEntries( 	final Long		firstUUID, 
													final String 	groupID, 
													final RetriveEntriesEventHandler eventHandler ) throws IOException
	{		
		init();
		
		HistoryQueue historyQueue = getHistoryQueue( groupID );
		
		if ( historyQueue != null )
		{
			historyQueue.retriveEntries( firstUUID, eventHandler);
		}
	}

	private synchronized void retriveSigleEntries( 	final Long		firstUUID, 
													final String 	senderID, 
													final String 	receivID, 
													final RetriveEntriesEventHandler eventHandler ) throws IOException
	{
		init();
		
		String queueID = calcQueueID( senderID, receivID );
		
		HistoryQueue historyQueue = getHistoryQueue( queueID );
		
		if ( historyQueue != null )
		{
			historyQueue.retriveEntries( firstUUID, eventHandler);
		}
	}


	
	
	


		
}
