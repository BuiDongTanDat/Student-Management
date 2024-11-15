package com.example.gk09;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddCertificate extends AppCompatActivity {

    private static final String TAG = "AddCertificate";
    private EditText editCertificateName, editDescription, editIssuedBy;
    private Button btnIssueDatePicker, btnExpiryDatePicker, btnSave;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String studentId;
    private Date issueDate, expiryDate;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_certificate);

            // Enable back button in action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Certificate");

            studentId = getIntent().getStringExtra("studentId");
            if (studentId == null || studentId.isEmpty()) {
                Log.e(TAG, "No student ID provided");
                Toast.makeText(this, "Error: No student ID provided", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            db = FirebaseFirestore.getInstance();
            dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            initializeViews();
            setupButtons();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        editCertificateName = findViewById(R.id.editCertificateName);
        editDescription = findViewById(R.id.editDescription);
        editIssuedBy = findViewById(R.id.editIssuedBy);
        btnIssueDatePicker = findViewById(R.id.btnIssueDatePicker);
        btnExpiryDatePicker = findViewById(R.id.btnExpiryDatePicker);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        btnIssueDatePicker.setText("SELECT ISSUE DATE");
        btnExpiryDatePicker.setText("SELECT EXPIRY DATE (OPTIONAL)");
    }

    private void setupButtons() {
        btnIssueDatePicker.setOnClickListener(v -> showDatePicker(true));
        btnExpiryDatePicker.setOnClickListener(v -> showDatePicker(false));
        btnSave.setOnClickListener(v -> saveCertificate());
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
                        // Update expiry date min date
                        if (expiryDate != null && expiryDate.before(issueDate)) {
                            expiryDate = null;
                            btnExpiryDatePicker.setText("SELECT EXPIRY DATE (OPTIONAL)");
                        }
                    } else {
                        expiryDate = selectedDate.getTime();
                        btnExpiryDatePicker.setText(dateFormat.format(expiryDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if (isIssueDate) {
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        } else if (issueDate != null) {
            picker.getDatePicker().setMinDate(issueDate.getTime());
        }

        picker.show();
    }

    private void saveCertificate() {
        String name = editCertificateName.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String issuedBy = editIssuedBy.getText().toString().trim();

        if (name.isEmpty()) {
            editCertificateName.setError("Name is required");
            return;
        }

        if (issueDate == null) {
            Toast.makeText(this, "Please select issue date", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Map<String, Object> certificate = new HashMap<>();
        certificate.put("name", name);
        certificate.put("description", description);
        certificate.put("issuedBy", issuedBy);
        certificate.put("issueDate", issueDate);
        if (expiryDate != null) {
            certificate.put("expiryDate", expiryDate);
        }
        certificate.put("timestamp", FieldValue.serverTimestamp());

        db.collection("students")
                .document(studentId)
                .collection("certificates")
                .add(certificate)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Certificate added with ID: " + documentReference.getId());
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding certificate: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
