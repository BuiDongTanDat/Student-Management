package com.example.gk09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;


public class AfterLogin extends AppCompatActivity {

    TextView nameLogin;
    ImageView avtUser ;
    CardView goUser , goStudent;
    ProgressBar progressFistLoad;

    // Init variables for user
    String uid, displayName, email, phone, imageUrl, role, password;
    String uidBefore;
    int age;
    boolean status;
    User user;
    String roleSession;

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.afterlogin);

        nameLogin = findViewById(R.id.nameLogin);
        avtUser  = findViewById(R.id.avtUser );
        goUser  = findViewById(R.id.goUser );
        goStudent = findViewById(R.id.goStudent);
        progressFistLoad = findViewById(R.id.progressFistLoad);

        // Check if user logged in before
        SharedPreferences sharedPreferences = getSharedPreferences("User  Session", MODE_PRIVATE);
        String uidBefore = sharedPreferences.getString("uid", null); // Retrieve UID

        Intent intent1 = getIntent();
        uid = intent1.getStringExtra("uid"); // Get UID from Intent

        // Check if the UID in SharedPreferences is the same as the one from Intent
        if (uidBefore != null && uidBefore.equals(uid)) {
            loadUserInfo();
        } else {
            // User is new, update SharedPreferences and load info
            Log.d("User Check", "New user: " + uid);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("uid", uid); // Store new UID
            editor.apply();
            loadUserInfo();
        }


        avtUser.setOnClickListener(view -> {
            Intent intent = new Intent(AfterLogin.this, UserProfile.class);
            user = new User(uid,displayName, email, password, phone, role, imageUrl, status, age);
            Log.d("Pas", user.getPassword());
            intent.putExtra("user", user);
            Log.d("Pass", user.getPassword());
            startActivity(intent);
        });

        goUser.setOnClickListener(view -> {
            Intent intent = new Intent(AfterLogin.this, UserManage.class);
            intent.putExtra("uid", uid);
            intent.putExtra("currRole", roleSession);
            startActivity(intent);
        });
    }



                    if ("manager".equals(role)) {
                        goUser .setActivated(false);
                    }
                    progressFistLoad.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.w("DB_ERROR", errorMessage);
                    progressFistLoad.setVisibility(View.GONE);
                }
            });
        } else {
            Log.w("DB_ERROR", "UID is null. Cannot load user info.");
            progressFistLoad.setVisibility(View.GONE);
        }
    }
}