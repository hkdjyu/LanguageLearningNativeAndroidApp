package edu.cuhk.csci3310project;

public class FlashcardSet {
    private String setID;
    private String title;
    private int flashcardCount;
    private long datetime;

    public FlashcardSet() {
        this.setID = "";
        this.title = "";
        this.flashcardCount = 0;
        this.datetime = 0;
    }

    public FlashcardSet(String setID, String title, int flashcardCount, long datetime) {
        this.setID = setID;
        this.title = title;
        this.flashcardCount = flashcardCount;
        this.datetime = datetime;
    }

    public String getSetID() {
        return setID;
    }

    public void setSetID(String setID) {
        this.setID = setID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFlashcardCount() {
        return flashcardCount;
    }

    public void setFlashcardCount(int flashcardCount) {
        this.flashcardCount = flashcardCount;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
