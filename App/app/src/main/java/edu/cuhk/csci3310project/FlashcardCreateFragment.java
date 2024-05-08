package edu.cuhk.csci3310project;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class FlashcardCreateFragment extends Fragment {

    private static final String CARD_PREFS_NAME_PREFIX = "FlashCardPrefs";
    private static final String KEY_CARD_COUNT = "CardCount";
    private static final String SET_PREFS_NAME = "SetPrefs";
    private static final String KEY_SET_COUNT = "SetCount";
    private LinearLayout cardsContainer;
    private List<Flashcard> cardList;
    private int cardCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard_create, container, false);

        cardsContainer = rootView.findViewById(R.id.createCardContainer);
        cardList = new ArrayList<>();

        setupButtons(rootView);

        return rootView;
    }

    private void setupButtons(View rootView) {
        Button saveButton = rootView.findViewById(R.id.createCardSaveButton);
        Button cancelButton = rootView.findViewById(R.id.createCardCancelButton);
        ImageButton addCardButton = rootView.findViewById(R.id.createCardAddButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFlashcard();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) requireActivity();
                activity.NavigateToFragmentByFragment(new FlashcardMainFragment());
            }
        });

        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCard();
            }
        });
    }

    private void saveFlashcard() {
        // Save the flashcard
        EditText title = requireView().findViewById(R.id.createCardEditTextTitle);
        if (title.getText().toString().isEmpty()) {
            Toast toast = Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (cardList.isEmpty()) {
            Toast toast = Toast.makeText(requireContext(), "Card cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // save the set to share pref
        int setID = saveFlashcardSet();
        Log.d("FlashcardCreateFragment", "DEBUG: 01");
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + setID, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int cardCount = sharedPreferences.getInt(KEY_CARD_COUNT, 0);

        Log.d("FlashcardCreateFragment", "DEBUG: 02");

        for (int i = 0; i < cardList.size(); i++) {
            Log.d("FlashcardCreateFragment", "DEBUG saveFlashcard() frontTextID: " + Integer.toString(R.id.flashcard_create_front * 10 + i));
            EditText frontText = requireView().findViewById(R.id.flashcard_create_front * 10 + i);

            EditText backText = requireView().findViewById(R.id.flashcard_create_back * 10 + i);
            Log.d("FlashcardCreateFragment", "DEBUG: 03");
            String front = frontText.getText().toString();
            String back = backText.getText().toString();
            long datetime = System.currentTimeMillis();
            Log.d("FlashcardCreateFragment", "DEBUG: 04");

            editor.putInt("set_" + setID + "_card_" + cardCount + "_setID", setID);
            editor.putString("set_" + setID + "_card_" + cardCount + "_front", front);
            editor.putString("set_" + setID + "_card_" + cardCount + "_back", back);
            editor.putLong("set_" + setID + "_card_" + cardCount + "_datetime", datetime);
            Log.d("FlashcardCreateFragment", "DEBUG: 05");
            cardCount++;
        }

        editor.putInt(KEY_CARD_COUNT, cardCount);
        editor.apply();

        MainActivity activity = (MainActivity) requireActivity();
        activity.NavigateToFragmentByFragment(new FlashcardMainFragment());
    }

    private int saveFlashcardSet() {
        // Save the flashcard set
        EditText title = requireView().findViewById(R.id.createCardEditTextTitle);
        String titleStr = title.getText().toString();
        int cardCount = cardList.size();
        long datetime = System.currentTimeMillis();

        // save set to share pref
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int setCount = sharedPreferences.getInt(KEY_SET_COUNT, 0);
        editor.putString("set_id_" + setCount, Integer.toString(setCount));
        editor.putString("set_title_" + setCount, titleStr);
        editor.putInt("set_count_" + setCount, cardCount);
        editor.putLong("set_datetime_" + setCount, datetime);
        editor.putInt(KEY_SET_COUNT, setCount + 1);
        editor.apply();

        return setCount;
    }

    private void addCard() {
        // Add a new card
        View cardView = getLayoutInflater().inflate(R.layout.flashcard_create_card, null);
        EditText frontText = cardView.findViewById(R.id.flashcard_create_front);
        EditText backText = cardView.findViewById(R.id.flashcard_create_back);
        Button viewButton = cardView.findViewById(R.id.flashcard_create_view_photo_button);
        Button addButton = cardView.findViewById(R.id.flashcard_create_add_photo_button);

        frontText.setId(R.id.flashcard_create_front * 10 + cardCount);
        Log.d("FlashcardCreateFragment", "addCard() DEBUG: " + frontText.getId());
        backText.setId(R.id.flashcard_create_back * 10 + cardCount);
        viewButton.setId(R.id.flashcard_create_view_photo_button * 10 + cardCount);
        addButton.setId(R.id.flashcard_create_add_photo_button * 10 + cardCount);
        cardCount++;

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FlashcardCreateFragment", "Photo button clicked, id: " + v.getId());
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FlashcardCreateFragment", "Photo button clicked, id: " + v.getId());
            }
        });
        cardList.add(new Flashcard());
        cardsContainer.addView(cardView);
    }
}