package edu.cuhk.csci3310project;

public class Note {
    private String title;
    private String content;
    private long datetime;

    public Note() {
        this.title = "";
        this.content = "";
        this.datetime = 0;
    }

    public Note(String title, String content, long datetime) {
        this.title = title;
        this.content = content;
        // e.g. 2024-05-01 12:00:00 -> 170_000_000
        this.datetime = datetime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
