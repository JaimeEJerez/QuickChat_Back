package com.pojo.chatContent;

import com.pojo.ChatContent;
import com.pojo.ChatMessageCore;

public class AudioMsg extends ChatContent
{
    private static final long	serialVersionUID	= 4899317047213832979L;

    String 			audioName 			= null;
    int             duration            = 0;
    byte[]          audioWave           = null;

    public AudioMsg()
    {
    }

    public AudioMsg(long    issueNumber,
                    String  audioName,
                    int     duration,
                    byte[]  audioWave)
    {

        this.issueNumber        = issueNumber;
        this.audioName          = audioName;
        this.duration           = duration;
        this.audioWave          = audioWave;
    }
    @Override
    public ChatMessageCore.BinaryPayload getBinaryPayload()
    {
        return new ChatMessageCore.BinaryPayload( audioName, "AUDIO" );
    }

    public String getAudioName()
    {
        return audioName;
    }

    public void setAudioName(String audioName)
    {
        this.audioName = audioName;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }

    public byte[] getAudioWave()
    {
        return audioWave;
    }

    public void setAudioWave(byte[] audioWave)
    {
        this.audioWave = audioWave;
    }



}