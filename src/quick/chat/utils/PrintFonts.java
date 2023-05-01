package quick.chat.utils;


import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class PrintFonts
 */
@WebServlet("/PrintFonts")
public class PrintFonts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PrintFonts() 
    {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String fonts[] =   GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

	    for ( int i = 0; i < fonts.length; i++ )
	    {
	    	response.getWriter().append(fonts[i]).append("\r\n");
	    }		
	    
	    ServletContext context = getServletContext();
	    
	    URL resource = context.getResource( "fonts/Quicksand-SemiBold.ttf" );
	    	    
	    File fontFile = new File( resource.getFile() );
	    
	    try
		{
			Font STEFont = Font.createFont(Font.TRUETYPE_FONT, fontFile );
			
			//String f = STEFont.getFamily();
			
			response.getWriter().append(STEFont.getFontName()).append("\r\n");
		} 
	    catch (FontFormatException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
