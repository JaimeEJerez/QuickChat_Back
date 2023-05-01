package quick.chat;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import quick.chat.utils.Util;


public class HTMLFormer3 
{
	private static class Key
	{
		public String key;
		
		public Key( String key )
		{
			this.key 	= key;
		}
	}
	
	BufferedReader  buffReader;
	
	char[] 						htmlArr 	= new char[1024*1000]; 
	Vector<Object> 				partsVect	= new Vector<Object>();
	Hashtable<String,String> 	valuesTable	= new Hashtable<String,String>();
	
	public void destroy()
	{
		htmlArr = null;
		partsVect.clear();
		partsVect = null;
		valuesTable.clear();
		valuesTable = null;
		buffReader = null;
	}

    public HTMLFormer3( HttpServlet servlet, URL  htmlFileURL ) throws Exception
    {
    	String text 		= Util.readTextFileFromURL( htmlFileURL );
    	Reader inputString 	= new StringReader(text);
    	
    	this.buffReader 	= new BufferedReader(inputString);
    	
    	init();
    }
    
    
    public HTMLFormer3( HttpServlet servlet, String  htmlFilePath ) throws IOException
    {
    	ServletContext 		servCntxt 	= servlet.getServletContext();
    	InputStream 		inpuStrem 	= servCntxt.getResourceAsStream( htmlFilePath );
    	InputStreamReader 	inputSRea 	= new  InputStreamReader( inpuStrem, StandardCharsets.ISO_8859_1 );
    			
    	this.buffReader = new BufferedReader( inputSRea );
    	
    	init();
    }
    
	public HTMLFormer3( BufferedReader  buffReader )
	{
		this.buffReader = buffReader;
	}

	public void init() throws IOException
	{
		int c=0;
		int l=0;
		int j=0;
		
		while ( ( c = buffReader.read() ) >= 0 )
		{
			htmlArr[j++] = (char)c;
			
			if ( c == '\r' )
			{
				htmlArr[j++] = '\n';
			}
			
			switch (l)
			{
				case 0:
					l = (c == '<' ? 1 : 0);
					break;
					
				case 1:
					l = (c == '!' ? 2 : 0);
					break;
					
				case 2:
					l = (c == '-' ? 3 : 0);
					break;
					
				case 3:
					l = (c == '-' ? 4 : 0);
					break;
					
				case 4:
					l = (c == '#' ? 5 : 0);
					
					if ( l != 0 )
					{
						String s = new String( htmlArr, 0, j-l );
						
						partsVect.add( s );
						
						j = 0;
					}
					break;
					
				case 5:
					if ( c == '>' )
					{
						String s = new String( htmlArr, 0, j-3 );
						
						partsVect.add( new Key( s ) );
						
						j = 0;
						l = 0;
					}
					break;
			}
		}
		
		if ( j > 0 )
		{
			String s = new String( htmlArr, 0, j );
			
			partsVect.add( s );
		}
	}
	
	
	public static String strArr2SelectOptions( String[] strArr )
	{
		StringBuffer sb = new StringBuffer();
		
		for ( String s : strArr )
		{
			sb.append( "\r\n\t<option value='" );
			sb.append( s );
			sb.append( "'>" );
			sb.append( s );
			sb.append( "</option>" );
		}
		
		return sb.toString();
	}
	
	public static String strVecte2selectOptions2Rows( Vector<String> vectArr, String pre, String pos, String selectedValue )
	{
		StringBuffer sb = new StringBuffer();
		
		int index = 0;
		
		for ( int i=0; i<vectArr.size()/2; i++ )
		{
			String s1 = vectArr.get(index++);
			String s2 = vectArr.get(index++);
			
			sb.append( "\r\n\t<option value='" );
			sb.append( s1 );
			sb.append( "'" );
			if ( selectedValue != null && s1.contains(selectedValue) )
			{
				sb.append( " selected" );
			}
			sb.append( ">" );
			if ( pre != null )
			{
				sb.append( pre );
			}
			sb.append( s2 );
			if ( pos != null )
			{
				sb.append( pos );
			}
			sb.append( "</option>" );
		}
		
		return sb.toString();
	}

	
	public static String strVecte2selectOptions( Vector<String> vectArr, String pre, String pos, String selectedValue )
	{
		StringBuffer sb = new StringBuffer();
		
		for ( String s : vectArr )
		{
			sb.append( "\r\n\t<option value='" );
			sb.append( s );
			sb.append( "'" );
			if ( selectedValue != null && s.contains(selectedValue) )
			{
				sb.append( " selected" );
			}
			sb.append( ">" );
			if ( pre != null )
			{
				sb.append( pre );
			}
			sb.append( s );
			if ( pos != null )
			{
				sb.append( pos );
			}
			sb.append( "</option>" );
		}
		
		return sb.toString();
	}

	
	public void addValue( String key, String value )
	{
		if ( value == null )
		{
			value = "";
		}
		
		valuesTable.put(key, value);
	}
	
	public void realice( PrintWriter printWriter )
	{
		//printWriter.append( "partsVect:" + partsVect.size() +"\r\n" );

		for ( Object kb : partsVect )
		{
			if ( kb instanceof Key )
			{
				Key k= (Key)kb;
				
				String s = valuesTable.get( k.key );
				
				printWriter.append( s );
			}
			else
			{
				printWriter.append( kb.toString() );
			}
		}
		
		printWriter.flush();
	}

	public String realice2String()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PrintWriter pw = new PrintWriter( baos );
				    				
		this.realice( pw );
		
		pw.flush();
		
		String string = new String( baos.toByteArray(), StandardCharsets.UTF_8 );
		
		return string;
}


}
