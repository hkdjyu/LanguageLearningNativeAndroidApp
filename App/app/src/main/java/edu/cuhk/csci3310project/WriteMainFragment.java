package edu.cuhk.csci3310project;

import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WriteMainFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    enum SortOption {
        DATE,
        TITLE
    }

    private static final String PREFS_NAME = "NotePrefs";
    private static final String KEY_NOTE_COUNT = "NoteCount";
    private LinearLayout notesContainer;
    private Button addButton;
    private Spinner sortSpinner;
    private EditText searchEditText;
    private List<Note> noteList;
    private SortOption sortOption = SortOption.DATE;
    private String keyword = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_write_main, container, false);

        noteList = new ArrayList<>();

        bindViews(rootView);
        setUpViews(rootView);
        loadNotesFromPreferences();
        displayNotes();

        return rootView;
    }

    private void bindViews(View rootView) {
        notesContainer = rootView.findViewById(R.id.writeLinearLayoutNotes);
        addButton = rootView.findViewById(R.id.writeButtonAdd);
        sortSpinner = rootView.findViewById(R.id.writeSortSpinner);
        searchEditText = rootView.findViewById(R.id.writeSearchEditText);
    }

    private void setUpViews(View rootView) {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: transition to WriteCreateFragment
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.NavigateToFragmentByFragment(new WriteCreateFragment());
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                keyword = editable.toString();
                refreshNotesContainer();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                sortOption = SortOption.DATE;
                break;
            case 1:
                sortOption = SortOption.TITLE;
                break;
        }

        refreshNotesContainer();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void displayNotes() {
        if (noteList.isEmpty()) {
            return;
        }

        // filtering
        List<Note> filteredNoteList = new ArrayList<>();
        for (Note note : noteList) {
            if (note.getTitle().toLowerCase().contains(keyword.toLowerCase()) || note.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                filteredNoteList.add(note);
            }
        }

        // sorting
        List<Note> sortedNoteList = new ArrayList<>(filteredNoteList);
        switch (sortOption) {
            case DATE:
                sortedNoteList.sort((note1, note2) -> Long.compare(note2.getDatetime(), note1.getDatetime()));
                break;

            case TITLE:
                sortedNoteList.sort((note1, note2) -> note1.getTitle().compareTo(note2.getTitle()));
                break;
        }

        for (Note note : sortedNoteList) {
            createNoteView(note);
        }
    }

    private void loadNotesFromPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        int noteCount = sharedPreferences.getInt(KEY_NOTE_COUNT, 0);

        for (int i = 0; i < noteCount; i++) {
            String title = sharedPreferences.getString("note_title_" + i, "");
            String content = sharedPreferences.getString("note_content_" + i, "");
            long datetime = sharedPreferences.getLong("note_datetime_" + i, 0);

            Note note = new Note();
            note.setTitle(title);
            note.setContent(content);
            note.setDatetime(datetime);

            noteList.add(note);
        }

        Log.d("writeFragment", "Loaded " + noteList.size() + " notes from SharedPreferences.");
    }

    private void createNoteView(final Note note) {
        View noteView = getLayoutInflater().inflate(R.layout.note_item, null);
        TextView titleTextView = noteView.findViewById(R.id.noteItemTitle);
        TextView contentTextView = noteView.findViewById(R.id.noteItemContent);
        TextView datetimeTextView = noteView.findViewById(R.id.noteItemDatetime);

        titleTextView.setText(note.getTitle());
        contentTextView.setText(note.getContent());
        Date date = new Date(note.getDatetime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss");
        String formattedDate = formatter.format(date);
        datetimeTextView.setText(formattedDate);

        noteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteCreateFragment writeFragment = new WriteCreateFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("noteIndex", noteList.indexOf(note));
                writeFragment.setArguments(bundle);

                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.NavigateToFragmentByFragment(writeFragment);
            }
        });

        noteView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(note);
                return true;
            }
        });

        notesContainer.addView(noteView);
    }

    private void showDeleteDialog(final Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete this note.");
        builder.setMessage("Are you sure you want to delete\n" + note.getTitle() + "?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNoteAndRefresh(note);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteNoteAndRefresh(Note note) {
        noteList.remove(note);
        saveNotesToPreferences();
        refreshNotesContainer();
    }

    private void refreshNotesContainer() {
        notesContainer.removeAllViews();
        displayNotes();
    }

    private void saveNotesToPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_NOTE_COUNT, noteList.size());

        for (int i = 0; i < noteList.size(); i++) {
            Note note = noteList.get(i);
            editor.putString("note_title_" + i, note.getTitle());
            editor.putString("note_content_" + i, note.getContent());
            editor.putLong("note_datetime_" + i, note.getDatetime());
        }

        editor.apply();

        Log.d("writeFragment", "Saved " + noteList.size() + " notes to SharedPreferences.");
    }
}