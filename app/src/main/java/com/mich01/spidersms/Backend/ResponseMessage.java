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

    public boolean isSent() {
        return Sent;
    }

}
