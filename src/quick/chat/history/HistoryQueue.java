package quick.chat.history;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.pojo.ChatMessageCore;

import quick.chat.history.HistoryManager.BeforeToWrite;
import quick.chat.history.HistoryManager.ReplaceEntryEvent;
import quick.chat.history.HistoryManager.RetriveEntriesEventHandler;



public class HistoryQueue
{
	
	private 	 	String 					filePath	= null;
	private 		ChatMessageCore			lastElement = null;
	
	
	public HistoryQueue(String filePath ) throws IOException
	{
		this.filePath = filePath;

		File file = new File(filePath);

		// if file does exists:
		if ( !file.exists() )
		{
			createEmptyFile(this.filePath);
		}
	}
	
	/** Creates an empty file with no content (0 bytes size) with given filename. */
	private void createEmptyFile(String filePath) throws IOException
	{
		File emptyFile = new File(filePath);

		try
		{
			if (!emptyFile.createNewFile())
			{
				boolean success = false;
	
				for (int i = 0; i < 7 && !success; i++)
				{
					try
					{
						Thread.sleep(1000);
					} 
					catch (InterruptedException e)
					{
					}
	
					if (emptyFile.exists())
					{
						emptyFile.delete();
					}
	
					success = emptyFile.createNewFile();
				}
	
				if (!success)
				{
					throw new IOException("Could not create new file: " + filePath);
				}
			}
		}
		catch ( IOException e )
		{
			throw new IOException("IOException: " + e.getMessage() + "\r\nCould not create new file: " + filePath);
		}
	}

	public ChatMessageCore getLastElement()
	{
		if ( lastElement != null)
		{
			return lastElement;
		}
		
		String theLastElement = null;
		
		FileInputStream fis;
		
		try
		{
			if ( !(new File(filePath)).exists() )
			{
				return null;
			}
			
			fis = new FileInputStream(filePath);
			
			// loop over all data that is in that file
			while (fis.available() > 0)
			{
				// a new object input stream to read in the next object
				DataInputStream ois = new DataInputStream(fis);
				try
				{
					theLastElement = ois.readUTF();
				} 
				catch (ClassCastException e)
				{
					ois.close();
					throw new IOException(e.toString());
				} 
				catch (StreamCorruptedException e)
				{
					ois.close();
					throw new IOException(e.toString());
				}

			}

			fis.close();
		} 
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if ( theLastElement != null )
		{
			lastElement = (ChatMessageCore)JsonReader.jsonToJava( theLastElement );
		}
		
		return lastElement;
	}
	
	public synchronized void add(ChatMessageCore chatMessage, BeforeToWrite beforeToWrite) throws IOException
	{
		appendEntryToFile( filePath, chatMessage, beforeToWrite );

		lastElement = chatMessage;
	}

	/** Attaches an entry to the file with given filename. 
	 * @param beforeToWrite */
	private synchronized long appendEntryToFile(String filename, ChatMessageCore chatMessage, BeforeToWrite beforeToWrite ) throws IOException
	{
		FileOutputStream   	fos = new FileOutputStream(filename, true);
		DataOutputStream 	oos = new DataOutputStream(fos);

		long position = fos.getChannel().position();
		
		chatMessage.setId(position);
		
		if ( beforeToWrite != null )
		{
			beforeToWrite.action(chatMessage);
		}
		
		String jsonStr = JsonWriter.objectToJson( chatMessage );

		oos.writeUTF(jsonStr);

		oos.flush();
		oos.close();
		fos.flush();
		fos.close();
		
		return position;
	}

	public String getEntry(long messageID) throws IOException
	{
		String entry = null;
		
		File f = new File( filePath );

		if ( f.exists() )
		{
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			
			try
			{				
				long fileSize = fis.available();
				
				if ( fileSize > messageID )
				{
					fis.skip(messageID);
						
					entry = dis.readUTF();
				}
			}
			catch (ClassCastException e)
			{
				if ( dis != null )
				{
					dis.close();
				}
				if ( fis != null )
				{
					fis.close();
				}
				throw new IOException(e.toString());
			} 
			catch (StreamCorruptedException e)
			{
				if ( dis != null )
				{
					dis.close();
				}
				if ( fis != null )
				{
					fis.close();
				}
				throw new IOException(e.toString());
			} 
			catch (FileNotFoundException e)
			{
				if ( dis != null )
				{
					dis.close();
				}
				if ( fis != null )
				{
					fis.close();
				}
				throw new IOException(e.toString());
			} 
			catch (IOException e)
			{
				if ( dis != null )
				{
					dis.close();
				}
				if ( fis != null )
				{
					fis.close();
				}
				throw new IOException(e.toString());
			}

			dis.close();
			fis.close();
		}
		
		return entry;
	}
	
	public void replaceEntry( long messageID, ReplaceEntryEvent ree ) throws IOException
	{		
		String oldEntry 	= getEntry(messageID);
		
		if ( oldEntry == null )
		{
			return;
		}
		
		String newEntry 	= ree.action(oldEntry);
		
		if ( newEntry.length() > oldEntry.length()  )
		{
			throw new IOException( "ERROR: Invalid entrie size in replaceEntry()" );
		}
		
		while ( newEntry.length() < oldEntry.length() )
		{
			newEntry += " ";
		}
		
		File f = new File( filePath );

		if ( f.exists() )
		{
			ByteArrayOutputStream 	bas 	= new ByteArrayOutputStream();
			DataOutputStream		dos		= new DataOutputStream( bas );
			dos.writeUTF(newEntry);
			dos.flush();
			
			ByteBuffer buff = ByteBuffer.wrap( bas.toByteArray() );
			
			RandomAccessFile		writer 	= new RandomAccessFile( f, "rw" );
			FileChannel 			channel = writer.getChannel();
		 			
			long size = channel.size();
			
			channel.position(messageID);
		    channel.write(buff);
		    
		    channel.position(size);

		    channel.close();
		    writer.close();
		}
	}
	
	public synchronized void retriveEntries( Long firstUUID, RetriveEntriesEventHandler eventHandler ) throws IOException
	{
		File f = new File( filePath );
		
		if ( f.exists() )
		{
			long lastValidPosition = 0;
			
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			
			// loop over all data that is in that file
			while ( fis.available() > 0 )
			{
				String pqentry;
	
				// a new object input stream to read in the next object
				try
				{
					pqentry = dis.readUTF();
					
					lastValidPosition = fis.getChannel().position();
					
					ChatMessageCore message = (ChatMessageCore)JsonReader.jsonToJava( pqentry );
							
					if ( firstUUID == null || firstUUID == message.getId() )
					{
						if ( !eventHandler.handleEvent(message) )
						{
							break;
						}
					}
				} 
				catch ( EOFException e )
				{
					File file = new File(filePath);
					
					FileOutputStream fos = new FileOutputStream( file );
					
					fos.getChannel().truncate(lastValidPosition);
					
					fos.close();
					
					if ( dis != null )
					{
						dis.close();
					}
					if ( fis != null )
					{
						fis.close();
					}
					
					//throw new IOException(e.toString());
				}
				catch (ClassCastException e)
				{
					fis.getChannel().truncate(lastValidPosition);
					
					if ( dis != null )
					{
						dis.close();
					}
					if ( fis != null )
					{
						fis.close();
					}
					
					throw new IOException(e.toString());
				} 
				catch (StreamCorruptedException e)
				{
					fis.getChannel().truncate(lastValidPosition);
					
					if ( dis != null )
					{
						dis.close();
					}
					if ( fis != null )
					{
						fis.close();
					}
					throw new IOException(e.toString());
				}
			}
	
			dis.close();
			fis.close();
		}
	}

}
