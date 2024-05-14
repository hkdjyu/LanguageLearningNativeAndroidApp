package edu.cuhk.languee;

public class Flashcard {
    private int setID;
    private String frontText;
    private String backText;
    private String image;
    private long datetime;

    public Flashcard() {
        this.setID = 0;
        this.frontText = "";
        this.backText = "";
        this.image = "";
        this.datetime = 0;
    }

    public Flashcard(int setID, String frontText, String backText, String image, long datetime) {
        this.setID = setID;
        this.frontText = frontText;
        this.backText = backText;
        this.image = image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
}
