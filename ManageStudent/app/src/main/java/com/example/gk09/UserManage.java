package com.example.gk09;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManage extends AppCompatActivity {

    RecyclerView viewUser;
    Button btnAddUser;
    private UserAdapter userAdapter;
    private List<QueryDocumentSnapshot> userList; // Change to QueryDocumentSnapshot

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.usermanage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewUser = findViewById(R.id.viewUser);
        btnAddUser = findViewById(R.id.btnAddUser);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        viewUser.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewUser.setAdapter(userAdapter);

        fetchUsersFromFirestore();
    }

    private void fetchUsersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's UID

        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Check if the document ID is not equal to the current user's UID
                    if (!document.getId().equals(currentUserId)) {
                        Log.d("USER", document.toString());
                        userList.add(document);
                    }
                }
                userAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the list
            } else {
                // Handle error
                Log.w("USER_FETCH_ERROR", "Error getting documents.", task.getException());
            }
        });
    }
}
