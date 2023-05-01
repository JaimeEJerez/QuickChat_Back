package quick.chat.api;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cedarsoftware.util.io.JsonWriter;
import com.gaborcselle.persistent.PersistentQueue;
import com.globals.Globals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.managers.MesageManager;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;
import com.pojo.chatContent.HTMLMsg;
import com.tcp.JSONResponse;
import com.tcp.Semaphore;

import quick.chat.HTMLFormer3;
import quick.chat.history.HistoryManager;
import quick.chat.history.HistoryManager.BeforeToWrite;
import quick.chat.utils.Util;

/**
 * Servlet implementation class SendMessage
 */
@WebServlet("/SendWebViewMessageToContacts")
public class SendWebViewMessageToContacts extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	
	private static final MesageManager	mesageService		= MesageManager.getSingleton();
	private static final HistoryManager historyManager 		= Globals.getHistoryManager();
	private static final Semaphore		semaphore			= Semaphore.getSingleton();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendWebViewMessageToContacts() 
    {
        super();
    }

    public String loadQuickChatForm( String formFileURL, LinkedTreeMap<String, Object> paremMap ) throws Exception
    {
		if ( formFileURL != null )
		{
			URL formURL = new URL(formFileURL);
			
			HTMLFormer3 html = new HTMLFormer3( this, formURL );
							
			if ( paremMap != null )
			{
				 Iterator<String> keySet_iterator = paremMap.keySet().iterator();
				
				while ( keySet_iterator.hasNext() )
				{
					String key = keySet_iterator.next();
					Object val = paremMap.get(key);
					
					if ( val instanceof String )
					{
						html.addValue( key, (String)val );
					}
				}
			}
			
			return html.realice2String();
		}
		
		return null;
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

	    LinkedTreeMap<String, Object> paramMap1 = null;
		try
		{
			paramMap1 = Util.getParamMap( request.getInputStream(), gson, false );
		} 
		catch (IOException e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		} 
		catch (Exception e1)
		{
			gson.toJson( JSONResponse.not_success( 1701, e1.getMessage() ), osw );
			e1.printStackTrace();
			osw.flush();
			return;
		}
		
		String senderID 		= (String)paramMap1.get("senderID");
		String senderName 		= (String)paramMap1.get("senderName");
		String formFileURL 		= (String)paramMap1.get("formFile");
		ArrayList<?> contacts	= (ArrayList<?>)paramMap1.get("contacts");

		if ( senderID == null || senderName == null || formFileURL == null || contacts == null  )
		{
			gson.toJson( JSONResponse.not_success( 0, "Input Parameters Error." ), osw ); 
		}
		else
		{								    
			JSONResponse posP = null;
						
			try
			{							
				String formText = loadQuickChatForm( formFileURL, paramMap1 );
				
				if ( formText.startsWith("ERROR:") )
				{
					posP = JSONResponse.not_success( 003, "IOException:" + formText );
				}
				else
				if ( !formText.isEmpty() )
				{				
					for ( int i=0; i<contacts.size(); i++ )
					{
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String,String> contact = (LinkedTreeMap<String,String>)contacts.get( i );
						
						String receiverID 	= contact.get("userID");
						String receiverName = contact.get("userName");
						
						if ( receiverID.startsWith("G" ) )
						{
							continue;
						}
						
						HTMLMsg	htmlMessage = new HTMLMsg( 0, "Pedido de atencion.", formText );
						
						htmlMessage.setChatContentClass( "quick_chat.adapters.chat.WebViewMessage" );
						
						ChatMessageCore messageCore = new ChatMessageCore( 	htmlMessage,
																			MessageType.kSingleUser,
																			senderName,
																			senderID,
																			MessageType.kSingleUser,
																			receiverName,
																			receiverID,
																			System.currentTimeMillis() );
						
						historyManager.addMessage2History( messageCore, new BeforeToWrite() 
						{

							@Override
							public boolean action( ChatMessageCore message )
							{
								HTMLMsg textMessage = (HTMLMsg)message.getContent();
								
								String htmlTxt = textMessage.getHtmlContent();
								
								htmlTxt = htmlTxt.replace( "@messageID", String.valueOf( message.getId() ) );
								htmlTxt = htmlTxt.replace( "@senderID", message.getSenderID() );
								htmlTxt = htmlTxt.replace( "@receiverID", message.getReceiverID() );
								
								textMessage.setHtmlContent( htmlTxt );
								
								return true;
							}} );
						
						PersistentQueue<String> messagesQueue = mesageService.getMessageQueue( receiverID );
						
						String jsonStr2 = JsonWriter.objectToJson( messageCore );
						
						messagesQueue.add( jsonStr2 );
						
						semaphore.doNotify( receiverID );
					}
																													
					posP = JSONResponse.success( "Message sent successfully." );
				}
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				
				posP = JSONResponse.not_success( 003, "IOException:" + e.getMessage() );

				e.printStackTrace();
			}
			
			response.setStatus( HttpServletResponse.SC_OK );
			
			if ( posP == null )
			{
				posP = JSONResponse.not_success( 003, "Mesage not send." );
			}
			
		    gson.toJson( posP, osw );
		}
	    
	    osw.flush();		
	}

}
