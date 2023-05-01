package quick.chat.history;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.pojo.ChatContent;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;

import quick.chat.db_io.MySQL;

public class SQLHistoryManeger extends HistoryManager
{

	public static synchronized HistoryManager getSingleton()
	{
		return self==null ? (self=new SQLHistoryManeger()) : self;
	}
	
	/*
	// the mysql insert statement
      String query = " insert into users (first_name, last_name, date_created, is_admin, num_points)"
        + " values (?, ?, ?, ?, ?)";

      // create the mysql insert preparedstatement
      PreparedStatement preparedStmt = conn.prepareStatement(query);
	 */

	@Override
	public void addMessage2History(ChatMessageCore m, BeforeToWrite beforeToWrite ) throws IOException
	{
		String jsonStr = JsonWriter.objectToJson( m.getContent() );
		
		jsonStr = jsonStr.replaceAll( "\"", "'" );
		
		String command = "INSERT INTO CHAT_HISTORY ( "
						+ "TIME, "
						+ "SENDER_TYPE, SENDER_NAME, SENDER_ID, "
						+ "RECEIVER_TYPE, RECEIVER_NAME, RECEIVER_ID, "
						+ "CONTENT ) "
						+ "VALUES ("
						+ m.getTime() + "," 
						+ "\"" + m.getSenderType() + "\"," 
						+ "\"" + m.getSenderName() + "\","
						+ "\"" + m.getSenderID() + "\","
						+ "\"" + m.getReceiverType() + "\"," 
						+ "\"" + m.getReceiverName() + "\","
						+ "\"" + m.getReceiverID() + "\","
						+ "\"" + jsonStr + "\" )";
		
		MySQL mySql = new MySQL();
		
		try
		{
			mySql.executeCommand(command);
			
			long id = mySql.get_mysql_insert_id();
			
			m.setId( id );
		}
		finally
		{
			mySql.disconect();
		}
		
		if ( mySql.getLastError() != null )
		{
			throw new IOException( "mySql ERROR:" + mySql.getLastError() );
		}
	}
	
	@Override
	public ChatMessageCore retriveLastMessagesFromHistory( final String senderID, final String receivID) throws IOException
	{
		final ChatMessageCore[] chatMessageCoreArr = new ChatMessageCore[1];
				
		retriveMessagesFromHistoryLimit( null, senderID, receivID, true, new RetriveEntriesEventHandler()
		{
			@Override
			public boolean handleEvent( ChatMessageCore message )
			{
				chatMessageCoreArr[0] = message;
				
				return false;
			}
		});
		
		return chatMessageCoreArr[0];
	}

	@Override
	public void retriveMessagesFromHistory( final Long		firstUUID,
											final String 	id1, 
											final String 	id2, 
											final RetriveEntriesEventHandler eventHandler) throws IOException
	{
		retriveMessagesFromHistoryLimit( firstUUID, id1, id2, false, eventHandler );
	}
	
	private void retriveMessagesFromHistoryLimit( 	final Long		firstUUID, 
													final String 	id1, 
													final String 	id2, 
													final boolean 	lastElement,
													final RetriveEntriesEventHandler eventHandler) throws IOException
	{
		String query;
	
		if ( id2.startsWith("G") )
		{
			query = "SELECT " 
			+ "UUID, TIME, "
			+ "SENDER_TYPE, SENDER_NAME, SENDER_ID,"
			+ "RECEIVER_TYPE, RECEIVER_NAME, RECEIVER_ID, CONTENT "
			+ "FROM CHAT_HISTORY "
			+ "WHERE RECEIVER_ID=\"" + id2 + "\" ORDER BY ID" ;
		}
		else
		{
			query = "SELECT " 
			+ "UUID, TIME, "
			+ "SENDER_TYPE, SENDER_NAME, SENDER_ID,"
			+ "RECEIVER_TYPE, RECEIVER_NAME, RECEIVER_ID, CONTENT "
			+ "FROM CHAT_HISTORY "
			+ "WHERE "
			+ "( RECEIVER_ID=\"" + id1 + "\" AND SENDER_ID=\"" + id2 + "\" ) OR "
			+ "( RECEIVER_ID=\"" + id2 + "\" AND SENDER_ID=\"" + id1 + "\" ) " 
			+ "ORDER BY ID" ;
		}
		
		if ( lastElement )
		{
			query += " DESC LIMIT 1";
		}
		
		MySQL mySql = new MySQL();
		
		try
		{
			mySql.callBackQuery(query, null, new MySQL.QueryCallBack()
			{
				@Override
				public boolean execute(ResultSet rs, int rowCount, int columnNumber, Object linkObj) throws SQLException, IOException
				{
					int c = 1;
					
					long 		ID 				= rs.getLong( c++ );
					long   		TIME 			= rs.getLong( c++ );
					char		SENDER_TYPE		= rs.getString( c++ ).charAt(0);
					String 		SENDER_NAME 	= rs.getString( c++ );
					String 		SENDER_ID 		= rs.getString( c++ );
					char		RECEIVER_TYPE	= rs.getString( c++ ).charAt(0);
					String 		RECEIVER_NAME 	= rs.getString( c++ );
					String 		RECEIVER_ID 	= rs.getString( c++ );
					String		CONTENT_TXT		= rs.getString( c++ ).replaceAll("'", "\"");
					ChatContent	CONTENT			= (ChatContent)JsonReader.jsonToJava( CONTENT_TXT  );
					
					ChatMessageCore message = new ChatMessageCore(	CONTENT, 
																	SENDER_TYPE, SENDER_NAME, SENDER_ID,
																	RECEIVER_TYPE, RECEIVER_NAME, RECEIVER_ID,
																	TIME );
					
					message.setId( ID );
					
					eventHandler.handleEvent( message );
					
					return true;
				}
				
			});
		}
		finally
		{
			mySql.disconect();
		}
	
	}

	@Override
	public ChatMessageCore replaceMessagesFromHistory(String senderID, String receivID, long messageID, ReplaceEntryEvent ree) throws IOException
	{
		// TODO Auto-generated method stub
		return null;
	}


	

}
