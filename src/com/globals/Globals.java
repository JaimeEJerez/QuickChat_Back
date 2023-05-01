

package com.globals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import quick.chat.history.HistoryManager;
import quick.chat.history.QueueHistoryManager;

public class Globals
{
	public static enum Mode
	{
		PRODUCTION,
		DEVELOP
	};
	
	public static class Parameters
	{
 		public String		fileName;
		public String 		guestName;
		public String 		mysqlUser;
		public String 		mysqlPass;
		public String 		kWebsocketServerIP;
		public String 		kWEB_Socket_Server;
		public String 		kServer_API_URL;
		public String 		kGetQCContactsURLtest;
		public String 		kGetQCContactsURLprod;
	}
		
	public static final String 	dataBase 	= "quick_chat";
	
	public static final Mode 	mode		= Mode.DEVELOP;
	public static final String  mysqlHost 	= "localhost";
	
	
	public static final String  avatarImgsDir 				= "AvatarPictures";
	public static final String  documents_repository 		= "documents_repository";
	public static final String  static_images_repository 	= "static_images_repository";
	public static final String  dinamic_images_repository 	= "dinamic_images_repository";
	public static final String  audio_repository 			= "audio_repository";
	public static final String	rootDirectory				= File.separator.equals("/") ? "/home/quick_chat/"      : "c:\\home\\quick_chat\\";
	public static final String	tempDirectory				= File.separator.equals("/") ? "/home/quick_chat/temp/" : "c:\\home\\quick_chat\\temp\\";

	
	public static final int 	kTCPReceiveListenerPort 	= 17013;
	public static final int 	kDebugListenerPort 			= 17014;
	public static final int 	kTCPSendListenerPort		= 17015;
	public static final int 	kWebsocketServerPort		= 17017;
	
	public static String  		guestName					= null;	
	public static String  		mysqlUser 					= null;
	public static String  		mysqlPass 					= null;
	public static String 		kWebsocketServerIP			= null;
	public static String		kWEB_Socket_Server			= null;
	public static String		kServer_API_URL				= null;
	public static String		kGetQCContactsURLtest		= null;
	public static String		kGetQCContactsURLprod		= null;

	public static boolean prettyPrinting = false;
		
	static
	{
		try
		{
			Parameters p = loadParameters();
			
			Globals.guestName				= p.guestName;	
			Globals.mysqlUser 				= p.mysqlUser;
			Globals.mysqlPass				= p.mysqlPass;
			Globals.kWebsocketServerIP		= p.kWebsocketServerIP;
			Globals.kWEB_Socket_Server		= p.kWEB_Socket_Server;
			Globals.kServer_API_URL			= p.kServer_API_URL;
			Globals.kGetQCContactsURLtest	= p.kGetQCContactsURLtest;
			Globals.kGetQCContactsURLprod	= p.kGetQCContactsURLprod;	
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
				
		//switch ( LOCATION_MODE.SVDP_SSL )
		/*{
			{
    			"fileName":"parameters.json",
			    "guestName":"LOCAL_HOST",
			    "mysqlUser":"root",
			    "mysqlPass":"Guacamole77.",
			    "kWebsocketServerIP":"192.168.0.106",
			   "kWEB_Socket_Server":"ws://192.168.0.106:17017",
			   "kGetQCContactsURLtest":"http://192.168.0.106:8080/SVDP/GetQuickChatContacts",
			    "kGetQCContactsURLprod_":"https://tc.svdp-help.com/SVDP/GetQuickChatContacts"
			}
	 
			case LOCAL_HOST:
				mysqlUser 				= "root";
				mysqlPass				= "Guacamole77.";
				kWebsocketServerIP		= "192.168.0.106";
				kWEB_Socket_Server		= "ws://" + kWebsocketServerIP + ":" + kWebsocketServerPort;
				kGetQCContactsURLtest	= "http://192.168.0.106:8080/SVDP/GetQuickChatContacts";
				//kGetQCContactsURLprod	= "https://tc.svdp-help.com/SVDP/GetQuickChatContacts";
				//kGetQCContactsURLprod	= "http://192.168.0.106:8080/QuickChat/GetYOIChatContacts";//"https://tc.svdp-help.com/SVDP/GetQuickChatContacts";
				break;
				
			{
			 	"fileName":"parameters.json",
		 		"guestName":"SVDP_SSL",
		 		"mysqlUser":"tomcat",
		 		"mysqlPass":"Guacamole77.",
		 		"kWebsocketServerIP":"qc.svdp-help.com",
				"kWEB_Socket_Server":"wss://qc.svdp-help.com:443",
				"kGetQCContactsURLtest":"https://tc.svdp-help.com/QuickChat/GetQuickChatContacts";
		 		"kGetQCContactsURLprod":"https://tc.svdp-help.com/SVDP/GetQuickChatContacts";
		 	}
			case SVDP_SSL:
				mysqlUser 				= "tomcat";
				mysqlPass				= "Guacamole77.";
				kWebsocketServerIP		= "qc.svdp-help.com";
				kWEB_Socket_Server		= "wss://" + kWebsocketServerIP + ":443";
				kGetQCContactsURLtest	= "https://tc.svdp-help.com/QuickChat/GetQuickChatContacts";
				kGetQCContactsURLprod	= "https://tc.svdp-help.com/SVDP/GetQuickChatContacts";
				break;
			 {
		 		"fileName":"parameters.json",
		 		"guestName":"YOY_SSL",
		 		"mysqlUser":"tomcat",
		 		"mysqlPass":"aksajdhaskjhTTTuytuyt555823732989**ghgj..",
		 		"kWebsocketServerIP":"qc.yoifirst.com",
				"kWEB_Socket_Server":"wss://qc.yoifirst.com:443",
				"kGetQCContactsURLtest":"http://localhost:8080/QuickChat/GetQuickChatContacts";
		 		"kGetQCContactsURLprod":"http://localhost:8080/QuickChat/GetYOIChatContacts";
		 	} 
			case YOY_SSL:
				mysqlUser 				= "tomcat";
				mysqlPass				= "aksajdhaskjhTTTuytuyt555823732989**ghgj..";
				kWebsocketServerIP		= "qc.yoifirst.com";
				kWEB_Socket_Server		= "wss://qc.yoifirst.com:443";
				kGetQCContactsURLtest	= "http://localhost:8080/QuickChat/GetQuickChatContacts";
				kGetQCContactsURLprod	= "http://localhost:8080/QuickChat/GetYOIChatContacts";
				break;
			default:
				break; 
		}*/
			
		File pipDir = new File(rootDirectory);
		
		if (!pipDir.exists())
		{
			pipDir.mkdir();
		}
		
		File tempDir = new File(tempDirectory);
		
		if (!tempDir.exists())
		{
			tempDir.mkdir();
		}
	}
	
	public static Parameters loadParameters() throws FileNotFoundException
	{		
		File f = new File( rootDirectory + "parameters.json" );
		
		Gson  gson 	=  new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
		
		FileReader fis = new FileReader( f );
		
		Parameters parameters = gson.fromJson( fis, Parameters.class );
		
		return parameters;
	}
	
	public static HistoryManager getHistoryManager()
	{
		//return SQLHistoryManeger.getSingleton();
		
		return QueueHistoryManager.getSingleton();
	}
}