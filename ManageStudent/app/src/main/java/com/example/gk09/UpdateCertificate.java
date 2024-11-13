package com.example.gk09;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UpdateCertificate extends AppCompatActivity {

    private EditText editCertificateName, editDescription, editIssuedBy;
    private Button btnIssueDatePicker, btnExpiryDatePicker, btnUpdate;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String certificateId;
    private String studentId;
    private Date issueDate, expiryDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_certificate);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Initialize views
        editCertificateName = findViewById(R.id.editCertificateName);
        editDescription = findViewById(R.id.editDescription);
        editIssuedBy = findViewById(R.id.editIssuedBy);
        btnIssueDatePicker = findViewById(R.id.btnIssueDatePicker);
        btnExpiryDatePicker = findViewById(R.id.btnExpiryDatePicker);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);

        // Get certificate ID from intent
        certificateId = getIntent().getStringExtra("certificateId");
        studentId = getIntent().getStringExtra("studentId");

        // Load certificate data
        loadCertificateData();

        // Set up date pickers
        btnIssueDatePicker.setOnClickListener(v -> showDatePicker(true));
        btnExpiryDatePicker.setOnClickListener(v -> showDatePicker(false));

        // Update button click listener
        btnUpdate.setOnClickListener(v -> updateCertificate());
    }

    private void loadCertificateData() {
        if (certificateId == null || studentId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        db.collection("students").document(studentId)
                .collection("certificates").document(certificateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        Certificate certificate = documentSnapshot.toObject(Certificate.class);
                        if (certificate != null) {
                            editCertificateName.setText(certificate.getName());
                            editDescription.setText(certificate.getDescription());
                            editIssuedBy.setText(certificate.getIssuedBy());
                            issueDate = certificate.getIssueDate();
                            expiryDate = certificate.getExpiryDate();
                            btnIssueDatePicker.setText(dateFormat.format(issueDate));
                            if (expiryDate != null) {
                                btnExpiryDatePicker.setText(dateFormat.format(expiryDate));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading certificate: " + e.getMessage(),
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

        DatePickerDialog picker = new DatePickerDialog(this,
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
                calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void updateCertificate() {
        String name = editCertificateName.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String issuedBy = editIssuedBy.getText().toString().trim();

        if (name.isEmpty() || issuedBy.isEmpty() || issueDate == null) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        Map<String, Object> certificate = new HashMap<>();
        certificate.put("name", name);
        certificate.put("description", description);
        certificate.put("issuedBy", issuedBy);
        certificate.put("issueDate", issueDate);
        certificate.put("expiryDate", expiryDate);

        DocumentReference certificateRef = db.collection("students").document(studentId)
                .collection("certificates").document(certificateId);

        certificateRef.update(certificate)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Certificate updated successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Error updating certificate: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
