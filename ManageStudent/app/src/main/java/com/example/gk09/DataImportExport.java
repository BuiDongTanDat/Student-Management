package com.example.gk09;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataImportExport {
    private static final String TAG = "DataImportExport";
    private final Context context;
    private final FirebaseFirestore db;
    private final SimpleDateFormat dateFormat;
    private final File exportDir;

    public DataImportExport(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());

        // Create export directory in app's external files directory
        this.exportDir = new File(context.getExternalFilesDir(null), "exports");
        if (!this.exportDir.exists()) {
            this.exportDir.mkdirs();
        }
    }

    public void importStudents(Uri fileUri, ImportCallback callback) {
        if (!checkConnectivity()) {
            callback.onError("No internet connection. Please check your network and try again.");
            return;
        }

        try {
            CSVReader reader = new CSVReader(new InputStreamReader(
                    context.getContentResolver().openInputStream(fileUri)));
            List<String[]> allRows = reader.readAll();

            if (allRows.size() <= 1) {
                callback.onError("CSV file is empty or contains only headers");
                return;
            }

            String[] headers = allRows.get(0);
            Log.d(TAG, "CSV Headers (" + headers.length + " columns): " + String.join(", ", headers));

            // Group rows by student data to handle multiple certificates
            Map<String, StudentImportData> studentMap = new HashMap<>();

            // Skip header row and process all rows
            for (int i = 1; i < allRows.size(); i++) {
                String[] rowData = allRows.get(i);
                Log.d(TAG, "Processing row " + i + " with " + rowData.length + " columns");

                if (rowData.length < 6) {
                    Log.w(TAG, "Skipping invalid row " + i + ": insufficient columns");
                    continue;
                }

                // Create unique key for student based on their data
                String studentKey = String.format("%s_%s_%s",
                        rowData[0].trim(), // name
                        rowData[2].trim(), // email
                        rowData[3].trim()  // phone
                );

                StudentImportData studentData = studentMap.computeIfAbsent(studentKey, k -> {
                    StudentImportData newStudent = new StudentImportData();
                    newStudent.studentData = createStudentMap(rowData);
                    newStudent.certificates = new ArrayList<>();
                    return newStudent;
                });

                // If there's certificate data, add it to the student's certificate list
                if (rowData.length >= 11 && !rowData[6].trim().isEmpty()) {
                    Map<String, Object> certificate = createCertificateMap(rowData);
                    if (certificate != null) {
                        studentData.certificates.add(certificate);
                    }
                }
            }
            reader.close();

            // Now import the grouped data
            importGroupedStudentData(studentMap, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error importing students: " + e.getMessage());
            callback.onError("Error importing students: " + e.getMessage());
        }
    }

    private void importGroupedStudentData(Map<String, StudentImportData> studentMap, ImportCallback callback) {
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger processedCount = new AtomicInteger(0);
        int totalStudents = studentMap.size();

        for (StudentImportData studentData : studentMap.values()) {
            db.collection("students")
                    .add(studentData.studentData)
                    .addOnSuccessListener(studentRef -> {
                        // Add all certificates for this student
                        if (!studentData.certificates.isEmpty()) {
                            addCertificatesForStudent(studentRef, studentData.certificates);
                        }

                        successCount.incrementAndGet();
                        if (processedCount.incrementAndGet() == totalStudents) {
                            callback.onSuccess("Imported " + successCount.get() + " students");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding student: " + e.getMessage());
                        if (processedCount.incrementAndGet() == totalStudents) {
                            callback.onSuccess("Imported " + successCount.get() + " students");
                        }
                    });
        }
    }

    private Map<String, Object> createStudentMap(String[] rowData) {
        Map<String, Object> student = new HashMap<>();
        student.put("name", rowData[0].trim());
        student.put("nameSearch", rowData[0].trim().toLowerCase());
        student.put("studentClass", rowData[1].trim());
        student.put("classSearch", rowData[1].trim().toLowerCase());
        student.put("email", rowData[2].trim());
        student.put("emailSearch", rowData[2].trim().toLowerCase());
        student.put("phone", rowData[3].trim());
        try {
            student.put("age", Integer.parseInt(rowData[4].trim()));
        } catch (NumberFormatException e) {
            student.put("age", 0);
        }
        student.put("address", rowData[5].trim());
        student.put("imageUrl", "");
        student.put("timestamp", new Date());
        return student;
    }

    private Map<String, Object> createCertificateMap(String[] rowData) {
        try {
            Map<String, Object> certificate = new HashMap<>();
            certificate.put("name", rowData[6].trim());
            certificate.put("description", rowData[7].trim());
            certificate.put("issuedBy", rowData[8].trim());

            SimpleDateFormat csvDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date issueDate = csvDateFormat.parse(rowData[9].trim());
            certificate.put("issueDate", issueDate);

            if (rowData.length > 10 && !rowData[10].trim().isEmpty()) {
                Date expiryDate = csvDateFormat.parse(rowData[10].trim());
                certificate.put("expiryDate", expiryDate);
            }
            certificate.put("timestamp", new Date());
            return certificate;
        } catch (Exception e) {
            Log.e(TAG, "Error creating certificate map: " + e.getMessage());
            return null;
        }
    }

    private void addCertificatesForStudent(DocumentReference studentRef,
                                           List<Map<String, Object>> certificates) {
        for (Map<String, Object> certificate : certificates) {
            studentRef.collection("certificates")
                    .add(certificate)
                    .addOnSuccessListener(certRef ->
                            Log.d(TAG, "Added certificate " + certRef.getId()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error adding certificate: " + e.getMessage()));
        }
    }

    private static class StudentImportData {
        Map<String, Object> studentData;
        List<Map<String, Object>> certificates;
    }

    private boolean checkConnectivity() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    // Export students to CSV
    public void exportStudents(ExportCallback callback) {
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onError("No students found to export");
                        return;
                    }

                    try {
                        String fileName = "students_" + dateFormat.format(new Date()) + ".csv";
                        File file = new File(exportDir, fileName);
                        CSVWriter writer = new CSVWriter(new FileWriter(file));

                        // Write header with certificate columns
                        String[] header = {"Name", "Class", "Email", "Phone", "Age", "Address",
                                "CertificateName", "Description", "IssuedBy", "IssueDate", "ExpiryDate"};
                        writer.writeNext(header);

                        // Counter for processing
                        AtomicInteger processedStudents = new AtomicInteger(0);
                        int totalStudents = queryDocumentSnapshots.size();

                        for (DocumentSnapshot studentDoc : queryDocumentSnapshots) {
                            // Get student data
                            String name = studentDoc.getString("name");
                            String studentClass = studentDoc.getString("studentClass");
                            String email = studentDoc.getString("email");
                            String phone = studentDoc.getString("phone");
                            String age = String.valueOf(studentDoc.getLong("age"));
                            String address = studentDoc.getString("address");

                            // Get certificates for this student
                            studentDoc.getReference().collection("certificates")
                                    .get()
                                    .addOnSuccessListener(certificatesSnapshot -> {
                                        try {
                                            if (certificatesSnapshot.isEmpty()) {
                                                // Write student data without certificates
                                                String[] row = {name, studentClass, email, phone, age, address, "", "", "", "", ""};
                                                writer.writeNext(row);
                                            } else {
                                                // Write a row for each certificate
                                                for (DocumentSnapshot certDoc : certificatesSnapshot) {
                                                    String[] row = new String[11];
                                                    // Student data
                                                    row[0] = name;
                                                    row[1] = studentClass;
                                                    row[2] = email;
                                                    row[3] = phone;
                                                    row[4] = age;
                                                    row[5] = address;

                                                    // Certificate data
                                                    row[6] = certDoc.getString("name");
                                                    row[7] = certDoc.getString("description");
                                                    row[8] = certDoc.getString("issuedBy");

                                                    SimpleDateFormat csvDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                                    if (certDoc.getDate("issueDate") != null) {
                                                        row[9] = csvDateFormat.format(certDoc.getDate("issueDate"));
                                                    }
                                                    if (certDoc.getDate("expiryDate") != null) {
                                                        row[10] = csvDateFormat.format(certDoc.getDate("expiryDate"));
                                                    }

                                                    writer.writeNext(row);
                                                }
                                            }

                                            // Check if all students have been processed
                                            if (processedStudents.incrementAndGet() == totalStudents) {
                                                writer.close();
                                                callback.onSuccess(file.getAbsolutePath());
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error writing student data: " + e.getMessage());
                                            callback.onError("Error writing data: " + e.getMessage());
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error getting certificates: " + e.getMessage());
                                        callback.onError("Error getting certificates: " + e.getMessage());
                                    });
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error creating CSV file", e);
                        callback.onError("Error creating CSV file: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting students", e);
                    callback.onError("Error getting students: " + e.getMessage());
                });
    }

    // Import certificates from CSV
    public void importCertificates(String studentId, Uri fileUri, ImportCallback callback) {
        // First verify the student exists
        db.collection("students").document(studentId).get()
                .addOnSuccessListener(studentDoc -> {
                    if (!studentDoc.exists()) {
                        callback.onError("Student not found!");
                        return;
                    }

                    try {
                        InputStreamReader inputReader = new InputStreamReader(
                                context.getContentResolver().openInputStream(fileUri));
                        CSVReader reader = new CSVReader(inputReader);
                        List<String[]> allRows = reader.readAll();

                        if (allRows.size() <= 1) {
                            callback.onError("CSV file is empty or contains only headers");
                            return;
                        }

                        Log.d(TAG, "Starting certificate import for student: " + studentId);
                        Log.d(TAG, "CSV contains " + (allRows.size() - 1) + " certificates");

                        AtomicInteger successCount = new AtomicInteger(0);
                        int totalRows = allRows.size() - 1;

                        // Skip header row
                        for (int i = 1; i < allRows.size(); i++) {
                            String[] row = allRows.get(i);
                            if (row.length < 4) {
                                Log.w(TAG, "Skipping invalid row " + i + ": insufficient columns");
                                continue;
                            }

                            Map<String, Object> certificate = new HashMap<>();
                            certificate.put("name", row[0]);
                            certificate.put("description", row[1]);
                            certificate.put("issuedBy", row[2]);

                            try {
                                Date issueDate = dateFormat.parse(row[3]);
                                certificate.put("issueDate", issueDate);
                                if (row.length > 4 && !row[4].isEmpty()) {
                                    Date expiryDate = dateFormat.parse(row[4]);
                                    certificate.put("expiryDate", expiryDate);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing date in row " + i + ": " + e.getMessage());
                                continue;
                            }

                            certificate.put("timestamp", new Date());

                            // Debug log the certificate data
                            Log.d(TAG, "Adding certificate: " + certificate);

                            final int currentRow = i;
                            db.collection("students")
                                    .document(studentId)
                                    .collection("certificates")
                                    .add(certificate)
                                    .addOnSuccessListener(docRef -> {
                                        Log.d(TAG, "Certificate added with ID: " + docRef.getId());
                                        int count = successCount.incrementAndGet();
                                        if (currentRow == allRows.size() - 1) {
                                            callback.onSuccess("Imported " + count + " out of " + totalRows + " certificates");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error adding certificate: " + e.getMessage());
                                        if (currentRow == allRows.size() - 1) {
                                            callback.onSuccess("Imported " + successCount.get() + " out of " + totalRows + " certificates");
                                        }
                                    });
                        }

                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error importing certificates: " + e.getMessage());
                        callback.onError("Error importing certificates: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError("Error verifying student: " + e.getMessage());
                });
    }

    // Export certificates to CSV
    public void exportCertificates(String studentId, ExportCallback callback) {
        db.collection("students")
                .document(studentId)
                .collection("certificates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        String fileName = "certificates_" + studentId + "_" +
                                dateFormat.format(new Date()) + ".csv";
                        File file = new File(exportDir, fileName);
                        CSVWriter writer = new CSVWriter(new FileWriter(file));

                        // Write header
                        String[] header = {"Name", "Description", "Issued By", "Issue Date", "Expiry Date"};
                        writer.writeNext(header);

                        // Write data
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String[] row = {
                                    document.getString("name"),
                                    document.getString("description"),
                                    document.getString("issuedBy"),
                                    dateFormat.format(document.getDate("issueDate")),
                                    document.getDate("expiryDate") != null ?
                                            dateFormat.format(document.getDate("expiryDate")) : ""
                            };
                            writer.writeNext(row);
                        }
                        writer.close();
                        callback.onSuccess(file.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e(TAG, "Error exporting certificates", e);
                        callback.onError("Error exporting certificates: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting certificates", e);
                    callback.onError("Error getting certificates: " + e.getMessage());
                });
    }

    // Callback interfaces
    public interface ImportCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }
}
