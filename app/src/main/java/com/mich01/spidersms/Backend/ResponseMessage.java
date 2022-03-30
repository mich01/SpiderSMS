package com.mich01.spidersms.Backend;

public class ResponseMessage
{
 String TextMessage;
 boolean Sent;
 String TimeStamp;
 int MessageStatus;

    public void setTextMessage(String textMessage) {
        TextMessage = textMessage;
    }

    public void setSent(boolean sent) {
        Sent = sent;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public void setMessageStatus(int MessageStatus) {
        MessageStatus = MessageStatus;
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
