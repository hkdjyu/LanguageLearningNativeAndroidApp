package edu.cuhk.csci3310project;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private static final String SETTINGS_PREFS_NAME = "settingsPrefs";
    // Preferences for AI
    private static final String AI_MESSAGE_PREFS_NAME = "MessagePrefs";
    private static final String AI_MESSAGE_COUNT_KEY = "MessageCount";
    // Preferences for FlashCard
    private static final String CARD_PREFS_NAME_PREFIX = "FlashCardPrefs";
    private static final String CARD_COUNT_KEY = "CardCount";
    private static final String SET_PREFS_NAME = "SetPrefs";
    private static final String SET_COUNT_KEY = "SetCount";
    private static final String SET_TEMP_PREFS_NAME = "FlashCardSetTempPrefs";
    // Preferences for Quiz
    private static final String QUIZ_PREFS_NAME = "QuizSetPrefs";
    private static final String QUIZ_SET_COUNT_KEY = "QuizSetCount";
    // Preference for Writing
    private static final String NOTE_PREFS_NAME = "NotePrefs";
    private static final String NOTE_COUNT_KEY = "NoteCount";

    private TextView homeArticleTextView;
    private TextView homeFlashcardSetTextView;
    private TextView homeFlashcardTextView;
    private TextView homeQuizDoneTextView;

    private EditText homeExplainEditText;

    private Button homeExplainButton;
    private Button homeAksAiButton;
    private Button homeWriteButton;
    private Button homeViewFlashcardSetButton;
    private Button homeStartQuizButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        bindViews(rootView);
        setupViews(rootView);

        return rootView;
    }

    private void bindViews(View rootView) {
        homeArticleTextView = rootView.findViewById(R.id.homeArticleTextView);
        homeFlashcardSetTextView = rootView.findViewById(R.id.homeFlashcardSetTextView);
        homeFlashcardTextView = rootView.findViewById(R.id.homeFlashcardTextView);
        homeQuizDoneTextView = rootView.findViewById(R.id.homeQuizDoneTextView);
        homeExplainEditText = rootView.findViewById(R.id.homeExplainEditText);
        homeExplainButton = rootView.findViewById(R.id.homeExplainButton);
        homeAksAiButton = rootView.findViewById(R.id.homeViewGoToAiButton);
        homeWriteButton = rootView.findViewById(R.id.homeWriteButton);
        homeViewFlashcardSetButton = rootView.findViewById(R.id.homeViewFlashcardSetButton);
        homeStartQuizButton = rootView.findViewById(R.id.homeStartQuizButton);
    }

    private void setupViews(View rootView) {
        homeExplainButton.setOnClickListener((View v) -> {
            // Translate the text in homeTranslateEditText
            translateText();
        });
        homeAksAiButton.setOnClickListener((View v) -> {
            // Go to AiFragment
            ((MainActivity) getActivity()).NavigateToFragmentByFragment(
                    new AiMainFragment()
            );
        });
        homeWriteButton.setOnClickListener((View v) -> {
            // Go to WriteFragment
            ((MainActivity) getActivity()).NavigateToFragmentByFragment(
                    new WriteCreateFragment()
            );
        });
        homeViewFlashcardSetButton.setOnClickListener((View v) -> {
            // Go to FlashcardSetFragment
            viewRandomFlashcardSet();
        });
        homeStartQuizButton.setOnClickListener((View v) -> {
            // Go to QuizFragment
            startRandomQuiz();
        });

        // Set TextViews
        String text;
        SharedPreferences prefs;

        prefs = requireActivity().getSharedPreferences(NOTE_PREFS_NAME, 0);
        text = String.valueOf(prefs.getInt(NOTE_COUNT_KEY, 0));
        homeArticleTextView.setText(text);

        prefs = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        text = String.valueOf(prefs.getInt(SET_COUNT_KEY, 0));
        homeFlashcardSetTextView.setText(text);

        int numberOfSets = prefs.getInt(SET_COUNT_KEY, 0);
        int numberOfCards = 0;
        for (int i = 0; i < numberOfSets; i++) {
            prefs = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + i, 0);
            numberOfCards += prefs.getInt(CARD_COUNT_KEY, 0);
        }
        text = String.valueOf(numberOfCards);
        homeFlashcardTextView.setText(text);

        prefs = requireActivity().getSharedPreferences(QUIZ_PREFS_NAME, 0);
        int numberOfQuizSets = prefs.getInt(QUIZ_SET_COUNT_KEY, 0);
        int numberOfQuizDone = 0;
        for (int i = 0; i < numberOfQuizSets; i++) {
            int numberOfCorrect = prefs.getInt("quiz_set_correct_count_" + i, -1);
            if (numberOfCorrect != -1) {
                numberOfQuizDone++;
            }
        }
        text = String.valueOf(numberOfQuizDone);
        homeQuizDoneTextView.setText(text);
    }

    private void translateText() {
        // Translate the text in homeTranslateEditText

        String prompt;
        SharedPreferences prefs = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0);
        String appLanguage = prefs.getString("language", "en");
        if (appLanguage.equals("en")) {
            prompt = "Explain the following word: ";
        } else {
            prompt = "解釋以下詞語： ";
        }
        String content = prompt.concat(homeExplainEditText.getText().toString());

        AiMainFragment aiMainFragment = new AiMainFragment();
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        bundle.putInt("mode", 0); // 0 for general purpose
        bundle.putBoolean("sendAutomatically", true);
        aiMainFragment.setArguments(bundle);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.NavigateToFragmentByFragment(aiMainFragment);
    }

    /*
    // read FlashCardTempPrefs shared preferences

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("FlashCardSetTempPrefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String setID = sharedPreferences.getString("set_id", "");
        currentSetIndex = Integer.parseInt(setID);
        setTitle = sharedPreferences.getString("set_title", "");
        int setCount = sharedPreferences.getInt("set_count", 0);
        long setDatetime = sharedPreferences.getLong("set_datetime", 0);
        editor.apply();
     */

    private void viewRandomFlashcardSet() {
        // Go to FlashcardSetFragment
        SharedPreferences prefs = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        int numberOfSets = prefs.getInt(SET_COUNT_KEY, 0);

        if (numberOfSets == 0) {
            Toast.makeText(requireContext(), (R.string.no_available_flashcard_set), Toast.LENGTH_SHORT).show();
            return;
        }

        int randomSetIndex = (int) (Math.random() * numberOfSets);
        String setTitle = prefs.getString("set_title_" + randomSetIndex, "");
        int setCount = prefs.getInt("set_count_" + randomSetIndex, 0);
        long setDatetime = prefs.getLong("set_datetime_" + randomSetIndex, 0);

        // Put the data into shared preferences temp
        prefs = requireActivity().getSharedPreferences(SET_TEMP_PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("set_id", String.valueOf(randomSetIndex));
        editor.putString("set_title", setTitle);
        editor.putInt("set_count", setCount);
        editor.putLong("set_datetime", setDatetime);
        editor.apply();

        // Navigate to FlashcardSetFragment
        MainActivity activity = (MainActivity) requireActivity();
        activity.NavigateToFragmentByFragment(new FlashcardViewFragment());
    }

    private void startRandomQuiz() {
        // Go to QuizFragment
        SharedPreferences prefs = requireActivity().getSharedPreferences(QUIZ_PREFS_NAME, 0);
        int numberOfQuizSets = prefs.getInt(QUIZ_SET_COUNT_KEY, 0);

        if (numberOfQuizSets == 0) {
            Toast.makeText(requireContext(), (R.string.no_available_quiz), Toast.LENGTH_SHORT).show();
            return;
        }

        int randomQuizSetIndex = (int) (Math.random() * numberOfQuizSets);
        int numberOfCorrect = prefs.getInt("quiz_set_correct_count_" + randomQuizSetIndex, -1);
        if (numberOfCorrect != -1) {
            // reset the quiz set
            int numberOfQuestions = prefs.getInt("quiz_set_question_count_" + randomQuizSetIndex, 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("quiz_set_correct_count_" + randomQuizSetIndex, -1).apply();
            for (int i = 0; i < numberOfQuestions; i++) {
                editor.putString("quiz_set_user_answer_" + randomQuizSetIndex + "_" + i, "").apply();
            }
        }

        QuizAttemptFragment fragment = new QuizAttemptFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("questionSetIndex", randomQuizSetIndex);
        fragment.setArguments(bundle);

        MainActivity activity = (MainActivity) requireActivity();
        activity.NavigateToFragmentByFragment(fragment);

    }
}