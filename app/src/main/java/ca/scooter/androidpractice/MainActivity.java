package ca.scooter.androidpractice;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ca.scooter.androidpractice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    boolean is_authed = false;
    boolean is_first_time = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (is_authed) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        //Checks if the user if loading it for the first time

        navController.navigate(R.id.setup_page);

        navController.navigate(R.id.setup_page, null, new NavOptions.Builder().setPopUpTo(navController.getGraph().getStartDestinationId(), true).build());
        // Launch Splash Screen on startup



        if is_first_time ==

    }

}