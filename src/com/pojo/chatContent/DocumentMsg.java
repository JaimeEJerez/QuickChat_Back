package com.pojo.chatContent;

import com.pojo.ChatContent;
import com.pojo.ChatMessageCore;

public class DocumentMsg extends ChatContent
{
    private static final long serialVersionUID = 7107717888302614984L;
    
	String 			text 				= null;
    String 			documentSrc 		= null;

    public DocumentMsg()
    {
    }

    public DocumentMsg(	long 		issueNumber,
    					String 		documentSrc,
    					String 		text )
    {

        this.issueNumber        = issueNumber;
        this.text               = text;
        this.documentSrc       	= documentSrc;
    }

    @Override
    public ChatMessageCore.BinaryPayload getBinaryPayload()
    {
        return new ChatMessageCore.BinaryPayload( documentSrc, "DOCUMENT" );
    }

    @Override
    public String getMsgTxt()
    {
        return text;
    }

    public String getDocumentSrc()
    {
        return documentSrc;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public void clear()
    {
        text = null;
    }

    public String getText()
    {
        return text;
    }

}