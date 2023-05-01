package com.tcp;

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
    public Object 	payload = null;
    public Error 	error	= null;

    public JSONResponse( 	boolean success,
                            Object 	payload,
                            Error 	error )
    {
        this.success 	= success;
        this.payload 	= payload;
        this.error 	 	= error;
    }

    public static JSONResponse success( Object payload )
    {
    	//TraceListener.println( "JSONResponse.success(" + payload + ")" );
    	
        return new JSONResponse( true, payload, null );
    }

    public static JSONResponse not_success( int code, String error )
    {
		//TraceListener.println( "JSONResponse.not_success(" + error + ")" );

        return new JSONResponse( false, null, new Error( code, error ) );
    }

}
