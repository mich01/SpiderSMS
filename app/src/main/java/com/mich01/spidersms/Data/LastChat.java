package com.mich01.spidersms.Data;

public class LastChat
{
    private final String ContactID;
    private final String ContactName;
    private final String LastMessage;
    private final String Timestamp;
    private final int Status;

    public LastChat(String contactID, String contactName, String lastMessage, String timestamp, int status) {
        ContactID = contactID;
        ContactName = contactName;
        LastMessage = lastMessage;
        Timestamp = timestamp;
        Status = status;
    }

    public String getContactID() {
        return ContactID;
    }


    public String getContactName() {
        return ContactName;
    }



    public String getLastMessage() {
        return LastMessage;
    }



    public String getTimestamp() {
        return Timestamp;
    }


    public int getStatus() {
        return Status;
    }

}
