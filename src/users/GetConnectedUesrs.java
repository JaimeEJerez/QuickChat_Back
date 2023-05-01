package users;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tcp.JSONResponse;
import com.tcp.Semaphore;

import quick.chat.db_io.MySQL;

/**
 * Servlet implementation class GetConnectedUesrs
 */
@WebServlet("/GetConnectedUesrs")
public class GetConnectedUesrs extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	Semaphore semaphore = Semaphore.getSingleton();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetConnectedUesrs() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
		
	    String securityToken = request.getHeader( "SecurityToken" );
	    
	    if ( securityToken == null || !securityToken.equalsIgnoreCase("602d544c-5219-42dc-8e46-883de0de7613"))
	    {
	    	gson.toJson( JSONResponse.not_success( 1000, "Invalid SecurityToken" ), osw );
	    	
	    	osw.flush();
	    	
	    	return;
	    }
		
		{			
			MySQL mySQL = new MySQL();
			
			JSONResponse posP;
			
			try
			{
				Hashtable<String, Semaphore> connTable = semaphore.getTable();
				
				Enumeration<String> enumK = connTable.keys();
				
				Vector<String> resultVect = new Vector<String>();
				
				while( enumK.hasMoreElements() )
				{
					resultVect.add(enumK.nextElement()) ;
				}
				
				//String query1 = "SELECT ID, DATE_TIME, EMAIL, DISPLAY_NAME, TYPE FROM USERS";

				//Vector<Map<String, String>> resultMap = mySQL.simpleHMapQuery( query1);
										
				posP = JSONResponse.success( resultVect );
			}
			finally
			{
				mySQL.disconect();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}

}
