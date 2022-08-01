package com.mich01.spidersms.Backend;

public class ResponseMessage
{
 String TextMessage;
 boolean Sent;
 String TimeStamp;
 int MessageStatus;




    public String getTimeStamp() {
        return TimeStamp;
    }


    public ResponseMessage(String textMessage, boolean sent, int Status,String timeStamp) {
        TextMessage = textMessage;
        Sent = sent;
        MessageStatus = Status;
        TimeStamp = timeStamp;
    }

    public String getTextMessage() {
        return TextMessage;
    }
    public int getMessageStatus() {
        return MessageStatus;
    }

    public boolean isSent() {
        return Sent;
    }

}
