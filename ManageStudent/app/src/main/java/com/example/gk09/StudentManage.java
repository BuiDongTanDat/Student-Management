package com.example.gk09;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentManage extends AppCompatActivity {
    private RecyclerView viewStudent;
    private FloatingActionButton btnAddStudent;
    private StudentAdapter studentAdapter;
    private List<QueryDocumentSnapshot> studentList;
    private FirebaseFirestore db;
    private ProgressBar loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d("StudentManage", "Starting onCreate");

            setContentView(R.layout.activity_student_manage);
            Log.d("StudentManage", "Content view set");

            // Initialize Firestore
            db = FirebaseFirestore.getInstance();
            Log.d("StudentManage", "Firestore initialized");

            // Initialize views
            viewStudent = findViewById(R.id.viewStudent);
            loadingProgress = findViewById(R.id.loadingProgress);
            btnAddStudent = findViewById(R.id.btnAddStudent);
            Log.d("StudentManage", "Views initialized");

            // Initialize list and adapter
            studentList = new ArrayList<>();
            studentAdapter = new StudentAdapter(studentList, this);
            Log.d("StudentManage", "Adapter initialized");

            // Setup RecyclerView
            viewStudent.setLayoutManager(new LinearLayoutManager(this));
            viewStudent.setAdapter(studentAdapter);
            Log.d("StudentManage", "RecyclerView setup complete");

            // Load students
            fetchStudentsFromFirestore();

        } catch (Exception e) {
            Log.e("StudentManage", "Error in onCreate: " + e.getMessage(), e);
        }

        btnAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(StudentManage.this, AddStudent.class);
            startActivity(intent);
        });
    }

    private void fetchStudentsFromFirestore() {
        loadingProgress.setVisibility(View.VISIBLE);

        db.collection("students")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingProgress.setVisibility(View.GONE);
                    studentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        studentList.add(document);
                    }
                    studentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading students: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("StudentManage", "Error fetching students", e);
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStudentsFromFirestore(); // Refresh list when returning to activity
    }
}
