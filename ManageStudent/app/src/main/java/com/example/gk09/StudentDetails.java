package com.example.gk09;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentDetails extends AppCompatActivity {
    private static final String TAG = "StudentDetails";
    private static final int ADD_CERTIFICATE_REQUEST = 100;
    private static final int PICK_CSV_FILE = 2;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private DataImportExport dataImportExport;

    private String studentId;
    private RecyclerView certificateRecyclerView;
    private CertificateAdapter certificateAdapter;
    private List<QueryDocumentSnapshot> certificateList;
    private FloatingActionButton fabAddCertificate;
    private FirebaseFirestore db;
    private TextView nameText, classText, emailText, phoneText, ageText, addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        try {
            // Initialize Firestore first
            db = FirebaseFirestore.getInstance();


            // Get studentId from intent
            studentId = getIntent().getStringExtra("studentId");
            db = FirebaseFirestore.getInstance();
            dataImportExport = new DataImportExport(this);
            certificateList = new ArrayList<>();
            if (studentId == null || studentId.isEmpty()) {
                Toast.makeText(this, "Error: Student ID not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views and setup UI
            initializeViews();
            setupRecyclerView();

            // Load data
            loadStudentInfo();
            loadCertificates();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing view", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("User Session", Context.MODE_PRIVATE);
        String role = sharedPreferences.getString("role", null);
        if (role != null && role.equals("employee")) {
            fabAddCertificate.setVisibility(View.GONE);
        }
    }

    private void initializeViews() {
        // Initialize TextViews
        nameText = findViewById(R.id.tvName);
        classText = findViewById(R.id.tvClass);
        emailText = findViewById(R.id.tvEmail);
        phoneText = findViewById(R.id.tvPhone);
        ageText = findViewById(R.id.tvAge);
        addressText = findViewById(R.id.tvAddress);

        // Initialize RecyclerView and FAB
        certificateRecyclerView = findViewById(R.id.certificateRecyclerView);
        fabAddCertificate = findViewById(R.id.btnAddCertificate);

        // Setup FAB click listener
        fabAddCertificate.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(StudentDetails.this, AddCertificate.class);
                intent.putExtra("studentId", studentId);
                startActivityForResult(intent, ADD_CERTIFICATE_REQUEST);
            } catch (Exception e) {
                Log.e(TAG, "Error starting AddCertificate: " + e.getMessage());
                Toast.makeText(this, "Error launching certificate form", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");

        certificateList = new ArrayList<>();
        certificateAdapter = new CertificateAdapter(certificateList, studentId, this, this);

        if (certificateRecyclerView != null) {
            certificateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            certificateRecyclerView.setAdapter(certificateAdapter);
            certificateRecyclerView.setHasFixedSize(true);
            Log.d(TAG, "RecyclerView setup complete");
        } else {
            Log.e(TAG, "certificateRecyclerView is null!");
        }
    }

    private void loadStudentInfo() {
        if (studentId == null) return;

        db.collection("students")
                .document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            nameText.setText(documentSnapshot.getString("name"));
                            classText.setText(documentSnapshot.getString("studentClass"));
                            emailText.setText(documentSnapshot.getString("email"));
                            phoneText.setText(documentSnapshot.getString("phone"));
                            Long age = documentSnapshot.getLong("age");
                            ageText.setText(age != null ? String.valueOf(age) : "N/A");
                            addressText.setText(documentSnapshot.getString("address"));
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting student info: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "Student document doesn't exist");
                        Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading student info: " + e.getMessage());
                    Toast.makeText(this, "Error loading student info", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCertificates() {
        if (studentId == null) {
            Log.e(TAG, "StudentId is null");
            return;
        }

        Log.d(TAG, "Loading certificates for student: " + studentId);

        // First verify the student document exists
        db.collection("students").document(studentId).get()
                .addOnSuccessListener(studentDoc -> {
                    if (studentDoc.exists()) {
                        Log.d(TAG, "Student found. Loading certificates...");

                        // Now load certificates
                        db.collection("students")
                                .document(studentId)
                                .collection("certificates")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    Log.d(TAG, "Found " + querySnapshot.size() + " certificates");

                                    certificateList.clear();
                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        Log.d(TAG, "Certificate data: " + document.getData());
                                        certificateList.add(document);
                                    }

                                    // Update UI
                                    if (certificateList.isEmpty()) {
                                        TextView tvNoCertificates = findViewById(R.id.tvNoCertificates);
                                        if (tvNoCertificates != null) {
                                            tvNoCertificates.setVisibility(View.VISIBLE);
                                            certificateRecyclerView.setVisibility(View.GONE);
                                        }
                                        Log.d(TAG, "No certificates found for student");
                                    } else {
                                        TextView tvNoCertificates = findViewById(R.id.tvNoCertificates);
                                        if (tvNoCertificates != null) {
                                            tvNoCertificates.setVisibility(View.GONE);
                                            certificateRecyclerView.setVisibility(View.VISIBLE);
                                        }
                                        certificateAdapter.updateData(certificateList);
                                        Log.d(TAG, "Updated adapter with " + certificateList.size() + " certificates");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading certificates: " + e.getMessage());
                                });
                    } else {
                        Log.e(TAG, "Student document not found!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verifying student: " + e.getMessage());
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == ADD_CERTIFICATE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Certificate added successfully");
                loadCertificates(); // Refresh the list
            }
        } else if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Log.d(TAG, "Selected CSV file: " + uri.toString());
                // Show loading progress
                ProgressBar progressBar = findViewById(R.id.progressBar);
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                dataImportExport.importCertificates(studentId, uri, new DataImportExport.ImportCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(StudentDetails.this,
                                    message, Toast.LENGTH_LONG).show();
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            loadCertificates(); // Refresh the list
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(StudentDetails.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show();
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning to this screen
        if (studentId != null) {
            loadStudentInfo();
            loadCertificates();
        }
    }

    public void showDeleteConfirmation(String certificateId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Certificate")
                .setMessage("Are you sure you want to delete this certificate?")
                .setPositiveButton("Yes", (dialog, which) -> deleteCertificate(certificateId))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteCertificate(String certificateId) {
        if (studentId == null || certificateId == null) return;

        db.collection("students")
                .document(studentId)
                .collection("certificates")
                .document(certificateId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Certificate deleted", Toast.LENGTH_SHORT).show();
                    loadCertificates();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting certificate: " + e.getMessage());
                    Toast.makeText(this, "Error deleting certificate", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importCertificates();
            } else {
                Toast.makeText(this,
                        "Storage permission is required to import/export files",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Method to create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sharedPreferences = getSharedPreferences("User Session", Context.MODE_PRIVATE);
        String role = sharedPreferences.getString("role", null);
        // If the role is 'employee', do not show any options
        if (role != null && role.equals("employee")) {
            return false;  // Do not show the menu
        }
        getMenuInflater().inflate(R.menu.menu_student_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_import_certificates) {
            importCertificates();
            return true;
        } else if (id == R.id.action_export_certificates) {
            exportCertificates();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",
                        getApplicationContext().getPackageName())));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    private void importCertificates() {
        if (!checkStoragePermission()) {
            requestStoragePermission();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_CSV_FILE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Please install a file manager app", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCertificates() {
        if (!checkStoragePermission()) {
            requestStoragePermission();
            return;
        }

        dataImportExport.exportCertificates(studentId, new DataImportExport.ExportCallback() {
            @Override
            public void onSuccess(String filePath) {
                runOnUiThread(() -> {
                    Toast.makeText(StudentDetails.this,
                            "Certificates exported to: " + filePath, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(StudentDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Export error: " + error);
                });
            }
        });
    }
}

