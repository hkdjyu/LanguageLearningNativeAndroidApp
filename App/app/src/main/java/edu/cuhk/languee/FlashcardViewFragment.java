package edu.cuhk.languee;

        import android.content.SharedPreferences;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Bundle;

        import androidx.fragment.app.Fragment;

        import android.util.Base64;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.TextView;

        import java.util.ArrayList;
        import java.util.List;

public class FlashcardViewFragment extends Fragment {

    private String setTitle;
    private List<String> frontTexts;
    private List<String> backTexts;
    private List<String> imageBase64List;
    private int currentSetIndex = 0;
    private int currentCardIndex = 0;
    private int numberOfCards = 0;
    private boolean isFront = true;
    private String currentFrontText;
    private String currentBackText;
    private TextView titleText;
    private TextView cardText;
    private TextView cardTextBack;
    private ImageView cardImage;
    private Button editButton;
    private Button prevButton;
    private Button nextButton;
    private Button flipButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard_view, container, false);

        bindViews(rootView);
        setupCards(rootView);
        setupButtons(rootView);

        return rootView;
    }

    private void bindViews(View rootView) {
        titleText = rootView.findViewById(R.id.flashcardViewTitle);
        cardText = rootView.findViewById(R.id.flashcardViewText);
        cardTextBack = rootView.findViewById(R.id.flashcardViewTextBack);
        cardImage = rootView.findViewById(R.id.flashcardViewImage);
        editButton = rootView.findViewById(R.id.flashcardViewEditButton);
        prevButton = rootView.findViewById(R.id.flashcardViewPrevButton);
        nextButton = rootView.findViewById(R.id.flashcardViewNextButton);
        flipButton = rootView.findViewById(R.id.flashcardViewFlipButton);
    }

    private void setupCards(View rootView) {
        // init array lists
        frontTexts = new ArrayList<>();
        backTexts = new ArrayList<>();
        imageBase64List = new ArrayList<>();

        // read FlashCardTempPrefs shared preferences

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("FlashCardSetTempPrefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String setID = sharedPreferences.getString("set_id", "");
        currentSetIndex = Integer.parseInt(setID);
        setTitle = sharedPreferences.getString("set_title", "");
        int setCount = sharedPreferences.getInt("set_count", 0);
        long setDatetime = sharedPreferences.getLong("set_datetime", 0);
        editor.apply();

        // get cards data
        String prefName = "FlashCardPrefs" + setID;
        sharedPreferences = requireActivity().getSharedPreferences(prefName, 0);
        numberOfCards = setCount;
        for (int i = 0; i < setCount; i++) {
            String front = sharedPreferences.getString("set_" + setID + "_card_" + i + "_front", "");
            String back = sharedPreferences.getString("set_" + setID + "_card_" + i + "_back", "");
            frontTexts.add(front);
            backTexts.add(back);
            imageBase64List.add(sharedPreferences.getString("set_" + setID + "_card_" + i + "_image", ""));
            Log.d("FlashcardViewFragment", "Loaded card " + front + " from SharedPreferences.");
        }

        // show the first card
        titleText.setText(String.format("%s %d/%d", setTitle, currentCardIndex + 1, numberOfCards));
        cardText.setText(frontTexts.get(0));
        cardTextBack.setText(backTexts.get(0));
        try{
            // https://stackoverflow.com/questions/13562429/how-many-ways-to-convert-bitmap-to-string-and-vice-versa
            byte[] encodeByte = Base64.decode(imageBase64List.get(0), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            cardImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.d("FlashcardViewFragment", "Failed to load image: " + e.getMessage());
        }


        cardText.setOnTouchListener(new OnSwipeTouchListener(requireContext()) {
            @Override
            public void onSwipeLeft() {
                nextCard();
            }

            @Override
            public void onSwipeRight() {
                prevCard();
            }

            @Override
            public void onSingleTap() {
                flipCardWithAnimation();
            }
        });

        cardTextBack.setOnTouchListener(new OnSwipeTouchListener(requireContext()) {
            @Override
            public void onSwipeLeft() {
                nextCard();
            }

            @Override
            public void onSwipeRight() {
                prevCard();
            }

            @Override
            public void onSingleTap() {
                flipCardWithAnimation();
            }
        });
    }

    private void setupButtons(View rootView) {
        // Setup the buttons
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the FlashcardMainFragment
//                MainActivity activity = (MainActivity) requireActivity();
//                activity.NavigateToFragmentByFragment(new FlashcardMainFragment());

                // Edit button, navigate to FlashcardCreateFragment
                FlashcardCreateFragment fragment = new FlashcardCreateFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("setIndex", currentSetIndex);
                fragment.setArguments(bundle);

                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.NavigateToFragmentByFragment(fragment);
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the previous flashcard
                prevCard();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the next flashcard
                nextCard();
            }
        });

        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Flip the flashcard
                flipCardWithAnimation();
            }
        });
    }

    private void prevCard() {
        // Show the previous flashcard
        if (currentCardIndex > 0) {
            currentCardIndex--;
            titleText.setText(String.format("%s %d/%d", setTitle, currentCardIndex + 1, numberOfCards));
            if (!isFront) {
                // flipCard();
                moveCard(cardTextBack, Direction.RIGHT);
            }
            else {
                // cardText.setText(frontTexts.get(currentCardIndex));
                //cardTextBack.setText(backTexts.get(currentCardIndex));
                moveCard(cardText, Direction.RIGHT);
            }
        }
    }

    private void nextCard() {
        // Show the next flashcard
        if (currentCardIndex < numberOfCards - 1) {
            currentCardIndex++;
            titleText.setText(String.format("%s %d/%d", setTitle, currentCardIndex + 1, numberOfCards));
            if (!isFront) {
                moveCard(cardTextBack, Direction.LEFT);
            }
            else {
                moveCard(cardText, Direction.LEFT);
                // cardText.setText(frontTexts.get(currentCardIndex));
                // cardTextBack.setText(backTexts.get(currentCardIndex));
            }
        }
    }

    private void flipCardWithAnimation() {
        // Flip the flashcard
        if (isFront) {
            cardText.animate()
                    .rotationY(90)
                    .scaleX(0f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                @Override
                public void run() {
                    cardText.setRotationY(0);
                    cardText.setScaleX(1f);
                    cardText.setVisibility(View.GONE);
                    cardTextBack.setVisibility(View.VISIBLE);
                    cardTextBack.setRotationY(-90);
                    cardTextBack.setScaleX(0f);
                    cardText.setText(frontTexts.get(currentCardIndex));
                    cardTextBack.setText(backTexts.get(currentCardIndex));
                    cardTextBack.animate().rotationY(0).scaleX(1f).setDuration(200)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {

                                }
                            }
                    ).start();
                }
            });
        } else {
            cardTextBack.animate()
                    .rotationY(-90)
                    .scaleX(0f)
                    .setDuration(200)
                    .withEndAction(new Runnable() {
                @Override
                public void run() {
                    cardTextBack.setRotationY(0);
                    cardTextBack.setScaleX(1f);
                    cardTextBack.setVisibility(View.GONE);
                    cardText.setVisibility(View.VISIBLE);
                    cardText.setRotationY(90);
                    cardText.setScaleX(0f);
                    cardText.setText(frontTexts.get(currentCardIndex));
                    cardTextBack.setText(backTexts.get(currentCardIndex));
                    cardText.animate().rotationY(0).scaleX(1f).setDuration(200)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {

                                }
                            }
                    ).start();
                }
            });
        }
        isFront = !isFront;
    }

    private void flipCard() {
        // Flip the flashcard
        if (isFront) {
            cardText.setVisibility(View.GONE);
            cardTextBack.setVisibility(View.VISIBLE);
            cardText.setText(frontTexts.get(currentCardIndex));
            cardTextBack.setText(backTexts.get(currentCardIndex));
        } else {
            cardTextBack.setVisibility(View.GONE);
            cardText.setVisibility(View.VISIBLE);
            cardText.setText(frontTexts.get(currentCardIndex));
            cardTextBack.setText(backTexts.get(currentCardIndex));
        }
        isFront = !isFront;
    }

    enum Direction {
        LEFT,
        RIGHT
    }

    private void moveCard(View disappearingCard, Direction direction) {
        disappearingCard.animate()
                .translationX(direction == Direction.LEFT ? -disappearingCard.getWidth() : disappearingCard.getWidth())
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        disappearingCard.setTranslationX(0);
                        disappearingCard.setVisibility(View.GONE);

                        cardText.setText(frontTexts.get(currentCardIndex));
                        cardTextBack.setText(backTexts.get(currentCardIndex));
                        if (!isFront) {
                            isFront = true;
                        }

                        cardText.setVisibility(View.VISIBLE);
                        cardText.setTranslationX(direction == Direction.LEFT ? cardText.getWidth() : -cardText.getWidth());
                        cardText.animate().translationX(0).setDuration(200).start();

                    }
                }).start();

        cardImage.animate()
                .translationX(direction == Direction.LEFT ? -cardImage.getWidth() : cardImage.getWidth())
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        cardImage.setTranslationX(0);
                        cardImage.setVisibility(View.GONE);

//                        Uri imageUri = Uri.parse(imageBase64List.get(currentCardIndex));
//                        cardImage.setImageURI(imageUri);
                        try{
                            // https://stackoverflow.com/questions/13562429/how-many-ways-to-convert-bitmap-to-string-and-vice-versa
                            byte[] encodeByte = Base64.decode(imageBase64List.get(currentCardIndex), Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                            cardImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            Log.d("FlashcardViewFragment", "Failed to load image: " + e.getMessage());
                        }

                        cardImage.setVisibility(View.VISIBLE);
                        cardImage.setTranslationX(direction == Direction.LEFT ? cardImage.getWidth() : -cardImage.getWidth());
                        cardImage.animate().translationX(0).setDuration(200).start();
                    }
                }).start();
    }
}