package com.pojo;



public class ChatMessageCore extends SerializableGSON
{
    /**
     *
     */
    private static final long	serialVersionUID	= 2010584966902958338L;

    public static class BinaryPayload extends SerializableGSON
    {
        /**
         *
         */
        private static final long serialVersionUID = 7962845934327990402L;

        public BinaryPayload()
        {
        }

        public BinaryPayload( String name, String type )
        {
            this.name = name;
            this.type = type;
        }

        public String name;
        public String type;
    }

    public static class MessageType
    {
        public static final char kSingleUser = 'S';
        public static final char kGroupUser  = 'G';
    }

    private long                id;
    private long 				time;
    private String				reactions;
    private char			    senderType;
    private String 				senderName;
    private String 				senderID;
    private char			    receiverType;
    private String 				receiverName;
    private String 				receiverID;
    private ChatContent         content;

    public ChatMessageCore()
    {
    }

    public ChatMessageCore( ChatMessageCore cmc )
    {
        this.id 		    = cmc.id;
        this.content 		= cmc.content;
        this.senderType 	= cmc.senderType;
        this.senderName 	= new String(cmc.senderName);
        this.senderID 		= new String(cmc.senderID);
        this.receiverType 	= cmc.receiverType;
        this.receiverName	= new String(cmc.receiverName);
        this.receiverID		= new String(cmc.receiverID);
        this.time 			= cmc.time;
        this.reactions 		= new String(cmc.reactions);
    }

    public ChatMessageCore( ChatContent content,
                            char	    senderType,
                            String 		senderName,
                            String		senderID,
                            char	    receiverType,
                            String 		receiverName,
                            String		receiverID,
                            long 		time )
    {
        this.id             = time;
        this.content 		= content;
        this.senderType 	= senderType;
        this.senderName 	= senderName;
        this.senderID 		= senderID.toUpperCase();
        this.receiverType 	= receiverType;
        this.receiverName	= receiverName;
        this.receiverID		= receiverID.toUpperCase();
        this.time 			= time;
        this.reactions		= "0000,0000,0000,0000,0000,0000,0000,0000";
    }

    public void addReaction( int indx )
    {
        if ( indx >= 0 && indx < 8 )
        {
            String[] split = reactions.split(",");

            String hexValue = split[indx];

            int intValue = Integer.parseInt(hexValue,16) + 1;

            hexValue = Integer.toHexString(intValue).toUpperCase();

            split[indx] = hexValue;

            while ( hexValue.length() < 4 )
            {
                hexValue = "0" + hexValue;
            }

            split[indx] = hexValue;

            String newValue = 	split[0] + "," +
			                    split[1] + "," +
			                    split[2] + "," +
			                    split[3] + "," +
			                    split[4] + "," +
			                    split[5] + "," +
			                    split[6] + "," +
			                    split[7];

            this.reactions = newValue;
        }
    }

    public void removeReaction( int indx )
    {
        if ( indx >= 0 && indx < 8 )
        {
            String[] split = reactions.split(",");

            String hexValue = split[indx];

            int intValue = Math.max( Integer.parseInt(hexValue,16) - 1, 0 );

            hexValue = Integer.toHexString(intValue).toUpperCase();

            split[indx] = hexValue;

            while ( hexValue.length() < 4 )
            {
                hexValue = "0" + hexValue;
            }

            split[indx] = hexValue;

            String newValue = 	split[0] + "," +
                                split[1] + "," +
                                split[2] + "," +
                                split[3] + "," +
                                split[4] + "," +
                                split[5] + "," +
                                split[6] + "," +
                                split[7];

            this.reactions = newValue;
        }

    }

    public int[] getReactions()
    {
        int[] result = new int[8];

        String[] split = reactions.split(",");

        result[0] = Integer.parseInt(split[0],16);
        result[1] = Integer.parseInt(split[1],16);
        result[2] = Integer.parseInt(split[2],16);
        result[3] = Integer.parseInt(split[3],16);
        result[4] = Integer.parseInt(split[4],16);
        result[5] = Integer.parseInt(split[5],16);
        result[6] = Integer.parseInt(split[6],16);
        result[7] = Integer.parseInt(split[7],16);

        return result;
    }


    public void updateTime()
    {
        time = System.currentTimeMillis();
    }

    public ChatContent getContent()
    {
        return content;
    }

    public long getTime()
    {
        return time;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public void setSenderName(String senderName)
    {
        this.senderName = senderName;
    }

    public String getSenderID()
    {
        return senderID;
    }

    public void setSenderID(String senderID)
    {
        this.senderID = senderID;
    }

    public String getReceiverName()
    {
        return receiverName;
    }

    public void setReceiverName(String receiverName)
    {
        this.receiverName = receiverName;
    }

    public String getReceiverID()
    {
        return receiverID;
    }

    public void setReceiverID(String receiverID)
    {
        this.receiverID = receiverID;
    }

    /**
     * @return the senderType
     */
    public char getSenderType()
    {
        return senderType;
    }

    /**
     * @param senderType the senderType to set
     */
    public void setSenderType(char senderType)
    {
        this.senderType = senderType;
    }

    /**
     * @return the receiverType
     */
    public char getReceiverType()
    {
        return receiverType;
    }

    /**
     * @param receiverType the receiverType to set
     */
    public void setReceiverType(char receiverType)
    {
        this.receiverType = receiverType;
    }

    public void setContent( ChatContent content )
    {
        this.content 		= content;
    }

    public void setTime( long time)
    {
        this.time = time;
    }

    public String getMsgTxt()
    {
        return content == null ? "DELETED MESSAGE" : content.getMsgTxt();
    }

    public String toString()
    {
        return  "senderType   = " + senderType + "\r\n" +
                "senderName   = " + senderName + "\r\n" +
                "senderID     = " + senderID + "\r\n" +
                "receiverType = " + receiverType + "\r\n" +
                "receiverName = " + receiverName + "\r\n" +
                "receiverID   = " + receiverID + "\r\n";
    }

    public ChatMessageCore cloneIt()
    {
        return new ChatMessageCore( this );
    }


    public void clear()
    {
        time			= 0;
        senderName		= null;
        senderID		= null;
        receiverName	= null;
        receiverID		= null;
        reactions 		= "0000,0000,0000,0000,0000,0000,0000,0000";
    }

    public String getContentClass()
    {
        return content == null ? "deleted" : content.getChatContentClass();
    }

    public void setContentClass(String contentClass)
    {
        if ( content != null )
        {
            content.setChatContentClass( contentClass );
        }
    }

    public BinaryPayload getBinaryPayload()
    {
        return content == null ? null : content.getBinaryPayload();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getMSGuid()
    {
        return id2Hex16( id );
    }

    public String getMSGuidFromTime()
    {
        return id2Hex16( time );
    }

    public static String id2Hex16( long lID )
    {
        String hexValue = Long.toHexString( lID ).toUpperCase();

        while ( hexValue.length() < 16 )
        {
            hexValue = "0" + hexValue;
        }

        return hexValue;
    }

}
