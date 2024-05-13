package edu.cuhk.csci3310project;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

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
    private EditText timeEditText;
    private CheckBox notificationCheckBox;


    private boolean notificationEnabled = false;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        createNotificationChannel();
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
        timeEditText = rootView.findViewById(R.id.settingTimeEditText);
        notificationCheckBox = rootView.findViewById(R.id.settingNotificationCheckBox);
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

        notificationCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked){
                setAlarm();
            } else {
                cancelAlarm();
            }
        });

        String alarmTime = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0)
                .getString("alarmTime", "");
        timeEditText.setText(alarmTime);
        notificationCheckBox.setChecked(!alarmTime.isEmpty());

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

    private void setAlarm(){
        // Schedule alarm
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Calculate the time when the alarm should go off (current time + 5 seconds)
        long triggerTime = SystemClock.elapsedRealtime() + 5000;

        // Set the alarm
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);

        Toast.makeText(requireActivity(), "Alarm set", Toast.LENGTH_SHORT).show();

        return;

        /*
        // validate time. format: HH:mm
        String time = timeEditText.getText().toString();
        if (time.isEmpty()){
            Toast.makeText(requireActivity(), R.string.invalid_time, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]")){
            Toast.makeText(requireActivity(), R.string.invalid_time, Toast.LENGTH_SHORT).show();
            return;
        }
        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1]);
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59){
            Toast.makeText(requireActivity(), R.string.invalid_time, Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireActivity(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);


        SharedPreferences prefs = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0);
        prefs.edit().putString("alarmTime", time).apply();
        Toast.makeText(requireActivity(), R.string.alarm_set, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Alarm set at " + time);
         */
    }

    private void cancelAlarm(){

        Intent intent = new Intent(requireActivity(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager == null){
            alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
        }
        alarmManager.cancel(pendingIntent);


        SharedPreferences prefs = requireActivity().getSharedPreferences(SETTINGS_PREFS_NAME, 0);
        prefs.edit().remove("alarmTime").apply();
        Toast.makeText(requireActivity(), R.string.alarm_cancel, Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "channel";
            String desc = "Channel for Alarm Manager";
            int imp = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("myLanguageApp", name, imp);
            channel.setDescription(desc);
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
        prefs.edit().putString("quiz_set_id_0", "0").apply();
        prefs.edit().putLong("quiz_set_datetime_0", 1715533525159L).apply();
        prefs.edit().putString("quiz_set_correct_answer_0_4", "Pfirsch").apply();
        prefs.edit().putInt("QuizSetCount", 1).apply();
        prefs.edit().putInt("quiz_set_question_count_0", 5).apply();
        prefs.edit().putString("quiz_set_question_0_0", "Apple").apply();
        prefs.edit().putString("quiz_set_user_answer_0_2", "").apply();
        prefs.edit().putString("quiz_set_user_answer_0_1", "").apply();
        prefs.edit().putString("quiz_set_user_answer_0_0", "").apply();
        prefs.edit().putInt("quiz_set_source_count_0", 2).apply();
        prefs.edit().putString("quiz_set_user_answer_0_4", "").apply();
        prefs.edit().putString("quiz_set_user_answer_0_3", "").apply();
        prefs.edit().putString("quiz_set_question_0_1", "Ferry").apply();
        prefs.edit().putString("quiz_set_question_0_2", "Motocycle").apply();
        prefs.edit().putString("quiz_set_source_0_0", "1").apply();
        prefs.edit().putString("quiz_set_question_0_3", "Airplane").apply();
        prefs.edit().putString("quiz_set_source_0_1", "0").apply();
        prefs.edit().putString("quiz_set_question_0_4", "Peach").apply();
        prefs.edit().putString("quiz_set_correct_answer_0_1", "Fähre").apply();
        prefs.edit().putInt("quiz_set_correct_count_0", -1).apply();
        prefs.edit().putString("quiz_set_correct_answer_0_0", "Apfel").apply();
        prefs.edit().putString("quiz_set_correct_answer_0_3", "Flugzeug").apply();
        prefs.edit().putString("quiz_set_correct_answer_0_2", "Motorrad").apply();
        prefs.edit().putString("quiz_set_title_0", "Quiz 1").apply();



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
        prefs.edit().putInt("SetCount", 2).apply();
        prefs.edit().putString("set_title_0", "Transport").apply();
        prefs.edit().putLong("set_datetime_0", 1715501657929L).apply();
        prefs.edit().putString("set_id_1", "1").apply();
        prefs.edit().putString("set_title_1", "Fruit").apply();
        prefs.edit().putString("set_id_0", "0").apply();
        prefs.edit().putLong("set_datetime_1", 1715502113715L).apply();
        prefs.edit().putInt("set_count_1", 4).apply();
        prefs.edit().putInt("set_count_0", 5).apply();

        // Card Pref 0
        prefs = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + "0", 0);
        prefs.edit().putString("set_0_card_1_image", "").apply();
        prefs.edit().putLong("set_0_card_2_datetime", 1715531132432L).apply();
        prefs.edit().putInt("set_0_card_0_setID", 0).apply();
        prefs.edit().putString("set_0_card_2_back", "Auto").apply();
        prefs.edit().putString("set_0_card_4_front", "Ferry").apply();
        prefs.edit().putString("set_0_card_2_front", "Car").apply();
        prefs.edit().putLong("set_0_card_1_datetime", 1715501657933L).apply();
        prefs.edit().putLong("set_0_card_4_datetime", 1715501657938L).apply();
        prefs.edit().putInt("set_0_card_2_setID", 0).apply();
        prefs.edit().putString("set_0_card_3_image", "").apply();
        prefs.edit().putString("set_0_card_1_back", "Flugzeug").apply();
        prefs.edit().putInt("set_0_card_4_setID", 0).apply();
        prefs.edit().putString("set_0_card_0_image", "").apply();
        prefs.edit().putString("set_0_card_0_front", "Train").apply();
        prefs.edit().putString("set_0_card_4_back", "Fähre").apply();
        prefs.edit().putString("set_0_card_3_back", "Motorrad").apply();
        prefs.edit().putString("set_0_card_0_back", "Zug").apply();
        prefs.edit().putInt("set_0_card_3_setID", 0).apply();
        prefs.edit().putString("set_0_card_4_image", "").apply();
        prefs.edit().putString("set_0_card_3_front", "Motocycle").apply();
        prefs.edit().putInt("CardCount", 5).apply();
        prefs.edit().putLong("set_0_card_0_datetime", 1715501657928L).apply();
        prefs.edit().putString("set_0_card_1_front", "Airplane").apply();
        prefs.edit().putString("set_0_card_2_image", "").apply();
        prefs.edit().putLong("set_0_card_3_datetime", 1715501657933L).apply();
        prefs.edit().putInt("set_0_card_1_setID", 0).apply();

        // Card Pref 1
        prefs = requireActivity().getSharedPreferences(CARD_PREFS_NAME_PREFIX + "1", 0);
        prefs.edit().putInt("set_1_card_0_setID", 1).apply();
        prefs.edit().putLong("set_1_card_1_datetime", 1715531305661L).apply();
        prefs.edit().putString("set_1_card_1_back", "Pfirsch").apply();
        prefs.edit().putString("set_1_card_2_image", "").apply();
        prefs.edit().putString("set_1_card_1_front", "Peach").apply();
        prefs.edit().putString("set_1_card_0_image", "").apply();
        prefs.edit().putString("set_1_card_2_back", "Zitronen").apply();
        prefs.edit().putString("set_1_card_1_image", "").apply();
        prefs.edit().putString("set_1_card_0_front", "Apple").apply();
        prefs.edit().putInt("set_1_card_3_setID", 1).apply();
        prefs.edit().putLong("set_1_card_3_datetime", 1715531305665L).apply();
        prefs.edit().putString("set_1_card_0_back", "Apfel").apply();
        prefs.edit().putLong("set_1_card_0_datetime", 1715531305660L).apply();
        prefs.edit().putString("set_1_card_3_front", "Pear").apply();
        prefs.edit().putInt("CardCount", 4).apply();
        prefs.edit().putInt("set_1_card_1_setID", 1).apply();
        prefs.edit().putInt("set_1_card_2_setID", 1).apply();
        prefs.edit().putString("set_1_card_2_front", "Lemon").apply();
        prefs.edit().putLong("set_1_card_2_datetime", 1715531305662L).apply();
        prefs.edit().putString("set_1_card_3_back", "Birne").apply();
        prefs.edit().putString("set_1_card_3_image", "").apply();

    }


}