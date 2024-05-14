package edu.cuhk.languee;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuizMainFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    enum SortOption {
        DATE,
        TITLE
    }

    private static final String TAG = "QuizMainFragment";
    private static final String PREFS_NAME = "QuizSetPrefs";
    private static final String KEY_SET_COUNT = "QuizSetCount";
    private LinearLayout setsContainer;
    private List<QuizSet> setList;
    private QuizMainFragment.SortOption sortOption = QuizMainFragment.SortOption.DATE;
    private String keyword = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quiz_main, container, false);

        setsContainer = rootView.findViewById(R.id.quizMainContainer);
        setList = new ArrayList<>();

        setupViews(rootView);
        loadSetsFromPreferencesToSetList();
        updateDisplayingSetContainer();

        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                sortOption = QuizMainFragment.SortOption.DATE;
                break;
            case 1:
                sortOption = QuizMainFragment.SortOption.TITLE;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // required by the interface
    }

    private void setupViews(View rootView) {
        // Setup the buttons
        View AddButton = rootView.findViewById(R.id.quizMainAddButton);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the add flashcard set dialog
                navigateToQuizCreateFragment();
            }
        });

        Spinner sortSpinner = rootView.findViewById(R.id.quizMainSortSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);

        EditText searchEditText = rootView.findViewById(R.id.quizMainSearchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
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
        // Remove all the child views in the sets container
        setsContainer.removeAllViews();

        // Add the sets to the sets container
        if (setList.size() == 0) {
            return;
        }

        List<QuizSet> filteredSetList = new ArrayList<>();
        for (QuizSet set : setList) {
            // Check if the set title, sources or questions contain the keyword
            if (set.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                filteredSetList.add(set);
            } else if (set.getSources().toString().toLowerCase().contains(keyword.toLowerCase())) {
                filteredSetList.add(set);
            } else if (set.getQuestions().toString().toLowerCase().contains(keyword.toLowerCase())) {
                filteredSetList.add(set);
            }
        }

        List<QuizSet> sortedSetList = new ArrayList<>(filteredSetList);
        switch (sortOption) {
            case DATE:
                sortedSetList.sort((set1, set2) -> Long.compare(set2.getDatetime(), set1.getDatetime()));
                break;
            case TITLE:
                sortedSetList.sort((set1, set2) -> set1.getTitle().compareTo(set2.getTitle()));
                break;
        }

        for (QuizSet set : sortedSetList) {
            View setView = getSetView(set);
            setsContainer.addView(setView);
        }
    }

    private View getSetView(QuizSet quizSet) {
        View setView = getLayoutInflater().inflate(R.layout.quiz_set, null);
        TextView titleTextView = setView.findViewById(R.id.quiz_set_title);
        TextView markTextView = setView.findViewById(R.id.quiz_set_mark);
        TextView datetimeTextView = setView.findViewById(R.id.quiz_set_datetime);

        String title = quizSet.getTitle();
        int correctCount = quizSet.getCorrectCount();
        int questionCount = quizSet.getQuestionCount();
        String mark;
        if (correctCount == -1){
            mark = "-/" + Integer.toString(questionCount);
        } else {
            mark = Integer.toString(correctCount) + "/" + Integer.toString(questionCount);
        }

        Date date = new Date(quizSet.getDatetime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss");
        String formattedDate = formatter.format(date);

        titleTextView.setText(title);
        markTextView.setText(mark);
        datetimeTextView.setText(formattedDate);

        setView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the quiz set fragment
                NavigateToQuizAttemptFragment(quizSet);
            }
        });

        setView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Open the edit set dialog
                showDeleteSetDialog(quizSet);
                return true;
            }
        });

        return setView;
    }

    private void NavigateToQuizAttemptFragment(QuizSet set) {
        // TODO: Implement this method
        // Navigate to the quiz attempt fragment

        QuizAttemptFragment fragment = new QuizAttemptFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("questionSetIndex", setList.indexOf(set));
        fragment.setArguments(bundle);

        MainActivity activity = (MainActivity) requireActivity();
        activity.NavigateToFragmentByFragment(fragment);
    }

    private void showDeleteSetDialog(QuizSet set) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.delete_quiz);
        builder.setMessage(getString(R.string.are_you_sure) + "\n" + getString(R.string.delete) + " " + set.getTitle());
        builder.setPositiveButton(R.string.delete, (dialog, which) -> {
            // Delete the set
            setList.remove(set);
            saveSetListToPreferences();
            updateDisplayingSetContainer();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            // Do nothing
        });
        builder.show();
    }


    private void loadSetsFromPreferencesToSetList() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        int setCount = prefs.getInt(KEY_SET_COUNT, 0); // Get the number of created sets

        // Load the sets from the preferences
        for (int i = 0; i < setCount; i++) {
//            String setID = prefs.getString("set_" + i + "_setID", "");
//            String title = prefs.getString("set_" + i + "_title", "");
//            int questionCount = prefs.getInt("set_" + i + "_questionCount", 0);
//            int correctCount = prefs.getInt("set_" + i + "_correctCount", 0);
//            int sourceCount = prefs.getInt("set_" + i + "_sourceCount", 0);
//            long datetime = prefs.getLong("set_" + i + "_datetime", 0);
            String setID = prefs.getString("quiz_set_id_" + i, "");
            String title = prefs.getString("quiz_set_title_" + i, "");
            int questionCount = prefs.getInt("quiz_set_question_count_" + i, 0);
            int correctCount = prefs.getInt("quiz_set_correct_count_" + i, 0);
            int sourceCount = prefs.getInt("quiz_set_source_count_" + i, 0);
            long datetime = prefs.getLong("quiz_set_datetime_" + i, 0);

            List<String> sources = new ArrayList<>();
            for (int j = 0; j < sourceCount; j++) {
                String source = prefs.getString("quiz_set_source_" + i + "_" + j, "");
                sources.add(source);
            }

            List<String> questions = new ArrayList<>();
            List<String> answers = new ArrayList<>();
            List<String> userAnswers = new ArrayList<>();
            for (int j = 0; j < questionCount; j++) {
                String question = prefs.getString("quiz_set_question_" + i + "_" + j, "");
                String answer = prefs.getString("quiz_set_correct_answer_" + i + "_" + j, "");
                String userAnswer = prefs.getString("quiz_set_user_answer_" + i + "_" + j, "");
                questions.add(question);
                answers.add(answer);
                userAnswers.add(userAnswer);
            }

            QuizSet set = new QuizSet(
                    setID, title, sources,
                    questionCount, correctCount, sourceCount, datetime,
                    questions, answers, userAnswers);
            setList.add(set);

            Log.d(TAG, "Loaded set: " + title);
        }
    }

    private void saveSetListToPreferences() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        // Save the number of created sets
        editor.putInt(KEY_SET_COUNT, setList.size());

        // Save the sets to the preferences
        for (int i = 0; i < setList.size(); i++) {
            QuizSet set = setList.get(i);
            editor.putString("set_" + i + "_setID", set.getSetID());
            editor.putString("set_" + i + "_title", set.getTitle());
            editor.putInt("set_" + i + "_questionCount", set.getQuestionCount());
            editor.putInt("set_" + i + "_correctCount", set.getCorrectCount());
            editor.putInt("set_" + i + "_sourceCount", set.getSourceCount());
            editor.putLong("set_" + i + "_datetime", set.getDatetime());

            List<String> sources = set.getSources();
            for (int j = 0; j < sources.size(); j++) {
                editor.putString("set_" + i + "_source_" + j, sources.get(j));
            }

            List<String> questions = set.getQuestions();
            List<String> answers = set.getCorrectAnswers();
            for (int j = 0; j < questions.size(); j++) {
                editor.putString("set_" + i + "_question_" + j, questions.get(j));
                editor.putString("set_" + i + "_answer_" + j, answers.get(j));
                editor.putString("set_" + i + "_userAnswer_" + j, set.getUserAnswers().get(j));
            }

            Log.d(TAG, "Saved set: " + set.getTitle());
        }

        editor.apply();
    }

    private void navigateToQuizCreateFragment() {
        // Open the add flashcard set dialog
        MainActivity activity = (MainActivity) requireActivity();
        activity.NavigateToFragmentByFragment(new QuizCreateFragment());
    }
}
