package edu.cuhk.csci3310project;

public class Flashcard {
    private int setID;
    private String frontText;
    private String backText;
    private String imageUri;
    private long datetime;

    public Flashcard() {
        this.setID = 0;
        this.frontText = "";
        this.backText = "";
        this.imageUri = "";
        this.datetime = 0;
    }

    public Flashcard(int setID, String frontText, String backText, String imageUri, long datetime) {
        this.setID = setID;
        this.frontText = frontText;
        this.backText = backText;
        this.imageUri = imageUri;
        this.datetime = datetime;
    }

    public int getSetID() {
        return setID;
    }

    public void setSetID(int setID) {
        this.setID = setID;
    }

    public String getFrontText() {
        return frontText;
    }

    public void setFrontText(String frontText) {
        this.frontText = frontText;
    }

    public String getBackText() {
        return backText;
    }

    public void setBackText(String backText) {
        this.backText = backText;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
