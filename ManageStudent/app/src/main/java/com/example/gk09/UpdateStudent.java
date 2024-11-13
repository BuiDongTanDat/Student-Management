package com.example.gk09;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class UpdateStudent extends AppCompatActivity {
    private EditText editName, editClass, editEmail, editPhone, editAge, editAddress;
    private Button btnUpdate;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_student);

        db = FirebaseFirestore.getInstance();

        initializeViews();

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "Error: Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadStudentData();

        btnUpdate.setOnClickListener(v -> updateStudent());
    }

    private void initializeViews() {
        editName = findViewById(R.id.editName);
        editClass = findViewById(R.id.editClass);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editAge = findViewById(R.id.editAge);
        editAddress = findViewById(R.id.editAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadStudentData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("students").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        editName.setText(documentSnapshot.getString("name"));
                        editClass.setText(documentSnapshot.getString("studentClass"));
                        editEmail.setText(documentSnapshot.getString("email"));
                        editPhone.setText(documentSnapshot.getString("phone"));
                        editAddress.setText(documentSnapshot.getString("address"));
                        Long age = documentSnapshot.getLong("age");
                        if (age != null) {
                            editAge.setText(String.valueOf(age));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading student data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStudent() {
        String name = editName.getText().toString().trim();
        String studentClass = editClass.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            editName.setError("Name is required");
            return;
        }
        if (studentClass.isEmpty()) {
            editClass.setError("Class is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Valid email is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("nameSearch", name.toLowerCase());
        updates.put("studentClass", studentClass);
        updates.put("classSearch", studentClass.toLowerCase());
        updates.put("email", email);
        updates.put("emailSearch", email.toLowerCase());
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("age", Integer.parseInt(ageStr.isEmpty() ? "0" : ageStr));

        db.collection("students").document(studentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student updated successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Error updating student: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
