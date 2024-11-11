package com.example.gk09;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentManage extends AppCompatActivity {
    private RecyclerView viewStudent;
    private FloatingActionButton btnAddStudent;
    private StudentAdapter studentAdapter;
    private List<QueryDocumentSnapshot> studentList;
    private FirebaseFirestore db;
    private ProgressBar loadingProgress;
    private ImageButton btnSort;
    private EditText searchEditText;
    private CheckBox checkName, checkClass, checkEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d("StudentManage", "Starting onCreate");

            setContentView(R.layout.activity_student_manage);
            Log.d("StudentManage", "Content view set");

            db = FirebaseFirestore.getInstance();
            Log.d("StudentManage", "Firestore initialized");

            studentList = new ArrayList<>();
            viewStudent = findViewById(R.id.viewStudent);
            loadingProgress = findViewById(R.id.loadingProgress);
            btnAddStudent = findViewById(R.id.btnAddStudent);
            btnSort = findViewById(R.id.btnSort);
            searchEditText = findViewById(R.id.searchEditText);
            checkName = findViewById(R.id.checkName);
            checkClass = findViewById(R.id.checkClass);
            checkEmail = findViewById(R.id.checkEmail);
            Log.d("StudentManage", "Views initialized");

            studentList = new ArrayList<>();
            studentAdapter = new StudentAdapter(studentList, this);
            Log.d("StudentManage", "Adapter initialized");

            viewStudent.setLayoutManager(new LinearLayoutManager(this));
            viewStudent.setAdapter(studentAdapter);
            Log.d("StudentManage", "RecyclerView setup complete");

            fetchStudentsFromFirestore();

        } catch (Exception e) {
            Log.e("StudentManage", "Error in onCreate: " + e.getMessage(), e);
        }

        btnAddStudent.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(StudentManage.this, AddStudent.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("StudentManage", "Error starting AddStudent activity: " + e.getMessage());
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnSort.setOnClickListener(v -> showSortOptions());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });
    }

    private void fetchStudentsFromFirestore() {
        loadingProgress.setVisibility(View.VISIBLE);

        db.collection("students")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingProgress.setVisibility(View.GONE);
                    studentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        studentList.add(document);
                    }
                    studentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading students: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("StudentManage", "Error fetching students", e);
                });
    }

    public void deleteStudent(String studentId) {
        db.collection("students")
                .document(studentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                    fetchStudentsFromFirestore(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting student: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void showDeleteConfirmation(String studentId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", (dialog, which) -> deleteStudent(studentId))
                .setNegativeButton("No", null)
                .show();
    }

    private void showSortOptions() {
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Class", "Age (Youngest)", "Age (Oldest)"};

        new AlertDialog.Builder(this)
                .setTitle("Sort by")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortByName(true); // ascending
                            break;
                        case 1:
                            sortByName(false); // descending
                            break;
                        case 2:
                            sortByClass();
                            break;
                        case 3:
                            sortByAge(true); // ascending
                            break;
                        case 4:
                            sortByAge(false); // descending
                            break;
                    }
                })
                .show();
    }

    private void sortByName(boolean ascending) {
        if (ascending) {
            db.collection("students")
                    .orderBy("name", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(this::updateStudentList)
                    .addOnFailureListener(this::handleError);
        } else {
            db.collection("students")
                    .orderBy("name", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(this::updateStudentList)
                    .addOnFailureListener(this::handleError);
        }
    }

    private void sortByClass() {
        loadingProgress.setVisibility(View.VISIBLE);
        db.collection("students")
                .orderBy("studentClass", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    loadingProgress.setVisibility(View.GONE);
                    studentList.clear();
                    // Cast documents to QueryDocumentSnapshot
                    for (DocumentSnapshot document : querySnapshot) {
                        if (document instanceof QueryDocumentSnapshot) {
                            studentList.add((QueryDocumentSnapshot) document);
                        }
                    }
                    studentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error sorting: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("StudentManage", "Sort error", e);
                });
    }

    private void sortByAge(boolean ascending) {
        if (ascending) {
            db.collection("students")
                    .orderBy("age", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(this::updateStudentList)
                    .addOnFailureListener(this::handleError);
        } else {
            db.collection("students")
                    .orderBy("age", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(this::updateStudentList)
                    .addOnFailureListener(this::handleError);
        }
    }

    private void updateStudentList(QuerySnapshot querySnapshot) {
        loadingProgress.setVisibility(View.GONE);
        studentList.clear();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            if (document instanceof QueryDocumentSnapshot) {
                studentList.add((QueryDocumentSnapshot) document);
            }
        }
        studentAdapter.notifyDataSetChanged();
    }
    private void handleError(Exception e) {
        loadingProgress.setVisibility(View.GONE);
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void performSearch(String query) {
        Log.d("Search", "Searching for: " + query);
        if (query.trim().isEmpty()) {
            fetchStudentsFromFirestore();
            return;
        }

        loadingProgress.setVisibility(View.VISIBLE);
        String searchQuery = query.toLowerCase().trim();
        List<Task<QuerySnapshot>> queries = new ArrayList<>();

        if (checkName.isChecked()) {
            Task<QuerySnapshot> nameQuery = db.collection("students")
                    .orderBy("nameSearch")
                    .startAt(searchQuery)
                    .endAt(searchQuery + "\uf8ff")
                    .get();
            queries.add(nameQuery);
        }

        if (checkClass.isChecked()) {
            Task<QuerySnapshot> classQuery = db.collection("students")
                    .orderBy("classSearch")
                    .startAt(searchQuery)
                    .endAt(searchQuery + "\uf8ff")
                    .get();
            queries.add(classQuery);
        }

        if (checkEmail.isChecked()) {
            Task<QuerySnapshot> emailQuery = db.collection("students")
                    .orderBy("emailSearch")
                    .startAt(searchQuery)
                    .endAt(searchQuery + "\uf8ff")
                    .get();
            queries.add(emailQuery);
        }

        if (queries.isEmpty()) {
            Task<QuerySnapshot> defaultQuery = db.collection("students")
                    .orderBy("nameSearch")
                    .startAt(searchQuery)
                    .endAt(searchQuery + "\uf8ff")
                    .get();
            queries.add(defaultQuery);
        }

        Tasks.whenAllComplete(queries)
                .addOnSuccessListener(tasks -> {
                    Set<String> foundIds = new HashSet<>();
                    List<QueryDocumentSnapshot> searchResults = new ArrayList<>();

                    for (Task<?> task : tasks) {
                        if (task.isSuccessful() && task.getResult() instanceof QuerySnapshot) {
                            QuerySnapshot snapshot = (QuerySnapshot) task.getResult();
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                if (doc instanceof QueryDocumentSnapshot && !foundIds.contains(doc.getId())) {
                                    foundIds.add(doc.getId());
                                    searchResults.add((QueryDocumentSnapshot) doc);
                                }
                            }
                        }
                    }

                    loadingProgress.setVisibility(View.GONE);
                    studentList.clear();
                    studentList.addAll(searchResults);
                    studentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Search", "Search failed", e);
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Search failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
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

    @Override
    protected void onResume() {
        super.onResume();
        fetchStudentsFromFirestore();
    }
}
