package edu.cuhk.csci3310project;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

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

    public List<Flashcard> getFlashcards(Activity activity) {
        List<Flashcard> flashcards = new ArrayList<>();
        String cardPrefsName = "FlashCardPrefs" + setID;
        SharedPreferences sharedPreferences = activity.getSharedPreferences(cardPrefsName, 0);

        for (int i = 0; i < flashcardCount; i++) {
            String front = sharedPreferences.getString("set_" + setID + "_card_" + i + "_front", "");
            String back = sharedPreferences.getString("set_" + setID + "_card_" + i + "_back", "");
            String image = sharedPreferences.getString("set_" + setID + "_card_" + i + "_image", "");
            long datetime = sharedPreferences.getLong("set_" + setID + "_card_" + i + "_datetime", 0);

            Flashcard flashcard = new Flashcard(Integer.parseInt(setID), front, back, image, datetime);
            flashcards.add(flashcard);
        }
        return flashcards;
    }
}
