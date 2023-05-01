package com.pojo.chatContent;


import com.pojo.ChatContent;

public class NotifMsg extends ChatContent
{
    private static final long	serialVersionUID	= 4899310047213832977L;

    String 			message 				= null;
    String 			value1 					= null;
    String 			value2 					= null;
    String 			value3 					= null;

    public NotifMsg()
    {
    }

    public NotifMsg( long issueNumber, String message, String value1, String value2, String value3 )
    {
        this.issueNumber  	= issueNumber;
        this.message        = message;
        this.value1			= value1;
        this.value2			= value2;
        this.value3			= value3;
    }

    @Override
    public String getMsgTxt()
    {
        return message;
    }

    @Override
    public void clear()
    {
    	message = value1 = value2 = value3 = null;
    }

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getValue1()
	{
		return value1;
	}

	public void setValue1(String value1)
	{
		this.value1 = value1;
	}

	public String getValue2()
	{
		return value2;
	}

	public void setValue2(String value2)
	{
		this.value2 = value2;
	}

	public String getValue3()
	{
		return value3;
	}

	public void setValue3(String value3)
	{
		this.value3 = value3;
	}
    
    
    
}