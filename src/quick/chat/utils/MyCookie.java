package quick.chat.utils;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class MyCookie extends javax.servlet.http.Cookie
{
	/**
	 * 
	 */
	private static final long 		serialVersionUID 	= -7609853943947028343L;
	
	
	public static enum CookieNames 
	{
		EMPTY,
		USER_UUID,
		USER_DISPLAY_NAME,
		RECEIVER_UUID,
		RECEIVER_MAIL,
		RECEIVER_DISPLAY_NAME,
		ACTIVE_RECEPTOR;
	}
		
	public MyCookie( CookieNames name, String value, int secunds ) 
	{
		super( name.name(), value );
		
		if ( name.equals(CookieNames.EMPTY) )
		{
			this.setMaxAge( secunds );
		}
		else
		{
			this.setMaxAge(-1);
		}
	}

	
	public MyCookie( CookieNames name, String value ) 
	{		
		super( name.name(), value.replace("\\", "/") );
		
		if ( name.equals(CookieNames.EMPTY) )
		{
			this.setMaxAge(0);
		}
		else
		{
			this.setMaxAge(-1);
		}
	}
	
	public static String getCookiesValueByName( HttpServletRequest request, CookieNames name ) throws IOException
	{
		Cookie cookie = getCookiesByName( request, name );
		
		return cookie == null ? null : cookie.getValue();
	}
	
	public static void clearCookiesValueByName( HttpServletRequest request, CookieNames name ) throws IOException
	{
		Cookie cookie = getCookiesByName( request, name );
		
		if ( cookie != null )
		{
			cookie.setMaxAge( 0 );
		}
	}
	
	public static Cookie getCookiesByName( HttpServletRequest request, CookieNames name ) throws IOException
    {		
		Cookie[] cookies = request.getCookies();
		
		if ( cookies != null )
		{
			for ( Cookie c : cookies )
			{
				if ( c.getName().equalsIgnoreCase( name.name() ) && c.getMaxAge() != 0 )
				{
					return c;
				}
			}
		}
		
		return null;
    }

	public String toString()
	{
		return  "MyCookie: "+ this.getName() + " , " + this.getValue() + " , " + this.getMaxAge();
	}
}

