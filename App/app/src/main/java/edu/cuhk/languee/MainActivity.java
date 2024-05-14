package edu.cuhk.languee;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "CHANNEL_ID_NOTIFICATION";
    private static final String SETTINGS_PREFS_NAME = "settingsPrefs";

    private DrawerLayout drawerLayout;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    public AlarmManager getAlarmManager() {
        return alarmManager;
    }
    public void setAlarmManager(AlarmManager alarmManager) {
        this.alarmManager = alarmManager;
    }
    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }
    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkNotificationPermission() {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS, android.Manifest.permission.SCHEDULE_EXACT_ALARM}, 123);
        } else {
            // Permissions granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted
            } else {
                // Permissions denied
            }
        }
    }

    private void createNotificationChannel(){
        CharSequence channelName = getResources().getString(R.string.notification_title);
        String channelDescription = getResources().getString(R.string.notification_title);
        int channelImportance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, channelImportance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        } else {
            Log.e(TAG, "createNotificationChannel: Notification channel cannot be created");
        }
    }

    private boolean scheduleNotification(int requestCode, long timeElapsed) {
        try{
            // initialize alarmManager and pendingIntent
            if (alarmManager == null) {
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            }
            if (pendingIntent == null) {
                Intent intent = new Intent(this, MyBroaodcastReceiver.class);
                intent.putExtra("notificationTitle", getResources().getString(R.string.notification_title));
                intent.putExtra("notificationContentText", getResources().getString(R.string.notification_content));
                pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeElapsed, AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.d(TAG, "scheduleNotification: Notification scheduled at " + timeElapsed);
        } catch (Exception e) {
            Log.e(TAG, "scheduleNotification: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void cancelNotification() {
        if (pendingIntent != null) {
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
        } else {
            Log.w(TAG, "cancelNotification: pendingIntent is null");
        }
    }

    private void SetNotification() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Notification permission denied");
            return;
        }
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFS_NAME, 0);
        boolean notificationEnabled = prefs.getBoolean("enableNotification", false);
        if (!notificationEnabled) {
            Log.d(TAG, "Notification is disabled in settings");
            cancelNotification();
            return;
        }

        int requestCode = 0;
        int hour = prefs.getInt("notificationHour", -1);
        int minute = prefs.getInt("notificationMinute", -1);
        if (hour == -1 || minute == -1) {
            Log.d(TAG, "Alarm set fail, hour or minute is not set");
            cancelNotification();
            return;
        }
        // timeElapsed = 07:35
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        long timeElapsed = calendar.getTimeInMillis();

        // create a new alarm
        boolean isSuccess = scheduleNotification(requestCode, timeElapsed);

        // save the alarm time to SharedPreferences
        if (isSuccess){
            Log.d(TAG, "Alarm set success, time: " + hour + ":" + minute);
        } else {
            cancelNotification();
            Log.d(TAG, "Alarm set fail");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }

        // Set up notification channel
        createNotificationChannel();

        // Set the language of the app
        Locale locale = new Locale(AppLanguage.fromCode(
                getSharedPreferences("settingsPrefs", 0).getString("language", AppLanguage.ENGLISH.getCode())).getCode());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.home);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        SetNotification();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        NavigateToFragmentByNavID(item.getItemId());

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        // if current fragment is not home
        else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass() != HomeFragment.class) {

            // if current fragment is flashcard create, go back to flashcard main
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass() == FlashcardCreateFragment.class) {
                NavigateToFragmentByNavID(R.id.nav_flashcard);
            }
            // if current fragment is flashcard view, go back to flashcard main
            else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass() == FlashcardViewFragment.class) {
                NavigateToFragmentByNavID(R.id.nav_flashcard);
            }
            // if current fragment is write create, go back to write main
            else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass() == WriteCreateFragment.class) {
                NavigateToFragmentByNavID(R.id.nav_write);
            }
            // if current fragment is quiz create, go back to quiz main
            else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass() == QuizCreateFragment.class) {
                NavigateToFragmentByNavID(R.id.nav_quiz);
            }
            // if current fragment is quiz view, go back to quiz main
            else if (getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass() == QuizAttemptFragment.class) {
                NavigateToFragmentByNavID(R.id.nav_quiz);
            }

            // else, go back to home
            else{
                NavigateToFragmentByNavID(R.id.nav_home);
            }

        }
        // if current fragment is home, exit
        else {
            super.onBackPressed();
        }
    }

    public void UpdateLanguage() {
        // Update the language of the app
        // This method will be called when the user changes the language in the settings
        // You can use this method to update the language of the app
        Locale locale = new Locale(AppLanguage.fromCode(
                getSharedPreferences("settingsPrefs", 0).getString("language", AppLanguage.ENGLISH.getCode())).getCode());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate();
    }

    public void NavigateToFragmentByNavID(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (id == R.id.nav_home) {
            Log.d(TAG, "Home clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_home);
            getSupportActionBar().setTitle(R.string.home);
        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "Settings clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SettingsFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_settings);
            getSupportActionBar().setTitle(R.string.settings);
        } else if (id == R.id.nav_write) {
            Log.d(TAG, "Write clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WriteMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_write);
            getSupportActionBar().setTitle(R.string.write);
        } else if (id == R.id.nav_flashcard) {
            Log.d(TAG, "Read clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new FlashcardMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_flashcard);
            getSupportActionBar().setTitle(R.string.flashcard);
        } else if (id == R.id.nav_ai) {
            Log.d(TAG, "AI clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new AiMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_ai);
            getSupportActionBar().setTitle(R.string.ai);
        } else if (id ==R.id.nav_quiz) {
            Log.d(TAG, "Quiz clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new QuizMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_quiz);
            getSupportActionBar().setTitle(R.string.quiz);
        }
        else {
            // default to home
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_home);
            getSupportActionBar().setTitle(R.string.home);
        }
    }

    public void NavigateToFragmentByFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragment).commit();
    }
}