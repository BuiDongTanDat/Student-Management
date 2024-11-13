package com.example.gk09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    EditText emailLogin, passwordLogin;
    Button btnLogin;
    ProgressBar processLogin;
    private FireStoreHelper fireStoreHelper;

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("User Session", MODE_PRIVATE);
        String savedUid = sharedPreferences.getString("uid", null);

        FirebaseApp.initializeApp(this);
        FirebaseFirestore.getInstance();

        processLogin = findViewById(R.id.processLogin);

        if (savedUid != null) {
            // If the user is already logged in, navigate to AfterLogin
            Intent intent = new Intent(MainActivity.this, AfterLogin.class);
            intent.putExtra("uid", savedUid);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        btnLogin = findViewById(R.id.btnLogin);

        fireStoreHelper = new FireStoreHelper();

        btnLogin.setOnClickListener(view -> {
            processLogin.setVisibility(View.VISIBLE);
            String email = emailLogin.getText().toString().trim();
            String password = passwordLogin.getText().toString().trim();

            // Perform the login check
            fireStoreHelper.checkLogin(email, password, new FireStoreHelper.LoginCallback() {
                @Override
                public void onSuccess(String uid) {
                    processLogin.setVisibility(View.GONE);
                    //Save history login
                    fireStoreHelper.logLoginHistory(uid);


                    // Clear old session data
                    SharedPreferences sharedPreferences = getSharedPreferences("User Session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // Clear old user data
                    editor.putString("uid", uid); // Store new UID
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, AfterLogin.class);
                    intent.putExtra("uid", uid);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Show an error message to the user
                    processLogin.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
