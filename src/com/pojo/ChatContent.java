package com.pojo;


public abstract class ChatContent extends SerializableGSON
{
    private static final long	serialVersionUID	= -7740777015526812209L;

    protected static final long  hours12 = 1000L * 3600L * 12L;

    protected 	long        issueNumber         = 0;
    protected   String      chatContentClass    = "quick_chat.adapters.chat.TextMessage";

    public ChatContent()
    {
    }

    public String getMsgTxt()
    {
        return "";
    }

    public long outOfDate()
    {
        return 0;
    }

    public boolean time2Delete()
    {
        return false;
    }

    public boolean time2Show()
    {
        return true;
    }

    public void clear()
    {

    }

    public long getIssueNumber()
    {
        return issueNumber;
    }

    public void setIssueNumber(long issueNumber)
    {
        this.issueNumber = issueNumber;
    }

    public void setChatContentClass( String theChatContentClass )
    {
        this.chatContentClass = theChatContentClass;
    }

    public String getChatContentClass()
    {
        return chatContentClass;
    }


    public ChatMessageCore.BinaryPayload getBinaryPayload()
    {
        return null;
    }

}