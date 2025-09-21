package ca.scooter.androidpractice;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StringBuilder logBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addLogEntry("onCreate: Activity starting. savedInstanceState is " + (savedInstanceState == null ? "null" : "not null"));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        addLogEntry("onCreate: Content view set to R.layout.activity_login");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            addLogEntry("onCreate: Window insets applied. Left: " + systemBars.left + ", Top: " + systemBars.top + ", Right: " + systemBars.right + ", Bottom: " + systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        addLogEntry("onCreate: Firebase Auth and Firestore instances obtained.");

        MaterialButton login_gh = findViewById(R.id.login_gh);
        MaterialButton view_logs_button = findViewById(R.id.view_logs_button);
        addLogEntry("onCreate: UI elements (login_gh, view_logs_button) found.");

        login_gh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLogEntry("login_gh.onClick: GitHub login button clicked.");
                signInWithGithub();
            }
        });

        view_logs_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLogEntry("view_logs_button.onClick: View Logs button clicked.");
                showLogsDialog();
            }
        });
        addLogEntry("onCreate: UI listeners initialized. Activity creation complete.");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        addLogEntry("onNewIntent: New intent received. Action: " + intent.getAction() + ", Data: " + intent.getDataString());
    }

    @Override
    public void onStart() {
        super.onStart();
        addLogEntry("onStart: Activity started.");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            addLogEntry("onStart: User already logged in. UID: " + currentUser.getUid() + ", DisplayName: " + currentUser.getDisplayName());
            navigateHome();
        } else {
            addLogEntry("onStart: No user currently logged in. Ready for new sign-in flow or pending result.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addLogEntry("onResume: Activity resumed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        addLogEntry("onPause: Activity paused.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        addLogEntry("onStop: Activity stopped.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addLogEntry("onDestroy: Activity being destroyed.");
    }

    private void addLogEntry(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
        logBuilder.append(timestamp).append(": ").append(message).append("\n");
        Log.d(TAG, message); 
    }

    private void signInWithGithub() {
        addLogEntry("signInWithGithub: Initiating GitHub sign-in process.");
        OAuthProvider.Builder providerBuilder = OAuthProvider.newBuilder("github.com");
        List<String> scopes = new ArrayList<>();
        scopes.add("user:email");
        providerBuilder.setScopes(scopes);
        addLogEntry("signInWithGithub: OAuthProvider for 'github.com' configured with scopes: " + scopes.toString());

        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();

        if (pendingResultTask != null) {
            addLogEntry("signInWithGithub: Found pending authentication result. Processing...");
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user = authResult.getUser();
                                    AdditionalUserInfo additionalUserInfo = authResult.getAdditionalUserInfo();
                                    String displayName = user != null ? user.getDisplayName() : "Unknown User";
                                    addLogEntry("signInWithGithub/pending: Success. User: " + displayName + ", UID: " + (user != null ? user.getUid() : "N/A") + ", ProviderId: " + (additionalUserInfo != null ? additionalUserInfo.getProviderId() : "N/A"));
                                    if (user != null) {
                                        addLogEntry("signInWithGithub/pending: User details - Email: " + user.getEmail() + ", PhotoURL: " + user.getPhotoUrl());
                                    }
                                    if (additionalUserInfo != null) {
                                        addLogEntry("signInWithGithub/pending: Additional User Info - Username: " + additionalUserInfo.getUsername() + ", Profile: " + additionalUserInfo.getProfile());
                                    }
                                    Toast.makeText(LoginActivity.this, "Welcome back " + displayName, Toast.LENGTH_SHORT).show();
                                    userSave(user, authResult);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    addLogEntry("signInWithGithub/pending: Failure - Error: " + e.getMessage() + ", Cause: " + e.getCause());
                                    Log.w(TAG, "signInWithGithub/pending: onFailure", e);
                                    Toast.makeText(LoginActivity.this, "Authentication failed (pending task). " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
        } else {
            addLogEntry("signInWithGithub: No pending result task. Starting new sign-in activity.");
            mAuth.startActivityForSignInWithProvider(this, providerBuilder.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user = authResult.getUser();
                                    AdditionalUserInfo additionalUserInfo = authResult.getAdditionalUserInfo();
                                    String displayName = user != null ? user.getDisplayName() : "Unknown User";
                                    addLogEntry("signInWithGithub/activity: Success. User: " + displayName + ", UID: " + (user != null ? user.getUid() : "N/A") + ", ProviderId: " + (additionalUserInfo != null ? additionalUserInfo.getProviderId() : "N/A"));
                                    if (user != null) {
                                        addLogEntry("signInWithGithub/activity: User details - Email: " + user.getEmail() + ", PhotoURL: " + user.getPhotoUrl());
                                    }
                                    if (additionalUserInfo != null) {
                                        addLogEntry("signInWithGithub/activity: Additional User Info - Username: " + additionalUserInfo.getUsername() + ", Profile: " + additionalUserInfo.getProfile());
                                    }
                                    userSave(user, authResult);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    addLogEntry("signInWithGithub/activity: Failure - Error: " + e.getMessage() + ", Cause: " + e.getCause());
                                    Log.w(TAG, "signInWithGithub/activity: onFailure", e);
                                    Toast.makeText(LoginActivity.this, "Authentication failed (new task): " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
        }
    }

    private void userSave(FirebaseUser firebaseUser, AuthResult authResult) {
        if (firebaseUser == null) {
            addLogEntry("userSave: FirebaseUser is null. Cannot save user data.");
            Toast.makeText(LoginActivity.this, "Login error: User data not available.", Toast.LENGTH_LONG).show();
            return;
        }
        addLogEntry("userSave: Attempting to save user data. UID: " + firebaseUser.getUid());

        String userID = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        Uri photoUri = firebaseUser.getPhotoUrl();
        String photoUrl = (photoUri != null) ? photoUri.toString() : null;

        String githubUsername = null;
        if (authResult != null && authResult.getAdditionalUserInfo() != null && authResult.getAdditionalUserInfo().getUsername() != null) {
            githubUsername = authResult.getAdditionalUserInfo().getUsername();
            addLogEntry("userSave: GitHub username obtained: " + githubUsername);
        } else {
            addLogEntry("userSave: GitHub username not found in additional user info. AuthResult: " + (authResult == null ? "null" : "exists") + ", AdditionalInfo: " + (authResult != null && authResult.getAdditionalUserInfo() == null ? "null" : "exists"));
            Toast.makeText(this, "GitHub username not found.", Toast.LENGTH_LONG).show();
        }

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("avatarUrl", photoUrl);
        user.put("githubUsername", githubUsername);
        addLogEntry("userSave: User data prepared for Firestore: " + user.toString());

        DocumentReference userDocRef = db.collection("users").document(userID);
        addLogEntry("userSave: Firestore document reference: " + userDocRef.getPath());
        userDocRef.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        addLogEntry("userSave: User details successfully saved to Firestore path: " + userDocRef.getPath());
                        navigateHome();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        addLogEntry("userSave: Failed to save user details to Firestore path: " + userDocRef.getPath() + ". Error: " + e.getMessage() + ", Cause: " + e.getCause());
                        Toast.makeText(LoginActivity.this, "Failed to save user details. Please try again. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateHome() {
        addLogEntry("navigateHome: Preparing to navigate to HomeScreen.");
        Intent intent = new Intent(LoginActivity.this, HomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        addLogEntry("navigateHome: Intent created for HomeScreen with flags NEW_TASK and CLEAR_TASK. Starting activity...");
        startActivity(intent);
        finish();
        addLogEntry("navigateHome: HomeScreen activity started and LoginActivity finished.");
    }

    private void showLogsDialog() {
        addLogEntry("showLogsDialog: Preparing to show logs dialog.");
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.login_logs_dialog_title));

        ScrollView scrollView = new ScrollView(this);
        TextView logsTextView = new TextView(this);
        int paddingInDp = 16;
        float scale = getResources().getDisplayMetrics().density;
        int paddingInPx = (int) (paddingInDp * scale + 0.5f);
        logsTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        logsTextView.setMovementMethod(new ScrollingMovementMethod());
        logsTextView.setTextIsSelectable(true);

        if (logBuilder.length() > 0) {
            logsTextView.setText(logBuilder.toString());
            addLogEntry("showLogsDialog: Displaying " + logBuilder.toString().split("\n").length + " log entries.");
        } else {
            logsTextView.setText(getString(R.string.no_logs_available));
            addLogEntry("showLogsDialog: No logs available to display.");
        }
        scrollView.addView(logsTextView);
        builder.setView(scrollView);

        builder.setPositiveButton(getString(R.string.save_logs_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addLogEntry("showLogsDialog: Save Logs button clicked.");
                saveLogsToFile();
            }
        });
        builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addLogEntry("showLogsDialog: Dismiss button clicked.");
                dialog.dismiss();
            }
        });
        addLogEntry("showLogsDialog: Logs dialog configured. Showing now.");
        builder.show();
    }

    private void saveLogsToFile() {
        if (logBuilder.length() == 0) {
            Toast.makeText(this, getString(R.string.no_logs_available), Toast.LENGTH_SHORT).show();
            addLogEntry("saveLogsToFile: No logs to save.");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "login_logs_" + timestamp + ".txt";
        File externalCacheDir = getExternalCacheDir();
        String logFilePath = "N/A";

        if (externalCacheDir == null) {
            addLogEntry("saveLogsToFile: External cache directory is null. Cannot save logs.");
            Toast.makeText(this, "Error: External storage not available to save logs.", Toast.LENGTH_LONG).show();
            return;
        }
        File logFile = new File(externalCacheDir, fileName);
        logFilePath = logFile.getAbsolutePath();
        addLogEntry("saveLogsToFile: Attempting to save logs to: " + logFilePath);

        try (FileOutputStream fos = new FileOutputStream(logFile)) {
            fos.write(logBuilder.toString().getBytes());
            String successMsg = getString(R.string.logs_saved_success, logFilePath);
            Toast.makeText(this, successMsg, Toast.LENGTH_LONG).show();
            addLogEntry("saveLogsToFile: Logs successfully saved to " + logFilePath);
        } catch (IOException e) {
            Log.e(TAG, "Error saving logs to file", e);
            Toast.makeText(this, getString(R.string.logs_saved_failed) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            addLogEntry("saveLogsToFile: Error saving logs to " + logFilePath + " - Error: " + e.getMessage() + ", Cause: " + e.getCause());
        }
    }
}
