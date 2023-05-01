package quick.chat.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import quick.chat.utils.Util;

/**
 * Servlet implementation class getYOIContacts
 */
@WebServlet("/getYOIContacts")
public class getYOIContacts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getYOIContacts() 
    {
        super();
    }

    /*
  	URL url = new URL("https://backend-dev.yoifirst.com/api/users/api/users/my_referrers/");
	HttpURLConnection http = (HttpURLConnection)url.openConnection();
	http.setRequestProperty("Accept", "application/json");
	http.setRequestProperty("Authorization", "Bearer v4.local.WMGkpNtMLc5bzvtLiOCCIUM7neEVXGmjHZmP_PRroUUOHdQ0UUpdj9EM2-QKUaCeJaVEW8-ZFesf5UwwMzEeMNa0gLUwqwL48zAjmk_vPQ74sBa44VZGHTdZcRi38vQDJkqTKtp42pMzJat6OGzxwEBjpUYr_sYVrE1moth6uBuPrxHyg_IL4s33V15PU9fhXa8");

	System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
	http.disconnect();


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Gson	gson	= Globals.prettyPrinting ? new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() : new GsonBuilder().disableHtmlEscaping().create();

		HttpURLConnection con0 = null;
		LinkedTreeMap<String, Object> paramMap = null;
		try
		{
			URL url = new URL( "https://backend-dev.yoifirst.com/paseto_auth/token/" );
			
			con0 = (HttpURLConnection)url.openConnection();
			
			con0.setRequestMethod("POST");
			con0.setRequestProperty("Content-Type", "application/json");
			con0.setRequestProperty("Accept", "application/json");
			con0.setDoOutput(true);
			
		    String jsonContacts =    "{"
			    						+ "\"email\":\"brnmarq@gmail.com\","
			    						+ "\"password\":\"Prueba1234.\","
			    						+ "\"remember\":true"
		    						+ "}";
						
			OutputStream 	os 		= con0.getOutputStream();
		    byte[] 			input 	= jsonContacts.getBytes("utf-8");
		    os.write(input, 0, input.length);			
			
			paramMap = Util.getParamMap( con0.getInputStream(), gson, false );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( con0 != null )
			{
				con0.disconnect();
			}
		}
		
		if ( paramMap != null && 
			 paramMap.containsKey("access_token") && 
			 paramMap.containsKey("refresh_token") )
		{
			String access_token = (String)paramMap.get("access_token");
			
			URL url = new URL("https://backend-dev.yoifirst.com/api/users/my_referrers");
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestProperty("Accept", "application/json");
			http.setRequestProperty("Authorization", "Bearer " + access_token );
				
			int responseCode = http.getResponseCode();
			
			if ( responseCode == 200 )
			{
				try
				{
					InputStream is = http.getInputStream();
					
					Vector<LinkedTreeMap<String, Object>> paramMapVect = Util.getParamMapArray( is, gson, false );
				
					int size = paramMapVect.size();
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			http.disconnect();
		}
	}

}
