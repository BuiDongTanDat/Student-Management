package com.example.gk09;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class AddStudent extends AppCompatActivity {
    private static final String TAG = "AddStudent";
    private EditText editName, editClass, editEmail, editPhone, editAge, editAddress;
    private Button btnSave;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_student);

            db = FirebaseFirestore.getInstance();
            initializeViews();
            btnSave.setOnClickListener(v -> saveStudent());

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Error initializing: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        editName = findViewById(R.id.editName);
        editClass = findViewById(R.id.editClass);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editAge = findViewById(R.id.editAge);
        editAddress = findViewById(R.id.editAddress);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void saveStudent() {
        try {
            // Validation and data collection remains same
            String name = editName.getText().toString().trim();
            String studentClass = editClass.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            String ageStr = editAge.getText().toString().trim();

            // Validation checks remain same
            if (name.isEmpty()) {
                editName.setError("Name is required");
                return;
            }
            if (studentClass.isEmpty()) {
                editClass.setError("Class is required");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Valid email is required");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);

            // Create student data
            Map<String, Object> student = new HashMap<>();
            student.put("name", name);
            student.put("nameSearch", name.toLowerCase());
            student.put("studentClass", studentClass);
            student.put("classSearch", studentClass.toLowerCase());
            student.put("email", email);
            student.put("emailSearch", email.toLowerCase());
            student.put("phone", phone);
            student.put("address", address);
            student.put("age", Integer.parseInt(ageStr.isEmpty() ? "0" : ageStr));
            student.put("imageUrl", "");
            student.put("timestamp", FieldValue.serverTimestamp());

            Map<String, Object> initialCertificate = new HashMap<>();
            initialCertificate.put("name", "Initial");
            initialCertificate.put("description", "Initial certificate to create collection");
            initialCertificate.put("issuedBy", "System");
            initialCertificate.put("issueDate", FieldValue.serverTimestamp());
            initialCertificate.put("createAt", FieldValue.serverTimestamp());

            // Use WriteBatch for atomic operation
            WriteBatch batch = db.batch();

            // Create student document reference with specific ID
            String studentId = db.collection("students").document().getId();

            // Set the student document
            batch.set(db.collection("students").document(studentId), student);

            // Create certificates collection with initial document
            batch.set(
                    db.collection("students")
                            .document(studentId)
                            .collection("certificates")
                            .document("initial"),
                    initialCertificate
            );

            // Commit the batch operation
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Student and certificates collection created successfully");
                        Toast.makeText(AddStudent.this,
                                "Student added successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating student and certificates: ", e);
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(AddStudent.this,
                                "Error adding student: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in saveStudent: " + e.getMessage());
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
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
