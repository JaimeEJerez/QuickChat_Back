package users;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.tcp.JSONResponse;
import com.tcp.Semaphore;
import com.tcp.TraceListener;

import quick.chat.db_io.MySQL;
import quick.chat.utils.Util;



/**
 * Servlet implementation class LogIn
 */

@WebServlet("/AutoLogIn")
public class AutoLogIn extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
	private static final Object sychronizationObj = new Object();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AutoLogIn() 
    {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		final String mail = "finola9410@ekcsoft.com";
		final String pass = "Prueba.1";
		
		TraceListener.println( "AutoLogIn.doPost( " + Globals.guestName + ")" );
		
	    response.setContentType("application/json");
	    response.setStatus( HttpServletResponse.SC_OK );
	    response.setCharacterEncoding("UTF-8");
		
		Gson 				gson 	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();
		OutputStreamWriter 	osw 	= new OutputStreamWriter (response.getOutputStream(), Charset.forName("UTF-8").newEncoder()  );
			
	    if ( Globals.guestName.equalsIgnoreCase( "YOY_SSL" ) )
	    {
			HttpURLConnection 				con0 		= null;
			LinkedTreeMap<String, Object> 	paramMap 	= null;
			try
			{
				URL url = new URL( "https://backend-dev.yoifirst.com/paseto_auth/token/" );
				
				con0 = (HttpURLConnection)url.openConnection();
				
				con0.setRequestMethod("POST");
				con0.setRequestProperty("Content-Type", "application/json");
				con0.setRequestProperty("Accept", "application/json");
				con0.setDoOutput(true);
				
			    String jsonContacts =    "{"
				    						+ "\"email\":\"" + mail + "\","
				    						+ "\"password\":\"" + pass + "\","
				    						+ "\"remember\":true"
			    						+ "}";
				
			    TraceListener.println( "AutoLogIn.doPost - paseto_auth" );
			    
				OutputStream 	os 		= con0.getOutputStream();
			    byte[] 			input 	= jsonContacts.getBytes("utf-8");
			    os.write(input, 0, input.length);			
				
				paramMap = Util.getParamMap( con0.getInputStream(), gson, false );
				
				if ( paramMap == null || paramMap.size() == 0 )
				{
					TraceListener.println( "AutoLogIn.doPost - paramMap null or empty" );
					
					gson.toJson( JSONResponse.not_success( 9784, "paseto_auth return empty" ), osw );
				}
				else
				{
					TraceListener.println( "AutoLogIn.doPost - paramMap.size=" + paramMap.size() );
					
					if ( paramMap != null && paramMap.containsKey("access_token") )
					{
						String accessToken = (String)paramMap.get("access_token");
						
						TraceListener.println( "AutoLogIn.doPost - paramMap.access_token=" + accessToken );

						Map<String, String> map = new HashMap<String, String>();
						
						//  "A" -> Agente
						//  "R" -> Referente
						//  "I" -> Inversionista
						
						map.put( "TYPE", "A" );
						map.put( "UUID", "457" );
					    map.put( "DISPLAY_NAME", "Agente-finola9410" );
					    map.put( "EMAIL", mail );
					    map.put( "PASSWORD", pass );
					    map.put( "TOKEN", accessToken );
					    
					    gson.toJson( JSONResponse.success( map ), osw );
					}
					else
					{
						TraceListener.println( "AutoLogIn.doPost - paseto_auth  'null access_token' " );
						
						gson.toJson( JSONResponse.not_success( 9784, "paseto_auth 'null access_token' " ), osw );
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				
				TraceListener.printException( getServletInfo(), e );
				
				gson.toJson( JSONResponse.not_success( 9786, e.getMessage() ), osw );
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				
				TraceListener.printException( getServletInfo(), e );
				
				gson.toJson( JSONResponse.not_success( 9787, e.getMessage() ), osw );
			}
			finally
			{
				if ( con0 != null )
				{
					con0.disconnect();
				}
			}
	    
	    }
	    else
		{
			MySQL mysql = new MySQL();
			
			try
			{
				String query = 	"SELECT ID, DISPLAY_NAME, EMAIL, PASSWORD FROM USERS";

				String[] usrsArr = mysql.simpleAQuery( query );
	
				if ( mysql.getLastError() != null )
				{
					gson.toJson( JSONResponse.not_success( 0, mysql.getLastError() ), osw ); 
				}
				else
				{
					Map<String, String> map = new HashMap<String, String>();

					Semaphore semaphore = Semaphore.getSingleton();
					
					synchronized ( sychronizationObj )
					{
						for ( int i=0; i<usrsArr.length; i+=4 )
						{
							String userID 	= usrsArr[i];
							String userName = usrsArr[i+1];
							String userMail = usrsArr[i+2];
							String password = usrsArr[i+3];
							
							String userCode = "SU" + String.format("%08X", Integer.valueOf(userID));
							
							if ( semaphore.isOnline(userCode) )
							{
								TraceListener.println( userID + " is online." );
								
								continue;
							}
							
							TraceListener.println( userID + " is NOT online." );
							
						    map.put( "TYPE", "U" );
							map.put( "UUID", userID );
						    map.put( "DISPLAY_NAME", userName );
						    map.put( "EMAIL", userMail );
						    map.put( "PASSWORD", password );
						    map.put( "TOKEN", "602d544c-5219-42dc-8e46-883de0de7613" );
	  	    
						    gson.toJson( JSONResponse.success( map ), osw );
						    				
						    break;
						}
					}
					
					if ( map.size() == 0 )
					{
						gson.toJson( JSONResponse.not_success( 0, "There are no free users, at this moment, if you like, wait a moment and try again." ), osw );
					}
				}
			}
			finally
			{
				mysql.disconect();
			}
		}
		
	    TraceListener.println( "AutoLogIn.doPost - flush" );
	    
		osw.flush();
	}

}
