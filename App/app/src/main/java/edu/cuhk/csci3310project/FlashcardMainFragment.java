package edu.cuhk.csci3310project;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FlashcardMainFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    enum SortOption {
        DATE,
        TITLE
    }

    private static final String PREFS_NAME = "SetPrefs";
    private static final String KEY_SET_COUNT = "SetCount";
    private LinearLayout setsContainer;
    private List<FlashcardSet> setList;
    private SortOption sortOption = SortOption.DATE;
    private String keyword = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard_main, container, false);

        setsContainer = rootView.findViewById(R.id.fmContainer);
        setList = new ArrayList<>();

        setupViews(rootView);
        loadSetsFromPreferences();
        displaySets();

        return rootView;
    }

    private void setupViews(View rootView) {
        // Setup the buttons
        View AddButton = rootView.findViewById(R.id.fmAddButton);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the add flashcard set dialog
                openCreateSetFragment();
            }
        });

        Spinner sortSpinner = rootView.findViewById(R.id.fmSortSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);

        EditText searchEditText = rootView.findViewById(R.id.fmSearchEditText);
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
                refreshSetsContainer();
            }
        });
    }

    private void openRandomSet() {
        // Open a random flashcard set
        if (setList.isEmpty()) {
            Toast toast = Toast.makeText(requireContext(), R.string.no_available_flashcard_set, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        FlashcardSet randomSet = setList.get((int) (Math.random() * setList.size()));
        openSet(randomSet);
    }

    private void openCreateSetFragment() {
        // Open the add flashcard set dialog
        MainActivity activity = (MainActivity) requireActivity();
        activity.NavigateToFragmentByFragment(new FlashcardCreateFragment());
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

        // Refresh the display
        refreshSetsContainer();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void displaySets() {
        // Display the flashcard sets
        if (setList.isEmpty()) {
            return;
        }

        // Filter the flashcard sets
        List<FlashcardSet> filteredSetList = new ArrayList<>();
        for (FlashcardSet set : setList) {
            if (set.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                filteredSetList.add(set);
            } else if (set.getFlashcardCount() > 0) {
                boolean found = false;
                Log.d("FlashcardMainFragment", "number of flashcards in var: " + set.getFlashcardCount());
                Log.d("FlashcardMainFragment", "number of flashcards in list: " + set.getFlashcards(requireActivity()).size());
                for (Flashcard card : set.getFlashcards(requireActivity())) {
                    if (card.getFrontText().toLowerCase().contains(keyword.toLowerCase()) || card.getBackText().toLowerCase().contains(keyword.toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    filteredSetList.add(set);
                }
            }
        }

        // Sort the flashcard sets
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
            // Create a view for each flashcard set
            createSetView(set);
        }
    }

    private void createSetView(FlashcardSet set) {
        // Create a view for a flashcard set
        Log.d("FlashcardMainFragment", "Creating view for set: " + set.getTitle() + " (" + set.getFlashcardCount() + " flashcards)" + "(" + set.getDatetime() + ")");
        View setView = getLayoutInflater().inflate(R.layout.flashcard_set, null);
        TextView titleTextView = setView.findViewById(R.id.flashcard_set_title);
        TextView countTextView = setView.findViewById(R.id.flashcard_set_count);
        TextView datetimeTextView = setView.findViewById(R.id.flashcard_set_datetime);

        String title = set.getTitle();
        String count = Integer.toString(set.getFlashcardCount());
        Date date = new Date(set.getDatetime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss");
        String formattedDate = formatter.format(date);

        titleTextView.setText(title);
        countTextView.setText(count);
        datetimeTextView.setText(formattedDate);

        setView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSet(set);
            }
        });

        setView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(set);
                return true;
            }
        });

        setsContainer.addView(setView);
    }

    private void openSet(FlashcardSet set) {
        // Open the flashcard set

        // save set to FlashCardTempPrefs
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("FlashCardSetTempPrefs", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("set_id", set.getSetID());
        editor.putString("set_title", set.getTitle());
        editor.putInt("set_count", set.getFlashcardCount());
        editor.putLong("set_datetime", set.getDatetime());
        editor.apply();

        // Open the flashcard set
        MainActivity activity = (MainActivity) requireActivity();
        activity.NavigateToFragmentByFragment(new FlashcardViewFragment());
    }

    private void showDeleteDialog(FlashcardSet set) {
        // Show a dialog to confirm deletion of the flashcard set
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.delete_flashcard_set);
        builder.setMessage(getString(R.string.are_you_sure) + "\n" + getString(R.string.delete) + " " + set.getTitle());
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteSetAndRefresh(set);
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void deleteSetAndRefresh(FlashcardSet set) {
        int setIndex = setList.indexOf(set);

        // Delete the flashcard set from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("set_id_" + setIndex);
        editor.remove("set_title_" + setIndex);
        editor.remove("set_count_" + setIndex);
        editor.remove("set_datetime_" + setIndex);
        editor.apply();

        // Delete the flashcards in the set from SharedPreferences
        String CARD_PREFS_NAME = "FlashCardPrefs" + setIndex;
        SharedPreferences sharedPreferences2 = requireActivity().getSharedPreferences(CARD_PREFS_NAME, 0);
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.clear();
        editor2.apply();

        // Delete the flashcard set and refresh the display
        setList.remove(set);


        saveSetsToPreferences();
        refreshSetsContainer();
    }

    private void loadSetsFromPreferences() {
        // Load the flashcard sets from preferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        int setCount = sharedPreferences.getInt(KEY_SET_COUNT, 0);

        for (int i = 0; i < setCount; i++) {
            String setID = sharedPreferences.getString("set_id_" + i, "");
            String title = sharedPreferences.getString("set_title_" + i, "");
            int count = sharedPreferences.getInt("set_count_" + i, 0);
            long datetime = sharedPreferences.getLong("set_datetime_" + i, 0);

            FlashcardSet set = new FlashcardSet();
            set.setSetID(setID);
            set.setTitle(title);
            set.setFlashcardCount(count);
            set.setDatetime(datetime);
            setList.add(set);
        }

        Log.d ("FlashcardMainFragment", "Loaded " + setList.size() + " flashcard sets from SharedPreferences.");
    }

    private void refreshSetsContainer() {
        // Refresh the display of flashcard sets
        setsContainer.removeAllViews();
        displaySets();
    }

    private void saveSetsToPreferences() {
        // Save the flashcard sets to preferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_SET_COUNT, setList.size());

        for (int i = 0; i < setList.size(); i++) {
            FlashcardSet set = setList.get(i);
            editor.putString("set_id_" + i, set.getSetID());
            editor.putString("set_title_" + i, set.getTitle());
            editor.putInt("set_count_" + i, set.getFlashcardCount());
            editor.putLong("set_datetime_" + i, set.getDatetime());
        }

        editor.apply();
    }
}