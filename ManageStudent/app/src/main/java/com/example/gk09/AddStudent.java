package com.example.gk09;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AddStudent extends AppCompatActivity {
    private static final String TAG = "AddStudent";
    private EditText editName, editClass, editEmail, editPhone, editAge, editAddress;
    private Button btnSave;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    // Phone validation pattern (adjust according to your needs)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_student);

            db = FirebaseFirestore.getInstance();
            initializeViews();
            setupValidationListeners();
            btnSave.setOnClickListener(v -> validateAndSaveStudent());

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

    private void setupValidationListeners() {
        // Real-time email validation
        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
                    editEmail.setError("Invalid email format");
                }
            }
        });

        // Real-time phone validation
        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();
                if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
                    editPhone.setError("Phone number must be 10-11 digits");
                }
            }
        });

        // Age validation
        editAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().isEmpty()) {
                        int age = Integer.parseInt(s.toString());
                        if (age < 0 || age > 100) {
                            editAge.setError("Age must be between 0 and 100");
                        }
                    }
                } catch (NumberFormatException e) {
                    editAge.setError("Invalid age");
                }
            }
        });
    }

    private void validateAndSaveStudent() {
        // Disable the save button to prevent double submission
        btnSave.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Get and validate all fields
        String name = editName.getText().toString().trim();
        String studentClass = editClass.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();

        // Field validation
        if (!validateFields(name, studentClass, email, phone, ageStr)) {
            btnSave.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Check for duplicate email
        checkDuplicateEmail(email, exists -> {
            if (exists) {
                runOnUiThread(() -> {
                    editEmail.setError("Email already registered");
                    btnSave.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddStudent.this,
                            "A student with this email already exists",
                            Toast.LENGTH_LONG).show();
                });
            } else {
                // Proceed with saving the student
                saveStudent(name, studentClass, email, phone, address, ageStr);
            }
        });
    }

    private boolean validateFields(String name, String studentClass, String email,
                                   String phone, String ageStr) {
        boolean isValid = true;

        // Name validation
        if (name.isEmpty()) {
            editName.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            editName.setError("Name must be at least 2 characters");
            isValid = false;
        }

        // Class validation
        if (studentClass.isEmpty()) {
            editClass.setError("Class is required");
            isValid = false;
        }

        // Email validation
        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            editEmail.setError("Invalid email format");
            isValid = false;
        }

        // Phone validation
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            editPhone.setError("Invalid phone number format");
            isValid = false;
        }

        // Age validation
        try {
            if (!ageStr.isEmpty()) {
                int age = Integer.parseInt(ageStr);
                if (age < 0 || age > 100) {
                    editAge.setError("Age must be between 0 and 100");
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            editAge.setError("Invalid age");
            isValid = false;
        }

        return isValid;
    }

    private void checkDuplicateEmail(String email, DuplicateCheckCallback callback) {
        db.collection("students")
                .whereEqualTo("emailSearch", email.toLowerCase())
                .get()
                .addOnSuccessListener(querySnapshot ->
                        callback.onResult(!querySnapshot.isEmpty()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking duplicate email: " + e.getMessage());
                    callback.onResult(false); // Assume no duplicate in case of error
                });
    }

    private void saveStudent(String name, String studentClass, String email,
                             String phone, String address, String ageStr) {
        try {
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

            // Create initial certificate
            Map<String, Object> initialCertificate = new HashMap<>();
            initialCertificate.put("name", "Initial");
            initialCertificate.put("description", "Initial certificate to create collection");
            initialCertificate.put("issuedBy", "System");
            initialCertificate.put("issueDate", FieldValue.serverTimestamp());
            initialCertificate.put("createAt", FieldValue.serverTimestamp());

            // Use WriteBatch for atomic operation
            WriteBatch batch = db.batch();
            DocumentReference studentRef = db.collection("students").document();

            batch.set(studentRef, student);
            batch.set(studentRef.collection("certificates").document("initial"),
                    initialCertificate);

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Student added successfully");
                        Toast.makeText(AddStudent.this,
                                "Student added successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding student: " + e.getMessage());
                        btnSave.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddStudent.this,
                                "Error adding student: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in saveStudent: " + e.getMessage());
            btnSave.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private interface DuplicateCheckCallback {
        void onResult(boolean exists);
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