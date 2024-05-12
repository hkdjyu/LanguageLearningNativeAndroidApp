package edu.cuhk.csci3310project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuizCreateFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    enum SortOption {
        DATE,
        TITLE
    }
    private static final String TAG = "QuizCreateFragment";
    private static final String QUIZ_PREFS_NAME = "QuizSetPrefs";
    private static final String QUIZ_KEY_SET_COUNT = "QuizSetCount";
    private static final String CARD_PREFS_NAME_PREFIX = "FlashCardPrefs";
    private static final String CARD_KEY_COUNT = "CardCount";
    private static final String SET_PREFS_NAME = "SetPrefs";
    private static final String SET_KEY_COUNT = "SetCount";
    private String maxFlashcardsTextPrefix = "Number of cards ";

    private LinearLayout setsContainer;
    private TextView maxFlashcardsTextView;
    private EditText titleEditText;
    private EditText maxFlashcardsEditText;
    private EditText keywordEditText;

    private List<FlashcardSet> flashcardSetList;
    private List<FlashcardSet> selectedFlashcardSetList;
    private SortOption sortOption = SortOption.DATE;
    private String keyword = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quiz_create, container, false);

        flashcardSetList = new ArrayList<>();
        selectedFlashcardSetList = new ArrayList<>();
        bindViews(rootView);
        setupViews(rootView);
        loadFlashcardSetsFromPreferences();
        updateDisplayingSetContainer();

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                sortOption = SortOption.DATE;
                break;
            case 1:
                sortOption = SortOption.TITLE;
                break;
        }
        updateDisplayingSetContainer();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Required by AdapterView.OnItemSelectedListener
    }

    private void bindViews(View rootView) {
        setsContainer = rootView.findViewById(R.id.quizCreateContainer);
        maxFlashcardsTextView = rootView.findViewById(R.id.quizCreateNumberOfQuestionsTextView);
        titleEditText = rootView.findViewById(R.id.quizCreateEditTextTitle);
        maxFlashcardsEditText = rootView.findViewById(R.id.quizCreateNumberOfQuestionsEditText);
        keywordEditText = rootView.findViewById(R.id.quizCreateSearchEditText);
    }

    private void setupViews(View rootView) {
        // Setup spinner
        Spinner sortSpinner = rootView.findViewById(R.id.quizCreateSortSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);

        // Setup Save and Cancel buttons
        rootView.findViewById(R.id.quizCreateSaveButton).setOnClickListener(v -> {
            // Save the quiz
            boolean result = saveQuizSet();
            // Go back to the previous fragment

            // TODO: Navigate to the quiz fragment
            if (result) {
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.NavigateToFragmentByFragment(new QuizMainFragment());
            }
        });

        rootView.findViewById(R.id.quizCreateCancelButton).setOnClickListener(v -> {
            // Cancel the quiz creation
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.NavigateToFragmentByFragment(new QuizMainFragment());
        });

        // Setup keywordEditText
        keywordEditText.addTextChangedListener(new TextWatcher() {
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
                keyword = s.toString();
                updateDisplayingSetContainer();
            }
        });
    }

    private void updateDisplayingSetContainer() {
        // Clear all views in setsContainer

        setsContainer.removeAllViews();

        // Add the sets to the sets container
        if (flashcardSetList.size() == 0) {
            return;
        }
        List<FlashcardSet> filteredSetList = new ArrayList<>();
        for (FlashcardSet set : flashcardSetList) {
            // if the set is already in the selectedFlashcardSetList, add it to the filteredSetList
            if (selectedFlashcardSetList.contains(set)) {
                filteredSetList.add(set);
                continue;
            }
            // Check if the set title, front text, or back text contains the keyword
            if (set.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                filteredSetList.add(set);
            } else {
                boolean found = false;
                for (Flashcard card : set.getFlashcards(requireActivity())) {
                    if (card.getFrontText().toLowerCase().contains(keyword.toLowerCase()) ||
                            card.getBackText().toLowerCase().contains(keyword.toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    filteredSetList.add(set);
                }
            }
        }

        List<FlashcardSet> sortedSetList = new ArrayList<>(filteredSetList);
        switch (sortOption) {
            case DATE:
                sortedSetList.sort((set1, set2) -> Long.compare(set2.getDatetime(), set1.getDatetime()));
                break;
            case TITLE:
                sortedSetList.sort((set1, set2) -> set1.getTitle().compareTo(set2.getTitle()));
                break;
        }

        for (FlashcardSet set : sortedSetList) {
            View cardSetView = getSetView(set);
            setsContainer.addView(cardSetView);
        }
    }

    private View getSetView(FlashcardSet cardSet) {
        // Create a view for the card set and return it
        View cardSetView = getLayoutInflater().inflate(R.layout.quiz_flashcard_set_item, null);
        TextView titleTextView = cardSetView.findViewById(R.id.quiz_flashcard_set_title);
        TextView datetimeTextView = cardSetView.findViewById(R.id.quiz_flashcard_set_datetime);
        TextView flashcardCountTextView = cardSetView.findViewById(R.id.quiz_flashcard_set_count);
        CheckBox checkBox = cardSetView.findViewById(R.id.quiz_flashcard_set_checkbox);

        String title = cardSet.getTitle();
        Date date = new Date(cardSet.getDatetime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss");
        String formattedDate = formatter.format(date);
        int flashcardCount = cardSet.getFlashcardCount();

        titleTextView.setText(title);
        datetimeTextView.setText(formattedDate);
        flashcardCountTextView.setText(String.valueOf(flashcardCount));

        if (selectedFlashcardSetList.contains(cardSet)) {
            checkBox.setChecked(true);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Add the set to the quiz
                selectedFlashcardSetList.add(cardSet);
                maxFlashcardsTextPrefix = getString(R.string.number_of_questions);
                maxFlashcardsTextView.setText(String.format("%s (max:%d)", maxFlashcardsTextPrefix, getMaximumFlashcards()));
            } else {
                // Remove the set from the quiz
                if (selectedFlashcardSetList.contains(cardSet)) {
                    selectedFlashcardSetList.remove(cardSet);
                }
            }
        });
        return cardSetView;
    }

    private boolean saveQuizSet() {
        // Save the quiz set to SharedPreferences
        if (!canSave()) {
            return false;
        }

        String quizSetSetID;
        String quizSetTitle;
        int quizSetQuestionCount;
        int quizSetCorrectCount;
        int quizSetSourceCount;
        long quizSetDatetime = System.currentTimeMillis();
        List<String> quizSetSources = new ArrayList<>();        // store the set IDs of the selected flashcard-sets
        List<String> quizSetQuestions = new ArrayList<>();      // store the front text of the flashcards
        List<String> quizSetCorrectAnswers = new ArrayList<>(); // store the back text of the flashcards
        List<String> quizSetUserAnswers = new ArrayList<>();    // store the user's answers

        // Save the quiz set
        List<Flashcard> availableFlashcards = new ArrayList<>();
        List<Flashcard> adoptedFlashcards = new ArrayList<>();
        int userSelectedFlashcardCount = Integer.parseInt(maxFlashcardsEditText.getText().toString());

        // put all flashcards from selectedFlashcardSetList to availableFlashcards
        for (FlashcardSet set : selectedFlashcardSetList) {
            availableFlashcards.addAll(set.getFlashcards(requireActivity()));
        }

        // randomly select userSelectedFlashcardCount flashcards from availableFlashcards
        while (adoptedFlashcards.size() < userSelectedFlashcardCount && availableFlashcards.size() > 0) {
            int randomIndex = (int) (Math.random() * availableFlashcards.size());
            Flashcard flashcard = availableFlashcards.get(randomIndex);
            adoptedFlashcards.add(flashcard);
            availableFlashcards.remove(randomIndex);
        }

        // set the lists for the quiz set
        for (Flashcard flashcard : adoptedFlashcards) {
            quizSetQuestions.add(flashcard.getFrontText());
            quizSetCorrectAnswers.add(flashcard.getBackText());
            quizSetUserAnswers.add("");
            if (quizSetSources.contains(String.valueOf(flashcard.getSetID()))) {
                continue;
            } else {
                quizSetSources.add(String.valueOf(flashcard.getSetID()));
            }
        }

        // Save the quiz set
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(QUIZ_PREFS_NAME, 0);
        int quizSetCount = sharedPreferences.getInt(QUIZ_KEY_SET_COUNT, 0);
        quizSetSetID = String.valueOf(quizSetCount);
        quizSetTitle = titleEditText.getText().toString();
        quizSetQuestionCount = adoptedFlashcards.size();
        quizSetCorrectCount = -1; // -1 means the quiz is not taken yet
        quizSetSourceCount = quizSetSources.size();

        // Save the quiz set to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(QUIZ_KEY_SET_COUNT, quizSetCount + 1);
        editor.putString("quiz_set_id_" + quizSetSetID, quizSetSetID);
        editor.putString("quiz_set_title_" + quizSetSetID, quizSetTitle);
        editor.putInt("quiz_set_question_count_" + quizSetSetID, quizSetQuestionCount);
        editor.putInt("quiz_set_correct_count_" + quizSetSetID, quizSetCorrectCount);
        editor.putInt("quiz_set_source_count_" + quizSetSetID, quizSetSourceCount);
        editor.putLong("quiz_set_datetime_" + quizSetSetID, quizSetDatetime);
        for (int i = 0; i < quizSetSourceCount; i++) {
            editor.putString("quiz_set_source_" + quizSetSetID + "_" + i, quizSetSources.get(i));
        }
        for (int i = 0; i < quizSetQuestionCount; i++) {
            editor.putString("quiz_set_question_" + quizSetSetID + "_" + i, quizSetQuestions.get(i));
            editor.putString("quiz_set_correct_answer_" + quizSetSetID + "_" + i, quizSetCorrectAnswers.get(i));
            editor.putString("quiz_set_user_answer_" + quizSetSetID + "_" + i, quizSetUserAnswers.get(i));
        }
        editor.apply();
        Log.d(TAG, "Quiz set saved");

        return true;
    }

    private void loadFlashcardSetsFromPreferences() {
        // Load sets from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        int setCount = sharedPreferences.getInt(SET_KEY_COUNT, 0);

        for (int i = 0; i < setCount; i++) {
            String setID = String.valueOf(i);
            String title = sharedPreferences.getString("set_title_" + setID, "");
            int flashcardCount = sharedPreferences.getInt("set_count_" + setID, 0);
            long datetime = sharedPreferences.getLong("set_datetime_" + setID, 0);
            FlashcardSet set = new FlashcardSet(setID, title, flashcardCount, datetime);
            flashcardSetList.add(set);
        }
    }

    private int getMaximumFlashcards() {
        int result = 0;
        for (FlashcardSet set : selectedFlashcardSetList) {
            result += set.getFlashcardCount();
        }
        return result;
    }

    private boolean canSave() {
        if (titleEditText.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), R.string.title_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedFlashcardSetList.size() == 0) {
            Toast.makeText(requireContext(), R.string.flashcard_set_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (maxFlashcardsEditText.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), R.string.enter_maximum_number_of_questions, Toast.LENGTH_SHORT).show();
            return false;
        }
        int userSelectedFlashcardCount = Integer.parseInt(maxFlashcardsEditText.getText().toString());
        int maximumFlashcards = getMaximumFlashcards();
        if (userSelectedFlashcardCount > maximumFlashcards) {
            Toast.makeText(requireContext(), R.string.the_maximum_number_of_flashcard_is + maximumFlashcards, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
