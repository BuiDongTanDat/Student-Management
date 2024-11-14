package com.example.gk09;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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
    private String uid;


    @SuppressLint("ClickableViewAccessibility")
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


        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        viewUser .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewUser .setAdapter(userAdapter);

        // Get role from SharedPreferences (if needed)
        SharedPreferences sharedPreferences = getSharedPreferences("User Session", Context.MODE_PRIVATE);
        String role = sharedPreferences.getString("role", null);



        if (!role.equals("admin")) {
            btnAddUser .setVisibility(View.GONE);
        } else {
            btnAddUser.setVisibility(View.VISIBLE);

            btnAddUser .setOnTouchListener(new View.OnTouchListener() {
                float dX, dY;
                boolean isDragging = false; // Track if we are dragging
                final float CLICK_THRESHOLD = 10; // Threshold for distinguishing click and drag

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Store the difference between the button's position and the touch position
                            dX = view.getX() - event.getRawX();
                            dY = view.getY() - event.getRawY();
                            isDragging = false; // Reset dragging flag
                            break;

                        case MotionEvent.ACTION_MOVE:
                            // Update the button's position
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            // Get the dimensions of the parent view
                            int parentWidth = ((View) view.getParent()).getWidth();
                            int parentHeight = ((View) view.getParent()).getHeight();

                            // Set boundaries
                            float minX = 0;
                            float maxX = parentWidth - view.getWidth();
                            float minY = 0;
                            float maxY = parentHeight - view.getHeight();

                            // Check if the movement exceeds the threshold
                            if (Math.abs(newX - view.getX()) > CLICK_THRESHOLD || Math.abs(newY - view.getY()) > CLICK_THRESHOLD) {
                                isDragging = true; // Set dragging flag if we are dragging
                            }

                            // Constrain the new position within the parent view's bounds
                            newX = Math.max(minX, Math.min(newX, maxX));
                            newY = Math.max(minY, Math.min(newY, maxY));

                            // Move the button
                            view.animate()
                                    .x(newX)
                                    .y(newY)
                                    .setDuration(0)
                                    .start();
                            break;

                        case MotionEvent.ACTION_UP:
                            // If we were not dragging, treat it as a click
                            if (!isDragging) {
                                view.performClick(); // This will trigger the click listener
                            }
                            break;

                        default:
                            return false;
                    }
                    return true;
                }
            });

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