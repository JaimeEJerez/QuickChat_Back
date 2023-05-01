package quick.chat.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.servlet.ServletContext;

public class RenderText
{
	
	private static final Hashtable<String,Font> fontTable = new Hashtable<String,Font>();
	
	public static Font loadFont( ServletContext context, String fontPath, float size ) throws  IOException
	{
		String key = fontPath + ":" + size;
		
		if ( fontTable.contains( key ) )
		{
			return fontTable.get( key );
		}
		
		Font font;
		    
		URL resource = context.getResource( fontPath );
    	    
		File fontFile = new File( resource.getFile() );
		
		try
		{
			font = Font.createFont(Font.TRUETYPE_FONT, fontFile );
		} 
		catch (FontFormatException | IOException e)
		{
			throw new IOException( "FontFormatException" );
		}		
		
		font = font.deriveFont( size );

		fontTable.put( key, font );
		
		return font;
	}
    
	public static void render( Graphics2D g2d, Font f, int left, int rght, int top, int bott, String text, int justify )
	{
		String[] 	words 		= text.split(" ");
		int 		sh 			= g2d.getFontMetrics().getHeight();
		int 		margin		= sh/10;
		int 		ty 			= top + sh/4;
		int			width		= rght-left - (2*margin);
		String 		textLine 	= "";

		g2d.setFont( f ); 
		
		int c = 0;
		
		for ( int i = 0; i<=words.length; i++ )
    	{
    		int strWidth = i<words.length ? g2d.getFontMetrics().stringWidth( textLine + " " + words[i] ) : g2d.getFontMetrics().stringWidth( textLine );
    		
    		if ( strWidth >= width || c == words.length )
    		{
    			int sw = g2d.getFontMetrics().stringWidth( textLine );
    			int tx = left + margin + (width-sw)/2;
    			
    			switch ( justify )
    			{
    				case -1://left
    					tx = left + margin;
    					break;
    				case 1://right
    					tx = rght - sw - margin;
    					break;
    				default: //center
    					tx = left + margin + (width-sw)/2;
    			}
    			
    			/*
    	    	g2d.setColor( Color.WHITE );
    	    	for ( int ii=-1; ii<=1; ii++ )
    	    	{
    	    		for ( int jj=-1; jj<=1; jj++ )
    	    		{
    	    			g2d.drawString( textLine, tx+ii, ty+jj );
    	    		}
    	    	}*/
    	    		    	
    	    	g2d.setColor( Color.decode( "#6e6e6e" ) );
    	    	g2d.drawString( textLine, tx, ty );
    	    	
    	    	textLine = "";
    	    	ty += sh;
    		}

    		c++;
    		
    		if ( i<words.length )
    		{
    			textLine += ( textLine.isEmpty() ? words[i] : ( " " + words[i] ) );
    		}
    	}
		
	}

}
