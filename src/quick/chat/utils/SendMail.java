package quick.chat.utils;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class SendMail 
{ 
    public static void send(String to, String sub, String msg, final String user,final String pass) throws MessagingException
    { 
     //create an instance of Properties Class   
     Properties props = new Properties();
 
 	/* Specifies the IP address of your default mail server
 	   for e.g if you are using gmail server as an email sever
       you will pass smtp.gmail.com as value of mail.smtp host. 
       As shown here in the code. 
       Change accordingly, if your email id is not a gmail id
    */
     
     /*
     auth="Container"
    type="javax.mail.Session"
    
    
    mail.smtp.user="nobody@gmail.com"
    password="foobar"
    
      */
     
     //         mail.smtp.host=  "smtp.gmail.com"
     props.put("mail.smtp.host", "smtp.gmail.com");

     //         mail.smtp.port=  "465"
     props.put("mail.smtp.port", "465");	
     
     //         mail.smtp.auth=  "true"
     props.put("mail.smtp.auth", "true");
     
     //         mail.smtp.starttls.enable=  "true"
     props.put("mail.smtp.starttls.enable", "true");
     
     //         mail.smtp.socketFactory.class="javax.net.ssl.SSLSocketFactory"
     props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
     
     /* Pass Properties object(props) and Authenticator object   
           for authentication to Session instance 
        */

    Session session = Session.getInstance(props,new javax.mail.Authenticator()
    {
  	  protected PasswordAuthentication getPasswordAuthentication() 
  	  {
  	 	 return new PasswordAuthentication(user,pass);
  	  }
   });
    
 
 	/* Create an instance of MimeMessage, 
 	      it accept MIME types and headers 
 	   */
 
    MimeMessage message = new MimeMessage(session);
       message.setFrom(new InternetAddress(user));
       message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
       message.setSubject(sub);
       message.setText(msg, "UTF-8", "html");
       /* Transport class is used to deliver the message to the recipients */
       
       Transport.send(message);
 
  }  
}