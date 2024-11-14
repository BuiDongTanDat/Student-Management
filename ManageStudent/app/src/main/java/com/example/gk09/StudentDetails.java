package com.example.gk09;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentDetails extends AppCompatActivity {
    private static final int PICK_CSV_FILE = 1;
    private static final String TAG = "StudentDetails";
    private static final int ADD_CERTIFICATE_REQUEST = 1;
    private TextView tvName, tvClass, tvEmail, tvPhone, tvAge, tvAddress;
    private RecyclerView certificateRecyclerView;
    private FloatingActionButton btnAddCertificate;
    private CertificateAdapter certificateAdapter;
    private List<QueryDocumentSnapshot> certificateList;
    private FirebaseFirestore db;
    private String studentId;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Log.e(TAG, "No student ID provided");
            Toast.makeText(this, "Error: Could not load student details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "StudentId received: " + studentId);

        // Initialize Firebase and views
        db = FirebaseFirestore.getInstance();
        initializeViews();
        displayStudentDetails();
        setupRecyclerView();
        setupAddCertificateButton();
        loadCertificates();

        SharedPreferences sharedPreferences = getSharedPreferences("User Session", MODE_PRIVATE);
        role = sharedPreferences.getString("role", null);
        if (role != null && role.equals("employee")) {
            btnAddCertificate.setVisibility(View.GONE);
        } else {
            btnAddCertificate.setVisibility(View.VISIBLE);
        }
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tvName);
        tvClass = findViewById(R.id.tvClass);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAge = findViewById(R.id.tvAge);
        tvAddress = findViewById(R.id.tvAddress);
        certificateRecyclerView = findViewById(R.id.certificateRecyclerView);
        btnAddCertificate = findViewById(R.id.btnAddCertificate);
    }

    private void displayStudentDetails() {
        String name = getIntent().getStringExtra("name");
        String studentClass = getIntent().getStringExtra("studentClass");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String address = getIntent().getStringExtra("address");
        long age = getIntent().getLongExtra("age", 0);

        tvName.setText(name != null ? name : "N/A");
        tvClass.setText(studentClass != null ? studentClass : "N/A");
        tvEmail.setText(email != null ? email : "N/A");
        tvPhone.setText(phone != null ? phone : "N/A");
        tvAddress.setText(address != null ? address : "N/A");
        tvAge.setText(String.valueOf(age));
    }

    private void setupRecyclerView() {
        certificateList = new ArrayList<>();
        certificateAdapter = new CertificateAdapter(certificateList, this, this);
        certificateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        certificateRecyclerView.setAdapter(certificateAdapter);
    }

    private void loadCertificates() {
        if (studentId == null) return;

        db.collection("students").document(studentId)
                .collection("certificates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    certificateList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Skip the initial placeholder document
                        if (!"initial".equals(document.getId())) {
                            certificateList.add(document);
                        }
                    }
                    certificateAdapter.notifyDataSetChanged();

                    // Log the number of certificates found
                    Log.d(TAG, "Loaded " + certificateList.size() + " certificates");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading certificates: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading certificates", Toast.LENGTH_SHORT).show();
                });
    }

    public void deleteCertificate(String certificateId) {
        if (studentId == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Certificate")
                .setMessage("Are you sure you want to delete this certificate?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("students").document(studentId)
                            .collection("certificates").document(certificateId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Certificate deleted successfully", Toast.LENGTH_SHORT).show();
                                loadCertificates();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting certificate: " + e.getMessage(), e);
                                Toast.makeText(this, "Error deleting certificate", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    public String getStudentId() {
        return studentId;
    }

    private void setupAddCertificateButton() {
        btnAddCertificate.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(StudentDetails.this, AddCertificate.class);
                intent.putExtra("studentId", studentId);
                startActivityForResult(intent, ADD_CERTIFICATE_REQUEST);
            } catch (Exception e) {
                Log.e(TAG, "Error launching AddCertificate: " + e.getMessage());
                Toast.makeText(this, "Error opening add certificate screen",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem importMenuItem = menu.findItem(R.id.menu_import_certificates);
        MenuItem exportMenuItem = menu.findItem(R.id.menu_export_certificates);
        SharedPreferences sharedPreferences = getSharedPreferences("User Session", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "guest");

        if ("employee".equals(role)) {
            importMenuItem.setVisible(false);  // Ẩn menu item
            importMenuItem.setVisible(false);  // Ẩn menu item
            exportMenuItem.setVisible(false);
        } else {
            importMenuItem.setVisible(true);   // Hiển thị lại menu item
            exportMenuItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_import_certificates) {
            openFilePicker();
            return true;
        } else if (id == R.id.menu_export_certificates) {
            exportCertificates();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");  // For CSV files
            startActivityForResult(intent, PICK_CSV_FILE);
        } catch (Exception e) {
            Log.e(TAG, "Error opening file picker: " + e.getMessage());
            Toast.makeText(this, "Error opening file picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CERTIFICATE_REQUEST) {
            if (resultCode == RESULT_OK) {
                loadStudentDetails();
                loadCertificates();
            }
        }
    }

    private void loadStudentDetails() {
        db.collection("students").document(studentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Update your views with student data
                        tvName.setText(documentSnapshot.getString("name"));
                        tvClass.setText(documentSnapshot.getString("studentClass"));
                        tvEmail.setText(documentSnapshot.getString("email"));
                        tvPhone.setText(documentSnapshot.getString("phone"));
                        tvAddress.setText(documentSnapshot.getString("address"));
                        Long age = documentSnapshot.getLong("age");
                        tvAge.setText(age != null ? age.toString() : "N/A");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading student details: " + e.getMessage());
                    Toast.makeText(this, "Error loading student details",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void importCertificates(Uri fileUri) {
        DataImportExport dataImportExport = new DataImportExport(this);
        dataImportExport.importCertificates(studentId, fileUri, new DataImportExport.ImportCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(StudentDetails.this, message, Toast.LENGTH_SHORT).show();
                loadCertificates(); // Refresh the list
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StudentDetails.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportCertificates() {
        DataImportExport dataImportExport = new DataImportExport(this);
        dataImportExport.exportCertificates(studentId, new DataImportExport.ExportCallback() {
            @Override
            public void onSuccess(String filePath) {
                Toast.makeText(StudentDetails.this,
                        "Certificates exported to: " + filePath,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StudentDetails.this,
                        "Error exporting certificates: " + error,
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error exporting certificates: " + error);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        if (studentId != null) {
            loadCertificates();
        }
    }


}
