package com.pojo.chatContent;

import com.pojo.ChatContent;
import com.pojo.ChatMessageCore;

public class StaticImageMsg extends ChatContent
{
    private static final long	serialVersionUID	= 4899310047213832978L;

    String 			text 				= null;
    String 			imageSrc 			= null;
    int 			imageWide 			= 0;
    int 			imageHeight 		= 0;
    String          smallImage          = null;

    public StaticImageMsg()
    {
    }

    public StaticImageMsg( long issueNumber,
                           String imageSrc,
                           String text,
                           int 	  imageWide,
                           int 	  imageHeight,
                           String smallImage )
    {

        this.issueNumber        = issueNumber;
        this.text               = text;
        this.imageSrc          	= imageSrc;
        this.smallImage         = smallImage;
        this.imageWide          = imageWide;
        this.imageHeight        = imageHeight;
    }

    @Override
    public ChatMessageCore.BinaryPayload getBinaryPayload()
    {
        return new ChatMessageCore.BinaryPayload( imageSrc, "STATIC_IMAGE" );
    }

    @Override
    public String getMsgTxt()
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

    public String getSmallImage()
    {
        return smallImage;
    }

    public void setSmallImage(String smallImage)
    {
        this.smallImage = smallImage;
    }

    public String getText()
    {
        return text;
    }

    public String getImageSrc() 
    {
        return imageSrc;
    }

    public String getImageName()
    {
        String[] split = imageSrc.split("/");

        String imgName = split[split.length-2] + "_" + split[split.length-1];

        return imgName;
    }

    public void setImageSrc(String imageSrc) 
    {
        this.imageSrc = imageSrc;
    }

    public int getImageWide() 
    {
        return imageWide;
    }

    public void setImageWide(int imageWide) 
    {
        this.imageWide = imageWide;
    }

    public int getImageHeight() 
    {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) 
    {
        this.imageHeight = imageHeight;
    }
}