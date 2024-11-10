package com.example.gk09;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddStudent extends AppCompatActivity {

    private EditText editName, editClass, editEmail;
    private Button btnSave;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editName = findViewById(R.id.editName);
        editClass = findViewById(R.id.editClass);
        editEmail = findViewById(R.id.editEmail);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for save button
        btnSave.setOnClickListener(v -> saveStudent());
    }

    private void saveStudent() {
        // Get values from fields
        String name = editName.getText().toString().trim();
        String studentClass = editClass.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        // Validate fields
        if (name.isEmpty() || studentClass.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Create student data
        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("studentClass", studentClass);
        student.put("email", email);
        student.put("timestamp", FieldValue.serverTimestamp());

        // Save to Firestore
        db.collection("students")
                .add(student)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddStudent.this, "Student added successfully",
                            Toast.LENGTH_SHORT).show();
                    finish(); // Close activity and return to list
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AddStudent.this, "Error adding student: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
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
}
