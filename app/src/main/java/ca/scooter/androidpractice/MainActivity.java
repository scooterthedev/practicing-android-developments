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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        Intent intent = getIntent();
        boolean needsAuth = intent.getBooleanExtra("NEEDS_AUTH", false);
        boolean isFirstInMain = intent.getBooleanExtra("IS_FIRST_TIME", false);

        if (needsAuth || mAuth.getCurrentUser() == null) {

            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                    .build();
            navController.navigate(R.id.loginActivity, null, navOptions);

            Intent authIntent = new Intent(this, LoginActivity.class);
            startActivity(authIntent);
            finish();
        } else if (isFirstInMain) {
            // THis is for when the user is authed, but firt time opening like when they want to restart onboarding on re-install the app without clearing the data
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                    .build();
            navController.navigate(R.id.setup_page, null, navOptions);
            }
        }

    }