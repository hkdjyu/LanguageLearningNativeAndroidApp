package edu.cuhk.csci3310project;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class FlashcardCreateFragment extends Fragment {

    private static final String CARD_PREFS_NAME_PREFIX = "FlashCardPrefs";
    private static final String KEY_CARD_COUNT = "CardCount";
    private static final String SET_PREFS_NAME = "SetPrefs";
    private static final String KEY_SET_COUNT = "SetCount";
    private LinearLayout cardsContainer;
    private LinearLayout topLinearLayout;
    private EditText titleEditText;
    private ScrollView scrollView;
    private List<Flashcard> cardList;
    private int cardCount = 0;
    private boolean isKeyboardShowing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard_create, container, false);

        cardsContainer = rootView.findViewById(R.id.createCardContainer);
        topLinearLayout = rootView.findViewById(R.id.createCardTopLayout);
        titleEditText = rootView.findViewById(R.id.createCardEditTextTitle);
        scrollView = rootView.findViewById(R.id.createCardScrollView);
        cardList = new ArrayList<>();

        setupButtons(rootView);

        setupGlobalLayoutListener(rootView);

        return rootView;
    }

    // For detecting keyboard visibility
    private void setupGlobalLayoutListener(View rootView){
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        rootView.getWindowVisibleDisplayFrame(r);
                        int screenHeight = rootView.getRootView().getHeight();

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        int keypadHeight = screenHeight - r.bottom;

                        Log.d("FlashcardCreateFragment", "keypadHeight = " + keypadHeight);

                        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                            // keyboard is opened
                            if (!isKeyboardShowing) {
                                isKeyboardShowing = true;
                                onKeyboardVisibilityChanged(true);
                            }
                        }
                        else {
                            // keyboard is closed
                            if (isKeyboardShowing) {
                                isKeyboardShowing = false;
                                onKeyboardVisibilityChanged(false);
                            }
                        }
                    }
                }
        );
    }

    // For performing actions when keyboard is shown or hidden
    private void onKeyboardVisibilityChanged(boolean opened) {
        if (titleEditText.hasFocus()) {
            return; // not applicable for title edit text
        }
        if (opened) {
            topLinearLayout.setVisibility(View.GONE);
        } else {
            topLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons(View rootView) {
        Button saveButton = rootView.findViewById(R.id.createCardSaveButton);
        Button cancelButton = rootView.findViewById(R.id.createCardCancelButton);
        ImageButton addCardButton = rootView.findViewById(R.id.createCardAddButton);
        ImageButton removeCardButton = rootView.findViewById(R.id.flashcard_create_remove_card_button);

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

        cardList.clear();

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
        int cardIndex = cardCount;

        // Add a new card
        View cardView = getLayoutInflater().inflate(R.layout.flashcard_create_card, null);
        EditText frontText = cardView.findViewById(R.id.flashcard_create_front);
        EditText backText = cardView.findViewById(R.id.flashcard_create_back);
        Button viewButton = cardView.findViewById(R.id.flashcard_create_view_photo_button);
        Button addButton = cardView.findViewById(R.id.flashcard_create_add_photo_button);
        ImageButton removeButton = cardView.findViewById(R.id.flashcard_create_remove_card_button);

        frontText.setId(R.id.flashcard_create_front * 10 + cardCount);
        Log.d("FlashcardCreateFragment", "addCard() DEBUG: " + frontText.getId());
        backText.setId(R.id.flashcard_create_back * 10 + cardCount);
        viewButton.setId(R.id.flashcard_create_view_photo_button * 10 + cardCount);
        addButton.setId(R.id.flashcard_create_add_photo_button * 10 + cardCount);
        removeButton.setId(R.id.flashcard_create_remove_card_button * 10 + cardCount);
        cardCount++;

        List<View> views = new ArrayList<>();
        views.add(frontText);
        views.add(backText);
        views.add(viewButton);
        views.add(addButton);
        views.add(removeButton);

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

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FlashcardCreateFragment", "Remove button clicked, id: " + v.getId());
                removeCard(v, views, cardIndex);
            }
        });

        cardList.add(new Flashcard());
        cardsContainer.addView(cardView);

        // scroll to the bottom
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void removeCard(View v, List<View> views, int cardIndex) {
        cardsContainer.removeView((View) v.getParent().getParent());
        updateCardListViewIDs(views, cardIndex);
    }

    private void updateCardListViewIDs(List<View> views, int cardIndex) {
        if (cardIndex >= cardCount -1) {
            cardCount--;
            cardList.remove(cardIndex);
            return;
        }
        int frontTextID = views.get(0).getId();
        int backTextID = views.get(1).getId();
        int viewButtonID = views.get(2).getId();
        int addButtonID = views.get(3).getId();
        int removeButtonID = views.get(4).getId();


        // move the post-cards views id to the previous one
        // e.g. if cardIndex = 1, then the view id of card 2 will be moved to card 1
        int counter = 1;
        for (int i = cardIndex + 1; i < cardCount; i++) {
            EditText frontText = requireView().findViewById(frontTextID + counter);
            EditText backText = requireView().findViewById(backTextID + counter);
            Button viewButton = requireView().findViewById(viewButtonID + counter);
            Button addButton = requireView().findViewById(addButtonID + counter);
            ImageButton removeButton = requireView().findViewById(removeButtonID + counter);

            frontText.setId(frontTextID + counter - 1);
            backText.setId(backTextID + counter - 1);
            viewButton.setId(viewButtonID + counter - 1);
            addButton.setId(addButtonID + counter - 1);
            removeButton.setId(removeButtonID + counter - 1);
            counter++;

            cardList.set(i - 1, cardList.get(i));
        }
        cardCount--;
        cardList.remove(cardIndex);


    }

}