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

import java.util.HashMap;
import java.util.Map;

public class AddStudent extends AppCompatActivity {

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

            editName = findViewById(R.id.editName);
            editClass = findViewById(R.id.editClass);
            editEmail = findViewById(R.id.editEmail);
            editPhone = findViewById(R.id.editPhone);
            editAge = findViewById(R.id.editAge);
            editAddress = findViewById(R.id.editAddress);
            btnSave = findViewById(R.id.btnSave);
            progressBar = findViewById(R.id.progressBar);

            btnSave.setOnClickListener(v -> saveStudent());

        } catch (Exception e) {
            Log.e("AddStudent", "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Error initializing: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveStudent() {
        try {
            String name = editName.getText().toString().trim();
            String studentClass = editClass.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            String ageStr = editAge.getText().toString().trim();

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

            db.collection("students")
                    .add(student)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e("AddStudent", "Error saving student: " + e.getMessage());
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
