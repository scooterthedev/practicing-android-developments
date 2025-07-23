package ca.scooter.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import ca.scooter.androidpractice.databinding.ActivityHomeScreenBinding;

public class HomeScreen extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    public static final String GITHUB_USERNAME = "ca.scooter.androidpractice.GITHUB_USERNAME";
    public static final String GITHUB_EMAIL = "ca.scooter.androidpractice.GITHUB_EMAIL";
    public static final String GITHUB_AVATAR_URL = "ca.scooter.androidpractice.GITHUB_AVATAR_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHomeScreenBinding binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHomeScreen.toolbar);
        binding.appBarHomeScreen.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;


        //nav header stuff
        View headerView = navigationView.getHeaderView(0);
        ImageView imageViewGithubAvatar = headerView.findViewById(R.id.imageViewGithubAvatar);
        TextView textViewGithubUsername = headerView.findViewById(R.id.textViewGitHubUsername);
        TextView textViewGithubEmail = headerView.findViewById(R.id.textViewGitHubEmail);

        Intent intent = getIntent();
        String githubUsername = intent.getStringExtra(GITHUB_USERNAME);
        String githubEmail = intent.getStringExtra(GITHUB_EMAIL);
        String githubAvatarUrl = intent.getStringExtra(GITHUB_AVATAR_URL);

        textViewGithubUsername.setText(githubUsername);
        textViewGithubEmail.setText(githubEmail);

        Glide.with(this)
                .load(githubAvatarUrl)
                .circleCrop()
                .into(imageViewGithubAvatar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_screen);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home_screen);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}