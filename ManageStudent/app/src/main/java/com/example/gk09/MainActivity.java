package com.example.gk09;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText emailLogin, passwordLogin;
    Button btnLogin;
    ProgressBar processLogin;

    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(MainActivity.this, AfterLogin.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        processLogin = findViewById(R.id.processLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailLogin.getText().toString().isEmpty() || passwordLogin.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    processLogin.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(emailLogin.getText().toString(), passwordLogin.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    processLogin.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Log.d("SIGNED IN", "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        // Check the user's status in Firestore
                                        if (user != null) {
                                            checkUserStatus(user.getUid());
                                        }
                                    } else {
                                        Log.w("SIGN IN FAILURE", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(MainActivity.this, "Email or password is incorrect",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }



    private void checkUserStatus(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean status = task.getResult().getBoolean("status");
                        if (status) {
                            // Status is true, proceed to AfterLogin activity
                            Intent intent = new Intent(MainActivity.this, AfterLogin.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Status is false, show a message
                            Toast.makeText(MainActivity.this, "Your account is not activated.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to retrieve user status.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}