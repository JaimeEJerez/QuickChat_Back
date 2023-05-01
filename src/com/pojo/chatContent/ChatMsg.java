package com.pojo.chatContent;

import com.pojo.ChatContent;
import com.pojo.ChatMessageCore;
import com.pojo.ChatMessageCore.MessageType;
import com.pojo.SerializableGSON;

import java.io.File;

public class ChatMsg extends SerializableGSON
{
    /**
     *
     */
    private static final long	serialVersionUID	= 1939127961170770477L;

    static final long sevenDaysInMilis = 60000L * 60L * 24L * 7L;

    private ChatMessageCore msgCore		= null;
    private boolean 		incoming	= false;
    private String			filePath	= null;
    private boolean			toSend		= true ;
    private boolean			toRead		= true ;

    public ChatMsg()
    {
    }

    public ChatMsg(ChatMsg cm)
    {
        this.msgCore 		= new ChatMessageCore( cm.msgCore );
        this.incoming 		= cm.incoming;
        this.filePath 		= cm.filePath;
        this.toSend			= cm.toSend;
        this.toRead			= cm.toRead;
    }

    public ChatMsg(ChatMessageCore msgCore,
                   boolean 		incoming,
                   boolean 		toSend,
                   boolean 		toRead)
    {
        this.msgCore 		= msgCore;
        this.incoming 		= incoming;
        this.toSend			= toSend;
        this.toRead			= toRead;
    }

    public long getIssueNumber()
    {
        ChatContent chatContent = msgCore.getContent();

        return chatContent.getIssueNumber();
    }

    public String getIssueString()
    {
        if (  getIssueNumber() == 0 )
        {
            return "";
        }

        String issue = String.valueOf( getIssueNumber() );

        while ( issue.length() < 4 )
        {
            issue = "0" + issue;
        }

        String orderString = issue + "-";

        return orderString;
    }

    public String getChatDiscriminationName()
    {
        String str = isIncoming() ? getIssueString() + getSenderName()  : getIssueString() + getReceiverName();

        if ( this.getMsgCore().getReceiverType() == MessageType.kGroupUser )
        {
            str = getIssueString() + getReceiverName();
        }

        return str;
    }

    public String getChatDiscriminationCode()
    {
        String str = isIncoming() ? getIssueString() + getSenderID()  : getIssueString() + getReceiverID();

        if ( this.getMsgCore().getReceiverType() == MessageType.kGroupUser )
        {
            str = getIssueString() + getReceiverID();
        }

        return str;
    }


    public static String getSenderIDFromFileName(String fileName)
    {
        String[] c = fileName.split("_");

        return c[0];
    }

    public ChatMsg clone()
    {
        return new ChatMsg( this );
    }

    public ChatMsg(ChatContent  content,
                   char 	    senderType,
                   String 		senderName,
                   String		senderID,
                   char	        receiverType,
                   String 		receiverName,
                   String		receiverID,
                   long 		time,
                   boolean 	    incoming,
                   boolean 	    toSend,
                   boolean 	    toRead,
                   boolean      instititional)
    {
        this.msgCore 		= new ChatMessageCore( content, senderType, senderName, senderID, receiverType, receiverName, receiverID, time );
        this.incoming 		= incoming;
        this.toSend			= toSend;
        this.toRead			= toRead;
    }

    public boolean isIncoming()
    {
        return incoming;
    }

    public boolean isToSend()
    {
        return toSend;
    }

    public void setToSend(boolean toSend)
    {
        this.toSend = toSend;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getFileName()
    {
        if ( filePath == null )
        {
            return null;
        }

        int s =  filePath.lastIndexOf( File.separator );

        if ( s >= 0 && s < filePath.length() )
        {
            return filePath.substring( s + 1 );
        }

        return filePath;
    }


    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public File getFile()
    {
        if ( filePath == null )
        {
            return null;
        }

        return new File( filePath );
    }

    public void  deleteFile()
    {
        File  f = getFile();

        if ( f != null && f.exists() )
        {
            f.delete();
        }
    }

    public void setIncoming(boolean incoming)
    {
        this.incoming = incoming;
    }

    public void updateTime()
    {
        msgCore.updateTime();
    }

    public ChatContent getContent()
    {
        return msgCore.getContent();
    }

    public long getTime()
    {
        return msgCore.getTime();
    }

    public String getSenderName()
    {
        return msgCore.getSenderName();
    }

    public void setSenderName(String senderName)
    {
        msgCore.setSenderName( senderName );
    }

    public String getSenderID()
    {
        return msgCore.getSenderID();
    }

    public void setSenderID(String senderID)
    {
        msgCore.setSenderID(senderID);
    }

    public String getReceiverName()
    {
        return msgCore.getReceiverName();
    }

    public void setReceiverName(String receiverName)
    {
        msgCore.setReceiverName( receiverName );
    }

    public String getReceiverID()
    {
        return msgCore.getReceiverID();
    }

    public void setReceiverID(String setReceiverID)
    {
        msgCore.setReceiverID( setReceiverID );
    }

    public ChatMessageCore getMsgCore()
    {
        return msgCore;
    }

    public void setMsgCore(ChatMessageCore msgCore)
    {
        this.msgCore = msgCore;
    }

    public boolean isToRead()
    {
        return toRead;
    }

    public void setToRead(boolean toRead)
    {
        this.toRead = toRead;
    }

    public char getSenderType()
    {
        return msgCore.getSenderType();
    }

    public void setSenderType(char senderType)
    {
        msgCore.setSenderType(senderType);
    }

    public char getReceiverType()
    {
        return msgCore.getReceiverType();
    }

    public void setReceiverType(char receiverType)
    {
        msgCore.setReceiverType(receiverType);
    }

    public String getMsgTxt()
    {
        return msgCore.getMsgTxt();
    }

    public void clear()
    {
        if ( msgCore != null )
        {
            msgCore.clear();
            msgCore = null;
        }
        filePath	= null;
    }

    public boolean time2Delete()
    {
        return System.currentTimeMillis() > getTime() + sevenDaysInMilis;
    }

    public boolean time2Show()
    {
        return getContent().time2Show();
    }

    public long getOderID()
    {
        ChatContent content = getContent();

        return content.getIssueNumber();
    }

    public String getChatContentClass()
    {
        return  getContent().getChatContentClass();
    }

    public String getChatViewType()
    {
        return  getContent().getChatContentClass() + ( this.isIncoming() ? ".i" : ".o" );
    }

    public String getMSGuid()
    {
       return  msgCore == null ? String.valueOf(System.currentTimeMillis()) : msgCore.getMSGuid();
    }

    public String calcMSGFileName()
    {
        return  getChatDiscriminationCode() + File.separator +  msgCore.getMSGuid() + ".msg";
    }

    public String calcMSGFileNameFromTime()
    {
        return  getChatDiscriminationCode() + File.separator +  msgCore.getMSGuidFromTime() + ".msg";
    }



}

