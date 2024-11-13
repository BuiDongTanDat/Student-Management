package com.example.gk09;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class UserManage extends AppCompatActivity {

    private RecyclerView viewUser ;
    private FloatingActionButton btnAddUser;
    private UserAdapter userAdapter;
    private ProgressBar progressBarLoadUser;
    private List<User> userList;
    private String uid, currRole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.usermanage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        progressBarLoadUser = findViewById(R.id.progressBarLoadUser);
        viewUser  = findViewById(R.id.viewUser );
        btnAddUser  = findViewById(R.id.btnAddUser );

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        currRole = intent.getStringExtra("currRole");
        Log.d("UserManage", "Received UID: " + currRole);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this, currRole);
        viewUser .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewUser .setAdapter(userAdapter);

        if (!currRole.equals("admin")) {
            btnAddUser .setVisibility(View.GONE);
        } else {
            btnAddUser.setVisibility(View.VISIBLE);
            btnAddUser.setOnClickListener(v -> {
                Intent intent1 = new Intent(UserManage.this, UserAdd.class);
                startActivityForResult(intent1, 222);

            });
        }

        // Fetch users after setting up the adapter
        fetchUsersFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void fetchUsersFromFirestore() {
        progressBarLoadUser.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query userQuery = db.collection("users");

        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    if (document.getId().equals(uid)) {
                        continue;
                    }
                    User user = document.toObject(User.class);
                    user.setId(document.getId());
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged(); // Notify the adapter for updates
                progressBarLoadUser.setVisibility(View.GONE);
            } else {
                Log.w("USER_FETCH_ERROR", "Error getting documents.", task.getException());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 222 && resultCode == Activity.RESULT_OK
                || requestCode == 333 && resultCode == Activity.RESULT_OK) {
            userList.clear();
            userAdapter.notifyDataSetChanged();

            fetchUsersFromFirestore(); // Fetch updated user list from Firestore
        }
    }
}