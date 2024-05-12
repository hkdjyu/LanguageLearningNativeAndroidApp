package edu.cuhk.csci3310project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class WriteCreateFragment extends Fragment {

    enum EditMode {
        CREATE,
        EDIT
    }

    private static final String PREFS_NAME = "NotePrefs";
    private static final String KEY_NOTE_COUNT = "NoteCount";
    private EditMode editMode = EditMode.CREATE;
    private int noteIndex = -1;
    private Note currentNote;

    public WriteCreateFragment() {
        // Required empty public constructor
    }

    public static WriteCreateFragment newInstance(int noteIndex) {
        WriteCreateFragment fragment = new WriteCreateFragment();
        Bundle args = new Bundle();
        args.putInt("noteIndex", noteIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteIndex = getArguments().getInt("noteIndex", -1);
            Log.d("WriteCreateFragment", "Note Index: " + noteIndex);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_write_create, container, false);

        Button saveButton = rootView.findViewById(R.id.writeButtonSave);
        Button checkButton = rootView.findViewById(R.id.writeButtonCheck);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
                // Navigate back to WriteMainFragment
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new WriteMainFragment()).commit();
            }
        });
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndNavigateToAiFragment();
            }
        });

        if (noteIndex == -1) {
            currentNote = new Note();
        }
        else {
            editMode = EditMode.EDIT;
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
            String title = sharedPreferences.getString("note_title_" + noteIndex, "");
            String content = sharedPreferences.getString("note_content_" + noteIndex, "");
            long datetime = sharedPreferences.getLong("note_datetime_" + noteIndex, 0);

            currentNote = new Note();
            currentNote.setTitle(title);
            currentNote.setContent(content);
            currentNote.setDatetime(datetime);

            EditText titleEditText = rootView.findViewById(R.id.writeEditTextTitle);
            EditText contentEditText = rootView.findViewById(R.id.writeEditTextContent);
            titleEditText.setText(title);
            contentEditText.setText(content);

            Log.d("WriteCreateFragment", "Loaded Note: " + title + " - " + content);
        }
        return rootView;
    }

    private void saveAndNavigateToAiFragment() {
        EditText contentEditText = requireView().findViewById(R.id.writeEditTextContent);
        String content = contentEditText.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), R.string.please_enter_a_content, Toast.LENGTH_SHORT).show();
            return;
        }
        saveNote();

        AiMainFragment aiMainFragment = new AiMainFragment();
        Bundle bundle = new Bundle();
        bundle.putString("content", content);
        aiMainFragment.setArguments(bundle);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.NavigateToFragmentByFragment(aiMainFragment);
    }

    private void saveNote() {
        EditText titleEditText = requireView().findViewById(R.id.writeEditTextTitle);
        EditText contentEditText = requireView().findViewById(R.id.writeEditTextContent);

        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        long datetime = System.currentTimeMillis();

        if (title.isEmpty() && content.isEmpty()) {
            return;
        }

        if (title.isEmpty()) {
            title = "Untitled";
        }
        if (content.isEmpty()) {
            content = "No content";
        }

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setDatetime(datetime);

        currentNote = note;
        saveNotesToPreferences(editMode);

        clearInputFields();

        Log.d("WriteCreateFragment", "Saved Note: " + title + " - " + content);
    }

    private void clearInputFields() {
        EditText titleEditText = requireView().findViewById(R.id.writeEditTextTitle);
        EditText contentEditText = requireView().findViewById(R.id.writeEditTextContent);

        titleEditText.getText().clear();
        contentEditText.getText().clear();
    }

    private void saveNotesToPreferences(EditMode mode) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (mode == EditMode.CREATE) {
            int noteCount = sharedPreferences.getInt(KEY_NOTE_COUNT, 0);
            editor.putInt(KEY_NOTE_COUNT, noteCount + 1);

            editor.putString("note_title_" + noteCount, currentNote.getTitle());
            editor.putString("note_content_" + noteCount, currentNote.getContent());
            editor.putLong("note_datetime_" + noteCount, currentNote.getDatetime());

            editor.apply();
        }
        else if (mode == EditMode.EDIT) {
            editor.putString("note_title_" + noteIndex, currentNote.getTitle());
            editor.putString("note_content_" + noteIndex, currentNote.getContent());
            editor.putLong("note_datetime_" + noteIndex, currentNote.getDatetime());
            editor.apply();
        }
    }
}