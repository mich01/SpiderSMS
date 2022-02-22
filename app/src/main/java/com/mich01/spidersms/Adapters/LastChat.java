package com.mich01.spidersms.Adapters;

public class LastChat
{
    private String ContactID;
    private String ContactName;
    private String LastMessage;
    private String Timestamp;
    private int Status;
    private int ProfilePicture;

    public LastChat(String contactID, String contactName, String lastMessage, String timestamp, int status, int profilePicture) {
        ContactID = contactID;
        ContactName = contactName;
        LastMessage = lastMessage;
        Timestamp = timestamp;
        Status = status;
        ProfilePicture = profilePicture;
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

    public void setLastMessage(String status) {
        LastMessage = LastMessage;
    }

    public int getProfilePicture() {
        return ProfilePicture;
    }


}
