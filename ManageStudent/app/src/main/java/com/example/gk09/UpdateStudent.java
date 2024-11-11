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

        editName = findViewById(R.id.editName);
        editClass = findViewById(R.id.editClass);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editAge = findViewById(R.id.editAge);
        editAddress = findViewById(R.id.editAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        studentId = getIntent().getStringExtra("studentId");
        editName.setText(getIntent().getStringExtra("name"));
        editClass.setText(getIntent().getStringExtra("studentClass"));
        editEmail.setText(getIntent().getStringExtra("email"));
        editPhone.setText(getIntent().getStringExtra("phone"));
        editAddress.setText(getIntent().getStringExtra("address"));

        Long age = getIntent().getLongExtra("age", 0L);
        editAge.setText(String.valueOf(age));

        btnUpdate.setOnClickListener(v -> updateStudent());
    }

    private void updateStudent() {
        String name = editName.getText().toString().trim();
        String studentClass = editClass.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();

        if (name.isEmpty() || studentClass.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        long age;
        try {
            age = Long.parseLong(ageStr);
        } catch (NumberFormatException e) {
            age = 0L;
        }

        Map<String, Object> student = new HashMap<>();
        student.put("name", name);
        student.put("studentClass", studentClass);
        student.put("email", email);
        student.put("phone", phone);
        student.put("address", address);
        student.put("age", age);  // Use the long value
        student.put("timestamp", FieldValue.serverTimestamp());

        db.collection("students")
                .document(studentId)
                .update(student)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to list
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Error updating student: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
