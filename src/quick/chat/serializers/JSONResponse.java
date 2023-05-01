package quick.chat.serializers;

/*
 {
  "success": false,
  "payload": {
    // Application-specific data would go here. 
  },
  "error": {
    "code": 123,
    "message": "An error occurred!"
  }
}
 */

public class JSONResponse
{
	public static class Error
	{
		public int 		code 	= 0;
		public String 	message = null;
		
		public Error( int code, String message ) 
		{
			this.code = code;
			this.message = message;
		}
	}
	
	public boolean 	success = true;
	public String 	payload = null;
	public Error 	error	= null;
	
	public JSONResponse( 	boolean success, 
							String 	payload, 
							Error 	error )
	{
		this.success 	= success;
		this.payload 	= payload;
		this.error 	 	= error;
	}
	
	public static JSONResponse success( String payload )
	{
		return new JSONResponse( true, payload, null ); 
	}
	
	public static JSONResponse not_success( int errorCode, String errorMessage  )
	{
		return new JSONResponse( false, null, new JSONResponse.Error( errorCode, errorMessage ) ); 
	}
}
