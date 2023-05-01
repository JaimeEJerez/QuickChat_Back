package quick.chat.utils;


import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet para generar c�digos cortos alfanum�ricos para el sistema de autenticaci�n
 */
@WebServlet("/GeneraCodigos")
public class GeneraCodigos extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static final char[] chars = { '0','1','2','3','4','5','6','7','8','9','0','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'}; 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeneraCodigos() 
    {
        super();
    }

    private String generateCode()
    {
    	String retValue = "";
    	
    	for ( int i=0; i<4; i++ )
    	{
    		int indx = (int)Math.round( Math.random() * (chars.length-1) );
    		
    		retValue += chars[indx];
    	}
    	
    	return retValue;
    }
        
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String nStr = request.getParameter("n");
		
		if ( nStr == null )
		{
			response.getWriter().append( "Error: falt� el parametro n , GeneraCodigos?n=X que representa el n�mero de codigos a generar" );
			
			return;
		}
		
		int n = Integer.valueOf( nStr );
		
		
		Hashtable<String,String> codesTable = new Hashtable<String,String>();
		
		int i=0;
				
		try
		{
			response.getWriter().append( "---------------------------------\r\n" );
			
			while ( i<n )
			{
				String code = generateCode();
				
				if ( !codesTable.containsKey(code) ) 
				{
					codesTable.put( code, code );
					
					i++;
					
					response.getWriter().append( String.valueOf(i) ).append( "\t" ).append( code ).append( "\r\n" );
				}
			}		
		}
		finally
		{
			response.getWriter().append( "---------------------------------\r\n" );
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
