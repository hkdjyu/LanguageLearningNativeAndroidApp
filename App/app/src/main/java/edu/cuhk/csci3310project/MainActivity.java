package edu.cuhk.csci3310project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Home");

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

    public void NavigateToFragmentByNavID(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (id == R.id.nav_home) {
            Log.d("MainActivity", "Home clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_home);
            getSupportActionBar().setTitle("Home");
        } else if (id == R.id.nav_settings) {
            Log.d("MainActivity", "Settings clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SettingsFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_settings);
            getSupportActionBar().setTitle("Settings");
        } else if (id == R.id.nav_write) {
            Log.d("MainActivity", "Write clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WriteMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_write);
            getSupportActionBar().setTitle("Write");
        } else if (id == R.id.nav_flashcard) {
            Log.d("MainActivity", "Read clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new FlashcardMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_flashcard);
            getSupportActionBar().setTitle("Flashcard");
        } else if (id == R.id.nav_ai) {
            Log.d("MainActivity", "AI clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new AiMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_ai);
            getSupportActionBar().setTitle("AI");
        } else if (id ==R.id.nav_quiz) {
            Log.d("MainActivity", "Quiz clicked");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new QuizMainFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_quiz);
            getSupportActionBar().setTitle("Quiz");
        }
        else {
            // default to home
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
//            navigationView.setCheckedItem(R.id.nav_home);
            getSupportActionBar().setTitle("Home");
        }
    }

    public void NavigateToFragmentByFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragment).commit();
    }
}