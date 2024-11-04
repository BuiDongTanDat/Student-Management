package com.example.gk09;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class AfterLogin extends AppCompatActivity {

    TextView nameLogin;
    ImageView avtUser;
    CardView goUser, goStudent;
    public Toolbar toolbar;
    //Init variables for user
    String displayName, email, phone, imageUrl, role;
    int age;
    boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.afterlogin);




        nameLogin = findViewById(R.id.nameLogin);
        avtUser = findViewById(R.id.avtUser);
        goUser = findViewById(R.id.goUser);
        goStudent = findViewById(R.id.goStudent);

        // Get the currently logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("USER", user != null ? user.getUid() : "No user logged in");

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        if (user != null) {
            DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        displayName = document.getString("name");
                        imageUrl = document.getString("image");
                        role = document.getString("role");
                        phone = document.getString("phone");
                        email = document.getString("email");
                        status = document.getBoolean("status");
                        age = document.getLong("age").intValue();

                        nameLogin.setVisibility(View.GONE);
                        avtUser.setVisibility(View.GONE);
                        nameLogin.setText(displayName);
                        Picasso.get().load(imageUrl).into(avtUser);


                        nameLogin.setVisibility(View.VISIBLE);
                        avtUser.setVisibility(View.VISIBLE);

                        if ("manager".equals(role)) {
                            goUser.setVisibility(CardView.GONE);
                        }
                    }
                } else {
                    Log.w("DB_ERROR", "User document does not exist.");
                }
            });



        }


        avtUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AfterLogin.this, UserProfile.class);
                User user = new User(displayName, email, phone, phone, role, imageUrl, status, age);
                intent.putExtra("user", user);
                startActivity(intent);

            }
        });


        goUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AfterLogin.this, UserManage.class);
                startActivity(intent);
            }
        });



    }
}
