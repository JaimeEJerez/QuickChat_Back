package quick.chat.history;

import java.io.IOException;

import com.pojo.ChatMessageCore;


public abstract class HistoryManager
{
	public static abstract class RetriveEntriesEventHandler
	{
		public abstract boolean handleEvent( ChatMessageCore message );
	};

	public static abstract class ReplaceEntryEvent
	{
		public abstract String action( String entry );
	}
	
	public static abstract class BeforeToWrite
	{
		public abstract boolean action( ChatMessageCore message );
	}

	protected static  HistoryManager self = null;
	
	
	public HistoryManager()
	{
		self = this;
	}
	
	public abstract void addMessage2History( ChatMessageCore chatMessage, BeforeToWrite beforeToWrite ) throws IOException;
	
	public abstract ChatMessageCore retriveLastMessagesFromHistory( String senderID, String receivID ) throws IOException;

	public abstract void retriveMessagesFromHistory(Long 	firstUUID, 
													String 	senderID, 
													String 	receivID, 
													RetriveEntriesEventHandler eventHandler ) throws IOException;

	public abstract ChatMessageCore replaceMessagesFromHistory( String senderID, String receivID, long messageID, ReplaceEntryEvent ree ) throws IOException;

}
