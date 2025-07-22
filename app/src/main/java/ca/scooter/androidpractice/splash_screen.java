package ca.scooter.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class splash_screen extends AppCompatActivity {

    private static final long SPLASH_DELAY = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(splash_screen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}