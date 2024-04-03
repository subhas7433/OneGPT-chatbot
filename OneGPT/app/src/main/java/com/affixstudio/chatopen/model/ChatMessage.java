package com.affixstudio.chatopen.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {

    private String text;
    public int sentByUser;
    private String time;

    private int modelID;

    int conversationID;
    int aiExpertIndex;
    String enabledPlugins;
    int isTem; // is the message a temporary response from server

    public String getEnabledPlugins() {
        return enabledPlugins;
    }

    public int getAiExpertIndex() {
        return aiExpertIndex;
    }

    public ChatMessage(String text, int sentByUser, String time, int conversationID, int modelID, int aiExpertIndex, String enabledPlugIns,int isTem) {
        this.text = text;
        this.sentByUser = sentByUser;
        this.time = time;

        this.modelID = modelID;
        this.conversationID = conversationID;
        this.aiExpertIndex = aiExpertIndex;
        this.enabledPlugins = enabledPlugIns;
        this.isTem = isTem;

    }

    public int getIsTem() {
        return isTem;
    }

    public void setIsTem(int isTem) {
        this.isTem = isTem;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSentByUser() {
        return sentByUser;
    }



    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }



    public int getModelID() {
        return modelID;
    }



    public int getConversationID() {
        return conversationID;
    }



    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a d/M/yy", Locale.getDefault());
        return sdf.format(new Date());
    }
}
