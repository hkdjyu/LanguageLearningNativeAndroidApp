package edu.cuhk.csci3310project;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;


public class FlashcardCreateFragment extends Fragment {

    private static final String TAG = "FlashcardCreateFragment";
    private static final String CARD_PREFS_NAME_PREFIX = "FlashCardPrefs";
    private static final String KEY_CARD_COUNT = "CardCount";
    private static final String SET_PREFS_NAME = "SetPrefs";
    private static final String KEY_SET_COUNT = "SetCount";
    private LinearLayout cardsContainer;
    private LinearLayout topLinearLayout;
    private EditText titleEditText;
    private ScrollView scrollView;
    private List<Flashcard> cardList;
//    private List<String> imageBase64List;
    private int cardCount = 0;
    private boolean isKeyboardShowing = false;
    private Flashcard editingCard = null;
    private int setIndex = -1;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: " + uri);
                    // set the uriList[cardIndex] to the selected uri
                    // uriList.set(editingUriIndex, uri.toString());

                    // get the bitmap from the uri and encode it to base64 nad save it to uriList
                    // https://stackoverflow.com/questions/13562429/how-many-ways-to-convert-bitmap-to-string-and-vice-versa

                    ImageView tempImageView = new ImageView(requireContext());
                    tempImageView.setImageURI(uri);
                    tempImageView.setVisibility(View.GONE);
                    BitmapDrawable drawable = (BitmapDrawable) tempImageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encoded = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
                    editingCard.setImage(encoded);
//                    imageBase64List.set(editingCard.getSetID(), encoded);

                    // Change text on the button to "Change photo"
                    Button addButton = requireView().findViewById(R.id.flashcard_create_add_photo_button * 10 + editingCard.getSetID());
                    addButton.setText("CHANGE");

                } else {
                    Log.d("PhotoPicker", "No media selected");
                }

                editingCard = null;
            });

    public FlashcardCreateFragment() {
        // Required empty public constructor
    }

    public static FlashcardCreateFragment newInstance(int setIndex) {
        FlashcardCreateFragment fragment = new FlashcardCreateFragment();
        Bundle args = new Bundle();
        args.putInt("setIndex", setIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            setIndex = getArguments().getInt("setIndex", -1);
            Log.d(TAG, "Set Index: " + setIndex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard_create, container, false);

        cardsContainer = rootView.findViewById(R.id.createCardContainer);
        topLinearLayout = rootView.findViewById(R.id.createCardTopLayout);
        titleEditText = rootView.findViewById(R.id.createCardEditTextTitle);
        scrollView = rootView.findViewById(R.id.createCardScrollView);
        cardList = new ArrayList<>();
//        imageBase64List = new ArrayList<>();

        setupButtons(rootView);
        setupGlobalLayoutListener(rootView);

        loadSet();

        return rootView;
    }

    private void loadSet() {
        if (setIndex == -1) {
            return;
        }

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        String title = sharedPreferences.getString("set_title_" + setIndex, "");
        int totalNumberofCard = sharedPreferences.getInt("set_count_" + setIndex, 0);
        titleEditText.setText(title);

        sharedPreferences = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + setIndex, 0);

        for (int i = 0; i < totalNumberofCard; i++) {
            String front = sharedPreferences.getString("set_" + setIndex + "_card_" + i + "_front", "");
            String back = sharedPreferences.getString("set_" + setIndex + "_card_" + i + "_back", "");
            String image = sharedPreferences.getString("set_" + setIndex + "_card_" + i + "_image", "");

            Log.d(TAG, "DEBUG loading set: " + front + " " + back + " " + "image: " + image.equals("") + " " + i);
            addCard(front, back, image);
        }
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

                        Log.d(TAG, "keypadHeight = " + keypadHeight);

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

        Log.d(TAG, "DEBUG: 01");
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + setID, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (setID != -1) {
            // existing set
            editor.clear();
        }

        int cardCount = 0;

        Log.d(TAG, "DEBUG: 02");

        for (int i = 0; i < cardList.size(); i++) {
            Log.d(TAG, "DEBUG saveFlashcard() frontTextID: " + Integer.toString(R.id.flashcard_create_front * 10 + i));
            EditText frontText = requireView().findViewById(R.id.flashcard_create_front * 10 + i);
            Log.d(TAG, "frontText at " + i + ": " + frontText.getText().toString());
            EditText backText = requireView().findViewById(R.id.flashcard_create_back * 10 + i);
            Log.d(TAG, "DEBUG: 03");
            String front = frontText.getText().toString();
            String back = backText.getText().toString();
            long datetime = System.currentTimeMillis();
            Log.d(TAG, "DEBUG: 04");

            editor.putInt("set_" + setID + "_card_" + cardCount + "_setID", setID);
            editor.putString("set_" + setID + "_card_" + cardCount + "_front", front);
            editor.putString("set_" + setID + "_card_" + cardCount + "_back", back);
            editor.putLong("set_" + setID + "_card_" + cardCount + "_datetime", datetime);
//            editor.putString("set_" + setID + "_card_" + cardCount + "_image", imageBase64List.get(i));
            editor.putString("set_" + setID + "_card_" + cardCount + "_image", cardList.get(i).getImage());
            Log.d(TAG, "DEBUG: 05");

            cardCount++;
        }

        editor.putInt(KEY_CARD_COUNT, cardCount);
        editor.apply();

        cardList.clear();
//        imageBase64List.clear();

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

        int setCount;
        if (setIndex == -1) {
            // new set
            setCount = sharedPreferences.getInt(KEY_SET_COUNT, 0);
        } else{
            // overwrite existing set
            setCount = setIndex;
        }

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
        Flashcard myCard = new Flashcard();

        // Add a new card
        View cardView = getLayoutInflater().inflate(R.layout.flashcard_create_card, null);
        EditText frontText = cardView.findViewById(R.id.flashcard_create_front);
        EditText backText = cardView.findViewById(R.id.flashcard_create_back);
        Button viewButton = cardView.findViewById(R.id.flashcard_create_view_photo_button);
        Button addButton = cardView.findViewById(R.id.flashcard_create_add_photo_button);
        ImageButton removeButton = cardView.findViewById(R.id.flashcard_create_remove_card_button);

        frontText.setId(R.id.flashcard_create_front * 10 + cardCount);
        Log.d(TAG, "addCard() DEBUG: " + frontText.getId());
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

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Remove button clicked, id: " + v.getId());
                removeCard(v, views, cardIndex);
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Photo button clicked, id: " + v.getId());
                viewPhoto(v, cardIndex, myCard);
            }
        });

//        imageBase64List.add("");
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Photo button clicked, id: " + v.getId());
                // set uriList[cardIndex] to the "0", meaning it is going to be replaced
                editingCard = myCard;

                // Launch the photo picker
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        cardList.add(myCard);
        cardsContainer.addView(cardView);

        // scroll to the bottom
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void addCard(String front, String back, String image) {
        int cardIndex = cardCount;
        Flashcard myCard = new Flashcard();
        Log.d(TAG, "cardCount in addCard(...): " + cardCount);

        // Add a new card
        View cardView = getLayoutInflater().inflate(R.layout.flashcard_create_card, null);
        EditText frontText = cardView.findViewById(R.id.flashcard_create_front);
        EditText backText = cardView.findViewById(R.id.flashcard_create_back);
        Button viewButton = cardView.findViewById(R.id.flashcard_create_view_photo_button);
        Button addButton = cardView.findViewById(R.id.flashcard_create_add_photo_button);
        ImageButton removeButton = cardView.findViewById(R.id.flashcard_create_remove_card_button);

        frontText.setId(R.id.flashcard_create_front * 10 + cardCount);
        Log.d(TAG, "addCard() DEBUG: " + frontText.getId());
        backText.setId(R.id.flashcard_create_back * 10 + cardCount);
        viewButton.setId(R.id.flashcard_create_view_photo_button * 10 + cardCount);
        addButton.setId(R.id.flashcard_create_add_photo_button * 10 + cardCount);
        removeButton.setId(R.id.flashcard_create_remove_card_button * 10 + cardCount);

        frontText.setText(front);
        backText.setText(back);
        if (!image.isEmpty()) {
//            imageBase64List.add(image);
            myCard.setImage(image);
            addButton.setText("CHANGE");
        } else {
//            imageBase64List.add("");
        }

        cardCount++;

        List<View> views = new ArrayList<>();
        views.add(frontText);
        views.add(backText);
        views.add(viewButton);
        views.add(addButton);
        views.add(removeButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"cardIndex: " + cardIndex);
                removeCard(v, views, cardIndex);
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Photo button clicked, id: " + v.getId());
                Log.d(TAG,"cardIndex: " + cardIndex);
                viewPhoto(v, cardIndex, myCard);
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Photo button clicked, id: " + v.getId());
                Log.d(TAG,"cardIndex: " + cardIndex);
                // set uriList[cardIndex] to the "0", meaning it is going to be replaced
//                editingUriIndex = cardIndex;

                editingCard = myCard;

                // Launch the photo picker
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        cardList.add(myCard);
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
        updateCardListViewIDs(v, views, cardIndex);
    }

    private void updateCardListViewIDs(View removeButtonView, List<View> views, int cardIndex) {
        if (cardIndex >= cardCount -1) {
            cardCount--;
            cardList.remove(cardIndex);
//            imageBase64List.remove(cardIndex);
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
//            imageBase64List.set(i - 1, imageBase64List.get(i));
        }
        cardCount--;
        cardList.remove(cardIndex);
//        imageBase64List.remove(cardIndex);
    }

    private void viewPhoto(View v, int cardIndex, Flashcard card) {
//        Log.d(TAG, "imageBase64List.length: " + imageBase64List.size());
//        Log.d(TAG, "imageBase64List.get(cardIndex).length(): " + imageBase64List.get(cardIndex).length());
//        if (imageBase64List.get(cardIndex).isEmpty()) {
//            Toast toast = Toast.makeText(requireContext(), "No photo added", Toast.LENGTH_SHORT);
//            toast.show();
//            return;
//        }
        if (card.getImage().isEmpty()) {
            Toast toast = Toast.makeText(requireContext(), "No photo added", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // Launch the photo in Dialog
//        byte[] decodedString = android.util.Base64.decode(imageBase64List.get(cardIndex), android.util.Base64.DEFAULT);
        byte[] decodedString = android.util.Base64.decode(card.getImage(), android.util.Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        CustomDialog alert = new CustomDialog();
        alert.showImageDialog(requireActivity(), decodedByte);
    }

}