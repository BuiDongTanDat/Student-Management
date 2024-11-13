package com.example.gk09;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "StudentDetails";
    private static final int ADD_CERTIFICATE_REQUEST = 1;
    private TextView tvName, tvClass, tvEmail, tvPhone, tvAge, tvAddress;
    private RecyclerView certificateRecyclerView;
    private FloatingActionButton btnAddCertificate;
    private CertificateAdapter certificateAdapter;
    private List<QueryDocumentSnapshot> certificateList;
    private FirebaseFirestore db;
    private String studentId;

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
        certificateAdapter = new CertificateAdapter(certificateList, this);
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
                Log.d(TAG, "Add certificate button clicked. StudentId: " + studentId);
                Intent intent = new Intent(StudentDetails.this, AddCertificate.class);
                intent.putExtra("studentId", studentId);
                startActivityForResult(intent, ADD_CERTIFICATE_REQUEST);
            } catch (Exception e) {
                Log.e(TAG, "Error launching AddCertificate: " + e.getMessage(), e);
                Toast.makeText(this, "Error opening add certificate screen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CERTIFICATE_REQUEST && resultCode == RESULT_OK) {
            loadCertificates();  // Refresh the list after adding
        }
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
