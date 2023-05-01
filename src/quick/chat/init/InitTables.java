package quick.chat.init;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import quick.chat.db_io.MySQL;


/**
 * Iicializa las table de la base de datos
 */
@WebServlet("/InitTables")
public class InitTables extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
    
	private PrintWriter printWrtr	= null;
		
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InitTables() 
    {
        super();
    }

    private boolean createTable( MySQL mysql, String tableName, String command )
    {				
		String result1 = mysql.simpleQuery( "SHOW TABLES LIKE '" + tableName + "'" );
		
		if ( result1 != null && result1.equalsIgnoreCase( tableName) )
		{
			printWrtr.append( "La tabla '" + tableName + "' ya  existe.\r\n" );
		}
		else
		{
			mysql.executeCommand(command);
			
			if ( mysql.getLastError() != null )
			{				
				printWrtr.append( "Error interno:" + mysql.getLastError() + " " + command + "\r\n" );
				
				return false;
			}
			
			printWrtr.append( "La tabla '" + tableName + "' fue creada.\r\n" );
			
			return true;
		}
		
		return false;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{		
		MySQL mysql = new MySQL();

		printWrtr = response.getWriter();
		
		File rootDir = new File( request.getServletContext().getRealPath("/db_tables/") );
	
		File[] files = rootDir.listFiles();
		
		for ( File f : files )
		{
			if ( f.getName().toLowerCase().endsWith(".sql") ) 
			{
				StringBuilder contentBuilder = new StringBuilder();
				
		        try (BufferedReader br = new BufferedReader(new FileReader( f ))) 
		        {
		            String sCurrentLine = br.readLine();
		            
		            if ( sCurrentLine != null && sCurrentLine.toUpperCase().startsWith( "CREATE TABLE" ) )
		            {
		            	contentBuilder.append(sCurrentLine).append("\n");
		            	
			            String[] split = sCurrentLine.split(" ");
			            
			            if ( split.length >= 3 )
			            {
				            String tableName = split[2];
				            		
				            while ((sCurrentLine = br.readLine()) != null) 
				            {
				                contentBuilder.append(sCurrentLine).append("\n");
				            }
				            
				            String[] commands = contentBuilder.toString().split(";");	
				            				             
				            if ( createTable( mysql, tableName, commands[0] ) )
				            {
					            if ( commands.length > 1 && !commands[1].trim().isEmpty())
					            {
					            	mysql.executeCommand(commands[1]);
					    			
					    			if ( mysql.getLastError() != null )
					    			{				
					    				printWrtr.append( "Error interno:" + mysql.getLastError() + " " + commands[1] + "\r\n" );
					    			}
					            }
				            }
			            }
		            }
		        } 
		        catch (IOException e) 
		        {
		            e.printStackTrace();
		        }
		        
		     }
		}
				
		mysql.disconect();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{  
		doGet(request, response);
	}

}
