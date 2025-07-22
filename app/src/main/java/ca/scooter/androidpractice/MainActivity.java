package ca.scooter.androidpractice;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import ca.scooter.androidpractice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        boolean isFirstInMain = intent.getBooleanExtra("IS_FIRST_TIME", false);

        if (mAuth.getCurrentUser() == null) {
            // User is not authed at all
            Intent authIntent = new Intent(this, LoginActivity.class);
            startActivity(authIntent);
            finish();
        } else if (isFirstInMain) {
            // User is authed, but it's their first time after login
            Intent setupIntent = new Intent(this, setup_page.class);
            startActivity(setupIntent);
            finish();
        } else {
            Intent mainInt = new Intent(this, HomeScreen.class);
            startActivity(mainInt);
            finish();
        }
        }
    }