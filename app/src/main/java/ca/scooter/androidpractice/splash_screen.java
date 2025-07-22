package ca.scooter.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ca.scooter.androidpractice.auth.ui.login.LoginActivity;

public class splash_screen extends AppCompatActivity {

    private static final long SPLASH_DELAY = 4000;
    private static final String PREF_NAME = "SplashPrefs";
    private static final String KEY_FIRST_LAUNCH = "firstLaunch";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean(KEY_FIRST_LAUNCH, true);

            if (isFirstTime) {
                Intent intent = new Intent(splash_screen.this, setup_page.class);
                startActivity(intent);
                intent.putExtra("IS_FIRST_TIME", true);
                startActivity(intent);
            } else {
                Intent intent = new Intent(splash_screen.this, MainActivity.class);
                startActivity(intent);
                intent.putExtra("NEEDS_AUTH", false);
                startActivity(intent);
            }
            finish();
        }, SPLASH_DELAY);
    }
}