package edu.cuhk.languee;

public class ChatMessage {
    private String sender;
    private String message;
    private String datetime;

    public ChatMessage() {
        this.sender = "";
        this.message = "";
        this.datetime = "";
    }

    public ChatMessage(String sender, String message, String datetime) {
        this.sender = sender;
        this.message = message;
        this.datetime = datetime;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getDatetime() {
        return datetime;
    }
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
