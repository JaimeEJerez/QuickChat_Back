package com.pojo.chatContent;

import com.pojo.ChatContent;

public class TextMsg extends ChatContent
{
    private static final long	serialVersionUID	= 4899310047213832977L;

    String 			text 				= null;

    public TextMsg()
    {
    }

    public TextMsg( long issueNumber, String text )
    {
        this.issueNumber  = issueNumber;
        this.text         = text;
    }

    @Override
    public String getMsgTxt()
    {
        return text;
    }

    public String getText()
    {
        return text;
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
    
}