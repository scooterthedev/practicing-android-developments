package ca.scooter.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import ca.scooter.androidpractice.databinding.ActivityHomeScreenBinding;

public class HomeScreen extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ImageView imageViewGithubAvatar;
    private TextView textViewGithubUsername;
    private TextView textViewGithubEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHomeScreenBinding binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        //loading in the users data from firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            loadData(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeScreen.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

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

    private void loadData(String userID){
        DocumentReference userDocRef = db.collection("users").document(userID);

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document != null & document.exists()){
                    String username = document.getString("username");
                    String email = document.getString("email");
                    String avatarUrl = document.getString("avatarUrl");

                    updateUI(username, email, avatarUrl);
                } else {
                    Toast.makeText(HomeScreen.this, "What service provider are you using lol, because this service aint working", Toast.LENGTH_LONG).show();
                    //display defualts when everything is null
                    updateUI(null, null, null);
                }
            }
        });
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