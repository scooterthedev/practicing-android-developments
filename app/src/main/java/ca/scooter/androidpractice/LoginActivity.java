package ca.scooter.androidpractice;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();

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
                                    userSave(authResult.getUser());
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "signInWithGithub(): onFailure", e);
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mAuth.startActivityForSignInWithProvider(this, providerBuilder.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.d(TAG, "signInWithGithub(): onSuccess" + Objects.requireNonNull(authResult.getUser()).getDisplayName());
                                    userSave(authResult.getUser());
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "signInWithGithub(): onFailure", e);
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void userSave(FirebaseUser firebaseUser){
        if (firebaseUser == null){
            return;
        }

        String userID = firebaseUser.getUid();
        String name= firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        Uri photoUri = firebaseUser.getPhotoUrl();
        String photoUrl = (photoUri != null) ? photoUri.toString() : null;

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("avatarUrl", photoUrl);

        DocumentReference userDocRef = db.collection("users").document(userID);
        userDocRef.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        navigateHome();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Failed to save user details. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            private void navigateHome() {
                Intent intent = new Intent(LoginActivity.this, HomeScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }