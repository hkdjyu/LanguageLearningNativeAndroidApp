package edu.cuhk.csci3310project;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "SettingsFragment";
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




    private Spinner languageSpinner;
    private EditText geminiAPIKeyEditText;
    private Button importDefaultDataButton;
    private Button clearAllDataButton;
    private Switch developerModeSwitch;
    private Button saveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        bindViews(rootView);
        setupViews(rootView);

        return rootView;
    }

    private void bindViews(View rootView) {
        languageSpinner = rootView.findViewById(R.id.settingLanguageSpinner);
        geminiAPIKeyEditText = rootView.findViewById(R.id.settingGoogleGeminiApiKeyEditText);
        importDefaultDataButton = rootView.findViewById(R.id.settingImportDefaultDataButton);
        clearAllDataButton = rootView.findViewById(R.id.settingClearAllDataButton);
        developerModeSwitch = rootView.findViewById(R.id.settingDeveloperModeSwitch);
        saveButton = rootView.findViewById(R.id.settingSaveButton);
    }

    private void setupViews(View rootView) {
        Spinner sortSpinner = rootView.findViewById(R.id.settingLanguageSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.support_language,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);

        // Load the selected language from SharedPreferences
        String language = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0)
                .getString("language", AppLanguage.ENGLISH.getCode());
        AppLanguage appLanguage = AppLanguage.fromCode(language);
        if (appLanguage != null) {
            sortSpinner.setSelection(appLanguage.ordinal());
        }

        // Load the saved Google Gemini API key from SharedPreferences
        String geminiAPIKey = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0)
                .getString("geminiAPIKey", "");
        geminiAPIKeyEditText.setText(geminiAPIKey);

        // import default data
        importDefaultDataButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(getString(R.string.import_default_data));
            builder.setMessage(R.string.are_you_sure);
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    importDefaultData();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        });

        // clear all data
        clearAllDataButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(getString(R.string.clear_all_data));
            builder.setMessage(R.string.are_you_sure);
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearAllData();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        });



        // Load the saved developer mode from SharedPreferences
        boolean developerMode = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0)
                .getBoolean("developerMode", false);
        developerModeSwitch.setChecked(developerMode);

        developerModeSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            // Save the developer mode to SharedPreferences
            requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0)
                    .edit()
                    .putBoolean("developerMode", isChecked)
                    .apply();
        });

        saveButton.setOnClickListener(view -> {
            // Save the Google Gemini API key to SharedPreferences
            requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0)
                    .edit()
                    .putString("geminiAPIKey", geminiAPIKeyEditText.getText().toString())
                    .apply();
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.UpdateLanguage();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        AppLanguage language = AppLanguage.fromIndex(i);
        if (language != null) {
            // Save the selected language to SharedPreferences
            requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0)
                    .edit()
                    .putString("language", language.getCode())
                    .apply();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void clearAllData() {
        // Get flashcard set count
        SharedPreferences prefs = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        int setCount = prefs.getInt(SET_COUNT_KEY, 0);

        // Clear all data
        prefs = requireActivity().getSharedPreferences(AI_MESSAGE_PREFS_NAME, 0);
        prefs.edit().clear().apply();
        prefs = requireActivity().getSharedPreferences(QUIZ_PREFS_NAME, 0);
        prefs.edit().clear().apply();
        prefs = requireActivity().getSharedPreferences(NOTE_PREFS_NAME, 0);
        prefs.edit().clear().apply();
        prefs = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        prefs.edit().clear().apply();
        prefs = requireActivity().getSharedPreferences(SET_TEMP_PREFS_NAME, 0);
        prefs.edit().clear().apply();
        for (int i = 0; i < setCount; i++) {
            prefs = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + i, 0);
            prefs.edit().clear().apply();
        }
        prefs = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0);
        prefs.edit().clear().apply();
    }
    private void importDefaultData() {
        // Import default data

        // setting data
        SharedPreferences prefs = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0);
        prefs.edit().putString("language", "en").apply();
        prefs.edit().putString("geminiAPIKey", "").apply();
        prefs.edit().putBoolean("developerMode", false).apply();

        // quiz data
        prefs = requireActivity().getSharedPreferences(QUIZ_PREFS_NAME, 0);
        prefs.edit().putLong("quiz_set_datetime_1", 1715502221169L).apply();
        prefs.edit().putLong("quiz_set_datetime_0", 1715502208611L).apply();
        prefs.edit().putString("quiz_set_correct_answer_0_4", "Katze").apply();
        prefs.edit().putString("quiz_set_correct_answer_1_3", "Vogel").apply();
        prefs.edit().putInt("QuizSetCount", 2).apply();
        prefs.edit().putString("quiz_set_question_0_0", "Fish").apply();
        prefs.edit().putString("quiz_set_question_0_1", "Motorcycle").apply();
        prefs.edit().putString("quiz_set_question_1_0", "Fish").apply();
        prefs.edit().putString("quiz_set_question_0_2", "Bird").apply();
        prefs.edit().putString("quiz_set_question_1_1", "Cat").apply();
        prefs.edit().putString("quiz_set_source_0_0", "0").apply();
        prefs.edit().putString("quiz_set_question_0_3", "Train").apply();
        prefs.edit().putString("quiz_set_question_1_2", "Dog").apply();
        prefs.edit().putString("quiz_set_source_0_1", "1").apply();
        prefs.edit().putString("quiz_set_question_0_4", "Cat").apply();
        prefs.edit().putString("quiz_set_source_1_0", "0").apply();
        prefs.edit().putString("quiz_set_question_1_3", "Bird").apply();
        prefs.edit().putString("quiz_set_title_0", "Animal+Trans").apply();
        prefs.edit().putString("quiz_set_title_1", "Fish").apply();
        prefs.edit().putString("quiz_set_question_0_0", "Animals").apply();
        prefs.edit().putString("quiz_set_id_0", "0").apply();
        prefs.edit().putString("quiz_set_id_1", "1").apply();
        prefs.edit().putInt("quiz_set_question_count_1", 4).apply();
        prefs.edit().putInt("quiz_set_question_count_0", 5).apply();
        prefs.edit().putString("quiz_set_user_answer_0_2", "").apply();
        prefs.edit().putString("quiz_set_user_answer_1_1", "").apply();
        prefs.edit().putString("quiz_set_user_answer_0_1", "").apply();
        prefs.edit().putString("quiz_set_user_answer_1_0", "").apply();
        prefs.edit().putString("quiz_set_user_answer_0_0", "").apply();
        prefs.edit().putInt("quiz_set_source_count_0", 2).apply();
        prefs.edit().putString("quiz_set_user_answer_0_4", "").apply();
        prefs.edit().putString("quiz_set_user_answer_1_3", "").apply();
        prefs.edit().putString("quiz_set_user_answer_0_3", "").apply();
        prefs.edit().putString("quiz_set_user_answer_1_2", "").apply();
        prefs.edit().putInt("quiz_set_source_count_1", 1).apply();
        prefs.edit().putString("quiz_set_correct_answer_0_1", "Motorrad").apply();
        prefs.edit().putInt("quiz_set_correct_count_1", -1).apply();
        prefs.edit().putString("quiz_set_correct_answer_1_0", "Fisch").apply();
        prefs.edit().putInt("quiz_set_correct_count_0", -1).apply();
        prefs.edit().putString("quiz_set_correct_answer_0_0", "Fisch").apply();
        prefs.edit().putString("quiz_set_correct_answer_0_3", "Zug").apply();
        prefs.edit().putString("quiz_set_correct_answer_1_2", "Hund").apply();
        prefs.edit().putString("quiz_set_correct_answer_0_2", "Vogel").apply();
        prefs.edit().putString("quiz_set_correct_answer_1_1", "Katze").apply();

        // Note Pref
        prefs = requireActivity().getSharedPreferences(NOTE_PREFS_NAME, 0);
        prefs.edit().putInt("NoteCount", 3).apply();
        prefs.edit().putLong("note_datetime_1", 1715501492257L).apply();
        prefs.edit().putString("note_title_2", "Cooking").apply();
        prefs.edit().putLong("note_datetime_2", 1715501609743L).apply();
        prefs.edit().putString("note_title_0", "One Day in CUHK").apply();
        prefs.edit().putLong("note_datetime_0", 1715501343125L).apply();
        prefs.edit().putString("note_title_1", "Hiking").apply();
        prefs.edit().putString("note_content_1", "Last Sunday I went to Lantau Island and reached the top of the Sunset Peak. Sunset Peak is different to other hills, it has an extraordinary wide view. We can look at the airport far away and enjoy the wind from the shorelines.").apply();
        prefs.edit().putString("note_content_2", "Today is my worst day. I almost burnt my house because I forgot to add water to my bowl. The bowl now is completely not usable. Thankfully no one is injured but now, I need to find a new one .").apply();
        prefs.edit().putString("note_content_0", "I am studying computer science in CUHK. Luckily I do not have classes in the morning this semester. I can sleep until brunch time. I usually wake up at around 9:30am and then make a brunch for myself. At around 1pm, I go to classes. In the evening, I often eat outside and the food price in campus is totally not expensive at all. After I finish my dinner, I spend around 3 hours for homework and revision. Before sleeping, I love playing LOL with my best friends. And finally I finish my day at 1am.").apply();


        // Message Pref
        prefs = requireActivity().getSharedPreferences(AI_MESSAGE_PREFS_NAME, 0);
        prefs.edit().putString("message_sender_0", "user").apply();
        prefs.edit().putString("message_text_0", "What are the advantages of using Java over Kotlin in Android development?").apply();
        prefs.edit().putString("message_sender_1", "model").apply();
        prefs.edit().putString("message_text_1", "<p><strong>Advantages of Java over Kotlin in Android Development:</strong></p>\n" +
                "<ul>\n" +
                "<li><strong>Larger developer community:</strong> Java has a much larger developer community than Kotlin, which means it has a wider range of libraries, tutorials, and support forums.</li>\n" +
                "<li><strong>Established ecosystem:</strong> Java has been used for Android development for over a decade, so there is a mature ecosystem of tools, frameworks, and third-party libraries available.</li>\n" +
                "<li><strong>Stability:</strong> Java is a stable language with a well-defined specification. This makes it less likely to undergo significant changes that could break existing code.</li>\n" +
                "<li><strong>Easy to integrate with existing code:</strong> Java code can be easily integrated with existing code written in other languages, such as C++.</li>\n" +
                "<li><strong>Backwards compatibility:</strong> Java guarantees backwards compatibility, meaning newer versions of the language are typically compatible with older versions. This makes it easier to upgrade and maintain code over time.</li>\n" +
                "<li><strong>Widely used:</strong> Java is a widely used language in many industries, including enterprise software development, web development, and data science. This makes it easier to find experienced Java developers.</li>\n" +
                "<li><strong>Platform independence:</strong> Java code can be compiled into bytecode that can run on different platforms, including Android, iOS, and desktop computers.</li>\n" +
                "</ul>\n" +
                "<p><strong>Note:</strong> It's important to mention that these advantages are primarily due to Java's long history and widespread adoption. Kotlin is a relatively newer language, but it offers many benefits that make it a compelling choice for Android development, such as improved type safety, conciseness, and null safety.</p>\n").apply();
        prefs.edit().putString("message_datetime_1", "1715500911986").apply();
        prefs.edit().putString("message_datetime_0", "1715500904203").apply();
        prefs.edit().putInt("MessageCount", 2).apply();

        // Set Pref
        prefs = requireActivity().getSharedPreferences(SET_PREFS_NAME, 0);
        prefs.edit().putInt("SetCount", 3).apply();
        prefs.edit().putString("set_title_0", "Animals").apply();
        prefs.edit().putLong("set_datetime_0", 1715501657929L).apply();
        prefs.edit().putString("set_id_1", "1").apply();
        prefs.edit().putString("set_title_1", "Transport").apply();
        prefs.edit().putString("set_id_2", "2").apply();
        prefs.edit().putString("set_title_2", "Fruit").apply();
        prefs.edit().putLong("set_datetime_2", 1715501903381L).apply();
        prefs.edit().putString("set_id_0", "0").apply();
        prefs.edit().putLong("set_datetime_1", 1715502113715L).apply();
        prefs.edit().putInt("set_count_2", 8).apply();
        prefs.edit().putInt("set_count_1", 4).apply();
        prefs.edit().putInt("set_count_0", 4).apply();

        // Card Pref 0
        prefs = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + "0", 0);
        prefs.edit().putString("set_0_card_1_image", "").apply();
        prefs.edit().putLong("set_0_card_2_datetime", 1715501657936L).apply();
        prefs.edit().putString("set_0_card_1_back", "Katze").apply();
        prefs.edit().putInt("set_0_card_0_setID", 0).apply();
        prefs.edit().putString("set_0_card_2_back", "Vogel").apply();
        prefs.edit().putString("set_0_card_0_image", "").apply();
        prefs.edit().putString("set_0_card_0_front", "Dog").apply();
        prefs.edit().putString("set_0_card_3_back", "Fisch").apply();
        prefs.edit().putString("set_0_card_0_back", "Hund").apply();
        prefs.edit().putString("set_0_card_2_front", "Bird").apply();
        prefs.edit().putInt("set_0_card_3_setID", 0).apply();
        prefs.edit().putLong("set_0_card_1_datetime", 1715501657935L).apply();
        prefs.edit().putInt("set_0_card_2_setID", 0).apply();
        prefs.edit().putString("set_0_card_3_front", "Fish").apply();
        prefs.edit().putInt("CardCount", 4).apply();
        prefs.edit().putLong("set_0_card_0_datetime", 1715501657935L).apply();
        prefs.edit().putString("set_0_card_1_front", "Cat").apply();
        prefs.edit().putString("set_0_card_3_image", "").apply();
        prefs.edit().putString("set_0_card_2_image", "").apply();
        prefs.edit().putLong("set_0_card_3_datetime", 1715501657937L).apply();
        prefs.edit().putInt("set_0_card_1_setID", 0).apply();





    }


}