package quick.chat.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.tcp.TraceListener;



public class Util
{

	static {
		System.setProperty("file.encoding",    "UTF-8");
		System.setProperty("sun.jnu.encoding", "UTF-8");
	}
	
	private static final int BUFFER_SIZE = 4096*10;
	 
	private static void extractFile(ZipInputStream zipIn, File file) throws IOException 
	{
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream( file ));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) 
        {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
	
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException 
	{		
	    File destFile = new File( destinationDir, zipEntry.getName() );

	    String destDirPath 		= destinationDir.getCanonicalPath();
	    String destFilePath 	= destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) 
	    {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}
	
	public static void unzip(String zipFilePath, String destDirectory) throws IOException 
	{
        File destDir = new File(destDirectory);
                
        if (!destDir.exists()) 
        {
        	TraceListener.println( "destDir.mkdir(): " + destDir.getAbsolutePath() );
        	
            destDir.mkdir();
        }
        
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        
        ZipEntry entry = zipIn.getNextEntry();
       
        // iterates over entries in the zip file
        while (entry != null) 
        {
        	File newFile = newFile( destDir, entry );
        	            
            if (!entry.isDirectory()) 
            {
                // if the entry is a file, extracts it
                extractFile(zipIn, newFile);
            } 
            else 
            {
                // if the entry is a directory, make the directory
            	
            	if ( newFile.exists() )
            	{
            		TraceListener.println( "deleteDeep( " + newFile.getAbsolutePath() + ", false )" );
                	
                	deleteDeep( newFile, false );
            	}
            	else
            	{
            		TraceListener.println( "newFile.mkdirs()( " + newFile.getAbsolutePath() + " )" );
            		
	                if ( !newFile.mkdirs() )
	                {
	                	throw new IOException( "Failed to create directory " + newFile.getAbsolutePath() );
	                }
            	}
            }  
            
            zipIn.closeEntry();
            
            try
            {
            	entry = zipIn.getNextEntry();
            }
            catch( java.io.IOException e )
            {
            	System.err.println( e.getMessage() );
            }
        }
    
    	zipIn.close();
    }
	
	public static void saveBufer( byte[] buff, File folder, String name ) throws IOException
	{
		File file = new File( folder, name );
			
		TraceListener.println( " saveBufer( " + file.getAbsolutePath() + " ) " );
		
		file.deleteOnExit();
		
		file.createNewFile();
			
		DataOutputStream fos = new DataOutputStream( new FileOutputStream( file ) );
			
		fos.write( buff );
			
		fos.close();		
	}
	
	public static byte[] loadBufer(File folder, String name )
	{
		File file = new File( folder, name );
		
		//DebugServer.println("------------------------------");
		//DebugServer.println( "LoadImage   :" + file.getAbsolutePath() );
		//DebugServer.println( "file.exists :" + file.exists() );
		//DebugServer.println( "file.canRead:" + file.canRead() );
		//DebugServer.println( "file.length :" + file.length() );
		
		if ( !file.exists() )
		{
			return null;
		}
		
		byte[] resultBuff = null;
		
		DataInputStream dis = null;
		
		try
		{
			dis = new DataInputStream( new FileInputStream( file ) );
			
			int buffSize = (int)file.length();
			
			resultBuff = new byte[buffSize];
			
			dis.readFully(resultBuff);
		} 
		catch (FileNotFoundException e) 
		{
			TraceListener.println( "FileNotFoundException:" + e.getMessage() );
		} 
		catch (IOException e) 
		{
			TraceListener.println( "IOException:" + e.getMessage() );
		}
		finally
		{
			if ( dis != null )
			{
				try 
				{
					dis.close();
				} 
				catch (IOException e) 
				{}
			}
		}
		
		
		
		return resultBuff; 
	}
	
	public static byte[] loadBuferFromFilePath( String filePath  )
	{
		File file = new File( filePath );
		
		if ( !file.exists() )
		{
			return null;
		}
		
		byte[] resultBuff = null;
		
		DataInputStream dis = null;
		
		try
		{
			dis = new DataInputStream( new FileInputStream( file ) );
			
			int buffSize = (int)file.length();
			
			resultBuff = new byte[buffSize];
			
			dis.readFully(resultBuff);
		} 
		catch (FileNotFoundException e) 
		{
			TraceListener.println( "FileNotFoundException:" + e.getMessage() );
		} 
		catch (IOException e) 
		{
			TraceListener.println( "IOException:" + e.getMessage() );
		}
		finally
		{
			if ( dis != null )
			{
				try 
				{
					dis.close();
				} 
				catch (IOException e) 
				{}
			}
		}
		
		
		
		return resultBuff; 
	}
	
	public static byte[] loadBuferFromFile( File file )
	{		
		if ( !file.exists() )
		{
			return null;
		}
		
		byte[] resultBuff = null;
		
		DataInputStream dis = null;
		
		try
		{
			dis = new DataInputStream( new FileInputStream( file ) );
			
			int buffSize = (int)file.length();
			
			resultBuff = new byte[buffSize];
			
			dis.readFully(resultBuff);
		} 
		catch (FileNotFoundException e) 
		{
			TraceListener.println( "FileNotFoundException:" + e.getMessage() );
		} 
		catch (IOException e) 
		{
			TraceListener.println( "IOException:" + e.getMessage() );
		}
		finally
		{
			if ( dis != null )
			{
				try 
				{
					dis.close();
				} 
				catch (IOException e) 
				{}
			}
		}
				
		return resultBuff; 
	}


	public static String add2Left( String input, int size, char add )
	{
		while ( input.length() < size )
		{
			input = add + input;
		}
		
		return input;
	}

	public static void createDirectoriesTree(int fisrtI, int lastI, String[] directories) throws IOException
	{
		TraceListener.println( "Util.createDirectoriesTree()" );
		
		for ( int i = fisrtI; i< lastI; i++ )
		{
			String dir = directories[i];
			
			File f = new File( dir );
	
			if ( !f.exists() )
			{								
				TraceListener.println( "dir:" + dir + " not found." );
				TraceListener.println( "File.mkdir(" + f.getAbsolutePath() + " )" );
				
				if ( !f.mkdir() )
				{					
					TraceListener.println( "Cant create dir:" + f.getAbsolutePath() );
					throw new IOException( "Cant create dir:" + f.getAbsolutePath() );
				}
			}
		}
	}

	public static String createDirectoryTree( String dirTree ) throws IOException
	{
		dirTree = dirTree.replace( File.separator , "_SEPARATOR_" );
		
		String[] split = dirTree.split( "_SEPARATOR_" );
		
		String dir = null;
						
		for ( String d: split )
		{		
			if ( dir == null )
			{
				dir = d;
			}
			else
			{
				dir = dir + File.separator + d;
			}
			
			if ( dir.length() > 1 )
			{				
				File f = new File( dir );

				if ( !f.exists() )
				{								
					TraceListener.println( "dir:" + dir + " not found." );
					TraceListener.println( "File.mkdir(" + f.getAbsolutePath() + " )" );
					
					if ( !f.mkdirs() )
					{					
						TraceListener.println( "Cant create dir:" + f.getAbsolutePath() );
						throw new IOException( "Cant create dir:" + f.getAbsolutePath() );
					}
				}
			}
		}
						
		return dir ;
	}

	public static void deleteDeep(File file, boolean deleteRoot )
	{		
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			
			for ( File f : files )
			{
				if ( f.isDirectory() )
				{
					deleteDeep( f, true );
				}
				else
				{
					f.delete();
				}
			}	
		}
		
		if ( deleteRoot )
		{
			file.delete();	
		}
	}
	
    public static boolean isValidEmailAddress(String email) 
    {
	   boolean result = true;
	   try 
	   {
	      InternetAddress emailAddr = new InternetAddress(email);
	      emailAddr.validate();
	   } 
	   catch (AddressException ex) 
	   {
	      result = false;
	   }
	   return result;
	}

	private static final String[] VALID_IP_HEADER_CANDIDATES = 
	{ 
	    "X-Forwarded-For",
	    "Proxy-Client-IP",
	    "WL-Proxy-Client-IP",
	    "HTTP_X_FORWARDED_FOR",
	    "HTTP_X_FORWARDED",
	    "HTTP_X_CLUSTER_CLIENT_IP",
	    "HTTP_CLIENT_IP",
	    "HTTP_FORWARDED_FOR",
	    "HTTP_FORWARDED",
	    "HTTP_VIA",
	    "REMOTE_ADDR" 
	 };

	public static String getClientIpAddress(HttpServletRequest request) 
	{
	    for (String header : VALID_IP_HEADER_CANDIDATES) 
	    {
	        String ipAddress = request.getHeader(header);
	        if (ipAddress != null && ipAddress.length() != 0 && !"unknown".equalsIgnoreCase(ipAddress)) 
	        {
	            return ipAddress;
	        }
	    }
	    return request.getRemoteAddr();
	}

	public static void printRequest( HttpServletRequest request )
	{
		Enumeration<String> pNames = request.getParameterNames();
		
		System.out.println( request.getRequestURL() + "?" );
		
		int c = 0;
		
		while ( pNames.hasMoreElements() )
		{
			String pName 	= pNames.nextElement();
			String pValue 	= request.getParameter(pName);
			
			if ( c++ > 0 )
			{
				System.out.println( "\t&" + pName + "=" + pValue );
			}
			else
			{
				System.out.println( "\t" + pName + "=" + pValue );
			}
			
		}
	}
	
	static public Vector<LinkedTreeMap<String,Object>>  getParamMapArray( InputStream is, Gson gson, boolean eliminateCommets ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader( is, "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	if ( eliminateCommets )
        	{
	        	int comment = line.lastIndexOf("//");
	        	
	        	if ( comment >= 0 )
	        	{
	        		line = line.substring( 0, comment);
	        	}
        	}
        	
        	jsontxt += ( line + "\r\n" );
        }

        if ( jsontxt.isEmpty() )
        {
        	return null;
        }
        
        jsontxt = jsontxt.trim();
        
        if ( jsontxt.startsWith("{") )
        {
        	jsontxt = "[" + jsontxt + "]";
        }
        
		@SuppressWarnings("unchecked")
		Vector<LinkedTreeMap<String,Object>> paramMapVect = (Vector<LinkedTreeMap<String,Object>>)gson.fromJson( jsontxt, Vector.class );

		return paramMapVect;
	}

	
	static public LinkedTreeMap<String,Object>  getParamMap( InputStream is, Gson gson, boolean eliminateCommets ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader( is, "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	if ( eliminateCommets )
        	{
	        	int comment = line.lastIndexOf("//");
	        	
	        	if ( comment >= 0 )
	        	{
	        		line = line.substring( 0, comment);
	        	}
        	}
        	
        	jsontxt += ( line + "\r\n" );
        }

        if ( jsontxt.isEmpty() )
        {
        	return null;
        }
                
		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,Object> paramMap = (LinkedTreeMap<String, Object>)gson.fromJson( jsontxt, LinkedTreeMap.class );

		return paramMap;
	}

	static public LinkedTreeMap<String,String>  getParamMap( HttpServletRequest request, Gson gson ) throws Exception, IOException
	{
		String jsontxt = "";
		
        String         line;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        
        while ((line = br.readLine()) != null)
        {
        	int comment = line.lastIndexOf("//");
        	
        	if ( comment >= 0 )
        	{
        		line = line.substring( 0, comment);
        	}
        	
        	jsontxt += ( line + "\r\n" );
        }

		@SuppressWarnings("unchecked")
		LinkedTreeMap<String,String> paramMap = (LinkedTreeMap<String, String>)gson.fromJson( jsontxt, LinkedTreeMap.class );

		return paramMap;
	}

	public static String calcElapsedTime( long date )
    {
    	if ( date == 0 )
    	{
    		return "NADA";
    	}
    	
    	long elapsed = (System.currentTimeMillis() - date) / 1000;
    	
    	if ( elapsed < 60 )
    	{
    		return "Ahora";
    	}
    	
    	elapsed = elapsed/60;
    	if ( elapsed < 60 )
    	{
    		return elapsed + " min";
    	}
    	
    	elapsed = elapsed/60;
    	if ( elapsed < 24 )
    	{
    		return elapsed + " hor";
    	}
    	
    	elapsed = elapsed/24;
    	
    	return elapsed > 1 ? elapsed + " dias" : elapsed + " dia" ;
    }

	public static String readTextFileFromURL(URL url) throws Exception
	{        
		StringBuilder sb = new StringBuilder();
		
        BufferedReader read = new BufferedReader( new InputStreamReader(url.openStream()));
        
        String text;
        
        while ((text = read.readLine()) != null)
        {
        	sb.append( text );
        }
        
        read.close();
        
        return sb.toString();
	}
}
