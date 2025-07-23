package ca.scooter.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;

    public static final String GITHUB_USERNAME = "ca.scooter.androidpractice.GITHUB_USERNAME";
    public static final String GITHUB_EMAIL = "ca.scooter.androidpractice.GITHUB_EMAIL";
    public static final String GITHUB_AVATAR_URL = "ca.scooter.androidpractice.GITHUB_AVATAR_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        Button login_gh = findViewById(R.id.login_gh);

        login_gh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                signInWithGithub();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
            finish();
        }
    }

    private void signInWithGithub(){
        OAuthProvider.Builder providerBuilder = OAuthProvider.newBuilder("github.com");
        List<String> scopes = new ArrayList<>();
        scopes.add("user:email");
        providerBuilder.setScopes(scopes);

        Task<AuthResult> pendingResultTalk = mAuth.getPendingAuthResult();

        if (pendingResultTalk != null){
            pendingResultTalk
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.d(TAG, "signInWithGithub(): onSuccess" + Objects.requireNonNull(authResult.getUser()).getDisplayName());
                                    Toast.makeText(LoginActivity.this, "Welcome " + authResult.getUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                                    updateUI(authResult.getUser());
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "signInWithGithub(): onFailure", e);
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                }
            });
        } else {
            mAuth.startActivityForSignInWithProvider(this, providerBuilder.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.d(TAG, "signInWithGithub(): onSuccess" + Objects.requireNonNull(authResult.getUser()).getDisplayName());
                                    updateUI(authResult.getUser());
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "signInWithGithub(): onFailure", e);
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                }
            });
        }
    }

    private void updateUI(FirebaseUser user){
        if (user != null){
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photoUrlUri = String.valueOf(user.getPhotoUrl());
            String photoUrl = null;
            if (photoUrlUri != null){
                photoUrl = photoUrlUri.toString();
            }
            Toast.makeText(this, "Signed in as: " + name + " (" + email + ")", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(LoginActivity.this, HomeScreen.class);
            intent.putExtra(GITHUB_USERNAME, name);
            intent.putExtra(GITHUB_EMAIL, email);
            intent.putExtra(GITHUB_AVATAR_URL, photoUrl);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show();
        }
    }
}