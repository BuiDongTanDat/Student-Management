package com.example.gk09;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddCertificate extends AppCompatActivity {

    private static final String TAG = "AddCertificateActivity";
    private EditText editCertificateName, editDescription, editIssuedBy;
    private Button btnIssueDatePicker, btnExpiryDatePicker, btnSave;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String studentId;
    private Date issueDate, expiryDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_certificate);

        // Get student ID
        studentId = getIntent().getStringExtra("studentId");
        Log.d(TAG, "Received studentId: " + studentId);

        if (studentId == null) {
            Log.e(TAG, "No student ID provided");
            Toast.makeText(this, "Error: Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initializeViews();
        setupButtons();

        // Set default issue date to today
        issueDate = Calendar.getInstance().getTime();
        btnIssueDatePicker.setText(dateFormat.format(issueDate));
    }

    private void initializeViews() {
        editCertificateName = findViewById(R.id.editCertificateName);
        editDescription = findViewById(R.id.editDescription);
        editIssuedBy = findViewById(R.id.editIssuedBy);
        btnIssueDatePicker = findViewById(R.id.btnIssueDatePicker);
        btnExpiryDatePicker = findViewById(R.id.btnExpiryDatePicker);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtons() {
        btnIssueDatePicker.setOnClickListener(v -> showDatePicker(true));
        btnExpiryDatePicker.setOnClickListener(v -> showDatePicker(false));
        btnSave.setOnClickListener(v -> saveCertificate());
    }

    private void saveCertificate() {
        String name = editCertificateName.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String issuedBy = editIssuedBy.getText().toString().trim();

        if (name.isEmpty()) {
            editCertificateName.setError("Certificate name is required");
            return;
        }
        if (issuedBy.isEmpty()) {
            editIssuedBy.setError("Issuer is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Map<String, Object> certificate = new HashMap<>();
        certificate.put("name", name);
        certificate.put("description", description);
        certificate.put("issuedBy", issuedBy);
        certificate.put("issueDate", issueDate);
        certificate.put("createAt", Calendar.getInstance().getTime());
        if (expiryDate != null) {
            certificate.put("expiryDate", expiryDate);
        }

        db.collection("students")
                .document(studentId)
                .collection("certificates")
                .add(certificate)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Certificate added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Certificate added successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding certificate: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error adding certificate: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker(boolean isIssueDate) {
        Calendar calendar = Calendar.getInstance();
        if (isIssueDate && issueDate != null) {
            calendar.setTime(issueDate);
        } else if (!isIssueDate && expiryDate != null) {
            calendar.setTime(expiryDate);
        }

        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    if (isIssueDate) {
                        issueDate = selectedDate.getTime();
                        btnIssueDatePicker.setText(dateFormat.format(issueDate));
                    } else {
                        expiryDate = selectedDate.getTime();
                        btnExpiryDatePicker.setText(dateFormat.format(expiryDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

}
