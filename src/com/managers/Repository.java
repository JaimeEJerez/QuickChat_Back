package com.managers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.globals.Globals;

import quick.chat.utils.Util;

public class Repository
{
	public static File  calcDir(  String userID, String type )
	{
		String rootDirectory = Globals.rootDirectory;

		File repositoryFileDir = null;
		
		if ( type.equalsIgnoreCase( "DOCUMENT" ) )
		{
			repositoryFileDir = new File( rootDirectory + File.separator + Globals.documents_repository + File.separator + userID );
		}
		else
		if ( type.equalsIgnoreCase( "STATIC_IMAGE" ) )
		{
			repositoryFileDir = new File( rootDirectory + File.separator + Globals.static_images_repository + File.separator + userID );
		}
		else
		if ( type.equalsIgnoreCase( "DINAMIC_IMAGE" ) )
		{
			repositoryFileDir = new File( rootDirectory + File.separator + Globals.dinamic_images_repository+ File.separator + userID );
		}
		else
		if ( type.equalsIgnoreCase( "AUDIO" ) )
		{
			repositoryFileDir = new File( rootDirectory + File.separator + Globals.audio_repository+ File.separator + userID );
		}		

		return repositoryFileDir;
	}

	public static void write( String userID, String fileName, String type, String buff) throws IOException
	{
		File repositoryDir = calcDir( userID, type );
			
		if (!repositoryDir.exists())
		{
			Util.createDirectoryTree( repositoryDir.getAbsolutePath() );
		}
		
		String fPath =  repositoryDir.getAbsolutePath() +  File.separator  + fileName;
		
		File repositoryFile = new File( fPath );
		
		FileOutputStream fos = new  FileOutputStream( repositoryFile );

		DataOutputStream dos = new DataOutputStream( fos );
		
		dos.writeBytes(buff);
		
		dos.close();
	}

	public static void write( String userID, String fileName, String type, byte[] buff) throws IOException
	{
		File 	repositoryDir 	= calcDir( userID, type );
		
		if ( !repositoryDir.exists() )
		{
			Util.createDirectoryTree( repositoryDir.getAbsolutePath() );
		}
		
		String 	fPath 			=  repositoryDir.getAbsolutePath() +  File.separator  + fileName;
		File 	repositoryFile 	= new File( fPath );
		
		FileOutputStream fos = new  FileOutputStream( repositoryFile );
		DataOutputStream dos = new DataOutputStream( fos );
		
		dos.write(buff);
		
		dos.close();
	}
	
	public static byte[] read( String userID, String fileName, String type ) throws IOException
	{
		File 	repositoryDir 	= calcDir( userID, type );
		String 	fPath 			=  repositoryDir.getAbsolutePath() +  File.separator  + fileName;
		File 	repoFile 		= new File( fPath );
		
		FileInputStream fis = new FileInputStream( repoFile ) ;
		DataInputStream dis = new DataInputStream( fis );

		int size = dis.available();
		
		byte[] buff = new byte[size];
		
		dis.readFully( buff );
		
		dis.close();
		
		return buff;
	}
	
	public static boolean toStream( String userID, String fileName, String type, DataOutputStream dos, boolean addSize ) throws IOException
	{
		File repositoryDir = calcDir( userID, type );
		
		if (!repositoryDir.exists())
		{
			Util.createDirectoryTree( repositoryDir.getAbsolutePath() );
		}
		
		String fPath =  repositoryDir.getAbsolutePath() +  File.separator  + fileName;
		
		File imageFile = new File( fPath );
		
		if ( !imageFile.exists() )
		{
			return false;
		}
		
		FileInputStream fis = new FileInputStream( imageFile ) ;
		
		DataInputStream dis = new DataInputStream( fis );

		int size = dis.available();
		
		if ( addSize )
		{
			dos.writeInt( size );
		}
		
		for ( int i=0; i<size; i++)
		{
			int dada = fis.read();
			
			dos.write(dada  );
		}
		
		dis.close();
		
		return true;
	}

	
	
	

}
