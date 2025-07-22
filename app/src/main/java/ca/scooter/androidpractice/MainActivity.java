package ca.scooter.androidpractice;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ca.scooter.androidpractice.auth.ui.login.LoginActivity;
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
        boolean needsAuth = intent.getBooleanExtra("NEEDS_AUTH", false);
        boolean isFirstInMain = intent.getBooleanExtra("IS_FIRST_TIME", false);

        if (needsAuth || mAuth.getCurrentUser() == null) {
            Intent authIntent = new Intent(this, LoginActivity.class);
            startActivity(authIntent);
            finish();
        } else if (isFirstInMain) {
            // THis is for when the user is authed, but firt time opening like when they want to restart onboarding on re-install the app without clearing the data
            Intent setupIntent = new Intent(this, setup_page.class);
            startActivity(setupIntent);
            finish();
            }
        }

    }