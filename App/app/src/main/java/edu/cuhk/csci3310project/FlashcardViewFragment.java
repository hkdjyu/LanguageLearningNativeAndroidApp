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
        import android.widget.TextView;

        import org.w3c.dom.Text;

        import java.util.ArrayList;
        import java.util.List;

public class FlashcardViewFragment extends Fragment {

    private String setTitle;
    private List<String> frontTexts;
    private List<String> backTexts;
    private int currentCardIndex = 0;
    private int numberOfCards = 0;
    private boolean isFront = true;
    private String currentFrontText;
    private String currentBackText;
    private TextView titleText;
    private TextView cardText;
    private Button exitButton;
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
        exitButton = rootView.findViewById(R.id.flashcardViewExitButton);
        prevButton = rootView.findViewById(R.id.flashcardViewPrevButton);
        nextButton = rootView.findViewById(R.id.flashcardViewNextButton);
        flipButton = rootView.findViewById(R.id.flashcardViewFlipButton);
    }

    private void setupCards(View rootView) {
        // init array lists
        frontTexts = new ArrayList<>();
        backTexts = new ArrayList<>();

        // read FlashCardTempPrefs shared preferences

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("FlashCardSetTempPrefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String setID = sharedPreferences.getString("set_id", "");
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
            Log.d("FlashcardViewFragment", "Loaded card " + front + " from SharedPreferences.");
        }

        // show the first card
        titleText.setText(String.format("%s %d/%d", setTitle, currentCardIndex + 1, numberOfCards));
        cardText.setText(frontTexts.get(0));
    }

    private void setupButtons(View rootView) {
        // Setup the buttons
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) requireActivity();
                activity.NavigateToFragmentByFragment(new FlashcardMainFragment());
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
                flipCard();
            }
        });
    }

    private void prevCard() {
        // Show the previous flashcard
        if (currentCardIndex > 0) {
            currentCardIndex--;
            titleText.setText(String.format("%s %d/%d", setTitle, currentCardIndex + 1, numberOfCards));
            if (!isFront) {
                flipCard();
            }
            cardText.setText(frontTexts.get(currentCardIndex));
        }
    }

    private void nextCard() {
        // Show the next flashcard
        if (currentCardIndex < numberOfCards - 1) {
            currentCardIndex++;
            titleText.setText(String.format("%s %d/%d", setTitle, currentCardIndex + 1, numberOfCards));
            if (!isFront) {
                flipCard();
            }
            cardText.setText(frontTexts.get(currentCardIndex));
        }
    }

    private void flipCard() {
        // Flip the flashcard
        if (isFront) {
            cardText.setText(backTexts.get(currentCardIndex));
            cardText.setBackground(requireActivity().getDrawable(R.color.purple_700));
            cardText.setTextColor(requireActivity().getColor(R.color.white));
        } else {
            cardText.setText(frontTexts.get(currentCardIndex));
            cardText.setBackground(requireActivity().getDrawable(R.color.purple_100));
            cardText.setTextColor(requireActivity().getColor(R.color.black));
        }
        isFront = !isFront;
    }
}