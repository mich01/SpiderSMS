package com.mich01.spidersms.Backend;

public class ResponseMessage
{
 String TextMessage;
 boolean Sent;
 int MessageType;


    public ResponseMessage(String textMessage, boolean sent, int Type) {
        TextMessage = textMessage;
        Sent = sent;
        MessageType = Type;
    }

    public String getTextMessage() {
        return TextMessage;
    }
    public int getMessageType() {
        return MessageType;
    }

    public void setTextMessage(String textMessage) {
        TextMessage = textMessage;
    }
public void setMessageType(int messageType) {
    MessageType = messageType;
    }

    public boolean isSent() {
        return Sent;
    }

    public void setSent(boolean sent) {
        Sent = sent;
    }
}
