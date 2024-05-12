package edu.cuhk.csci3310project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class QuizAttemptFragment extends Fragment {

    enum Mode {
        ATTEMPT,
        REVIEW
    }

    private static final String TAG = "QuizAttemptFragment";
    private static final String QUIZ_PREFS_NAME = "QuizSetPrefs";
    private static final String QUIZ_KEY_SET_COUNT = "QuizSetCount";

    private ImageView questionImageView;
    private TextView titleTextView;
    private TextView questionWordTextView;
    private TextView answerTextView;

    private EditText answerEditText;

    private Button prevButton;
    private Button submitButton;
    private Button nextButton;

    private Mode mode = Mode.ATTEMPT;
    private int questionSetIndex = -1;
    private int currentCardIndex = 0;
    private QuizSet currentSet;

    public QuizAttemptFragment() {
        // Required empty public constructor
    }

    public static QuizAttemptFragment newInstance(int setIndex) {
        QuizAttemptFragment fragment = new QuizAttemptFragment();
        Bundle args = new Bundle();
        args.putInt("questionSetIndex", setIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionSetIndex = getArguments().getInt("questionSetIndex", -1);
            Log.d(TAG, "Question set index: " + questionSetIndex);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quiz_attempt, container, false);
        if (!loadQuestionSetFromSharedPreferences()) {
            Log.e(TAG, "Failed to load question set from shared preferences");
            return rootView;
        }

        bindViews(rootView);
        setUpViews(rootView);
        showQuestionByIndex(currentCardIndex);
        updateMode();
        updateViewBasedOnMode();


        return rootView;
    }

    private void bindViews(View rootView) {
        // Bind views here
        questionImageView = rootView.findViewById(R.id.quizAttemptImage);
        titleTextView = rootView.findViewById(R.id.quizAttemptTitle);
        questionWordTextView = rootView.findViewById(R.id.quizAttemptText);
        answerTextView = rootView.findViewById(R.id.quizAttemptCorrectAnswer);
        answerEditText = rootView.findViewById(R.id.quizAttemptEditText);
        prevButton = rootView.findViewById(R.id.quizAttemptPrevButton);
        submitButton = rootView.findViewById(R.id.quizAttemptSubmitButton);
        nextButton = rootView.findViewById(R.id.quizAttemptNextButton);
    }

    private void setUpViews(View rootView) {
        // Text
        String titleText = String.format("%s %d/%d", currentSet.getTitle(), currentCardIndex+1, currentSet.getQuestionCount());
        titleTextView.setText(titleText);

        answerTextView.setVisibility(View.INVISIBLE);

        // EditText
        answerEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Save the user answer
                currentSet.getUserAnswers().set(currentCardIndex, s.toString());
            }
        });


        // Buttons
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prev();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
    }

    private void updateMode() {
        // Update the mode
        boolean isAnswered = false;
        for (int i = 0; i < currentSet.getQuestionCount(); i++) {
            // Check if the user has answered any question
            if (!currentSet.getUserAnswers().get(i).isEmpty()) {
                isAnswered = true;
                break;
            }
        }
        mode = isAnswered ? Mode.REVIEW : Mode.ATTEMPT;
    }

    private void updateViewBasedOnMode() {
        // Update the view based on the mode
        switch (mode) {
            case ATTEMPT:
                answerTextView.setVisibility(View.INVISIBLE);
                submitButton.setVisibility(View.VISIBLE);
                answerEditText.setEnabled(true);
                break;
            case REVIEW:
                answerTextView.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.INVISIBLE);
                answerEditText.setEnabled(false);
                break;
        }
    }

    private boolean loadQuestionSetFromSharedPreferences() {
        if (questionSetIndex == -1) {
            Log.e(TAG, "Question set index is not set");
            return false;
        }

        // Load the question set from shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(QUIZ_PREFS_NAME, 0);
        int setCount = sharedPreferences.getInt(QUIZ_KEY_SET_COUNT, 0);
        if (questionSetIndex >= setCount) {
            Log.e(TAG, "Question set index is out of range");
            return false;
        }

        try{
            // Load the question set
            String setId = sharedPreferences.getString("quiz_set_id_" + questionSetIndex, "");
            String title = sharedPreferences.getString("quiz_set_title_" + questionSetIndex, "");
            int questionCount = sharedPreferences.getInt("quiz_set_question_count_" + questionSetIndex, 0);
            int correctCount = sharedPreferences.getInt("quiz_set_correct_count_" + questionSetIndex, 0);
            int sourceCount = sharedPreferences.getInt("quiz_set_source_count_" + questionSetIndex, 0);
            long datetime = sharedPreferences.getLong("quiz_set_datetime_" + questionSetIndex, 0);

            List<String> sourceIndexList = new ArrayList<>();
            for (int i = 0; i < sourceCount; i++) {
                sourceIndexList.add(sharedPreferences.getString("quiz_set_source_" + questionSetIndex + "_" + i, ""));
            }
            List<String> questionIndexList = new ArrayList<>();
            List<String> correctAnswerIndexList = new ArrayList<>();
            List<String> userAnswerIndexList = new ArrayList<>();
            for (int i = 0; i < questionCount; i++) {
                questionIndexList.add(sharedPreferences.getString("quiz_set_question_" + questionSetIndex + "_" + i, ""));
                correctAnswerIndexList.add(sharedPreferences.getString("quiz_set_correct_answer_" + questionSetIndex + "_" + i, ""));
                userAnswerIndexList.add(sharedPreferences.getString("quiz_set_user_answer_" + questionSetIndex + "_" + i, ""));
            }
            currentSet = new QuizSet(
                    setId, title, sourceIndexList, questionCount, correctCount, sourceCount, datetime,
                    questionIndexList, correctAnswerIndexList, userAnswerIndexList
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to load question set from shared preferences");
            return false;
        }

        return true;

    }

    private void saveUserAnswersToSharedPreferences() {
        // Save the user answers to shared preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(QUIZ_PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the user answers
        for (int i = 0; i < currentSet.getQuestionCount(); i++) {
            editor.putString("quiz_set_user_answer_" + questionSetIndex + "_" + i, currentSet.getUserAnswers().get(i));
        }

        // Calculate the correct count
        int correctCount = 0;
        for (int i = 0; i < currentSet.getQuestionCount(); i++) {
            if (currentSet.getUserAnswers().get(i).replace(" ", "").replace("\n", "")
                    .equalsIgnoreCase(currentSet.getCorrectAnswers().get(i).replace(" ", "").replace("\n", ""))) {
                correctCount++;
            }
        }
        editor.putInt("quiz_set_correct_count_" + questionSetIndex, correctCount);

        // update datetime
        editor.putLong("quiz_set_datetime_" + questionSetIndex, System.currentTimeMillis());

        editor.apply();
    }

    private void showQuestionByIndex(int index) {
// Show the question by index
        if (currentSet == null) {
            Log.e(TAG, "Current set is null");
            return;
        }
        if (index < 0 || index >= currentSet.getQuestionCount()) {
            Log.e(TAG, "Index is out of range");
            return;
        }

        // Show the question
        titleTextView.setText(currentSet.getTitle());
        questionWordTextView.setText(currentSet.getQuestions().get(index));
        answerEditText.setText(currentSet.getUserAnswers().get(index));

        String answerString = currentSet.getCorrectAnswers().get(index);
        if (checkAnswer(index)) {
            answerString = "Correct :)";
        } else {
            answerString = "Incorrect :(\n" + answerString;
        }
        answerTextView.setText(answerString);

    }

    private boolean checkAnswer(int index) {
        return currentSet.getUserAnswers().get(index).replace(" ", "").replace("\n", "")
                .equalsIgnoreCase(currentSet.getCorrectAnswers().get(index).replace(" ", "").replace("\n", ""));
    }

    private void prev() {
        // Go to the previous question
        if (currentCardIndex > 0) {
            currentCardIndex--;
            showQuestionByIndex(currentCardIndex);
        }
        UpdateTitle();
    }

    private void next() {
        // Go to the next question
        Log.d(TAG, "Before card index: " + currentCardIndex);
        Log.d(TAG, "After card index: " + currentCardIndex+1);
        Log.d(TAG, "is currentSet null: " + currentSet == null ? "yes" : "no");

        if (currentCardIndex < currentSet.getQuestionCount() - 1) {
            currentCardIndex++;
            showQuestionByIndex(currentCardIndex);
        }
        UpdateTitle();
    }

    private void submit() {
        // Submit the answer

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.submit);
        builder.setMessage(R.string.are_you_sure);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            // Save the user answers
            saveUserAnswersToSharedPreferences();
            // Go to the quiz main fragment
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.NavigateToFragmentByFragment(new QuizMainFragment());
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // Do nothing
        });
        builder.show();
    }

    private void UpdateTitle() {
        // Update the title
        if (currentSet == null) {
            Log.e(TAG, "Current set is null");
            return;
        }
        String titleText = String.format("%s %d/%d", currentSet.getTitle(), currentCardIndex+1, currentSet.getQuestionCount());
        titleTextView.setText(titleText);
    }
}
