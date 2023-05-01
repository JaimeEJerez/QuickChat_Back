package com.pojo.chatContent;

public class HTMLMsg  extends TextMsg 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9072541822731990040L;

	String htmlContent = null;

    public HTMLMsg( long issueNumber, String text, String htmlContent )
    {
    	super( issueNumber, text );
    	
    	this.htmlContent = htmlContent;
    }

	public String getHtmlContent()
	{
		return htmlContent;
	}

	public void setHtmlContent(String htmlContent)
	{
		this.htmlContent = htmlContent;
	}
	
}
