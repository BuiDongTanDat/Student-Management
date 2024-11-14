package com.example.gk09;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataImportExport {
    private static final String TAG = "DataImportExport";
    private final Context context;
    private final FirebaseFirestore db;
    private final SimpleDateFormat dateFormat;

    public DataImportExport(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    }

    // Import students from CSV
    public void importStudents(Uri fileUri, ImportCallback callback) {
        try {
            InputStreamReader inputReader = new InputStreamReader(
                    context.getContentResolver().openInputStream(fileUri));
            CSVReader reader = new CSVReader(inputReader);
            List<String[]> allRows = reader.readAll();

            // Skip header row
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                Map<String, Object> student = new HashMap<>();
                student.put("name", row[0]);
                student.put("nameSearch", row[0].toLowerCase());
                student.put("studentClass", row[1]);
                student.put("classSearch", row[1].toLowerCase());
                student.put("email", row[2]);
                student.put("emailSearch", row[2].toLowerCase());
                student.put("phone", row[3]);
                student.put("age", Integer.parseInt(row[4]));
                student.put("address", row[5]);
                student.put("imageUrl", "");
                student.put("timestamp", new Date());

                // Add to Firestore
                db.collection("students")
                        .add(student)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Student imported with ID: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error importing student", e);
                        });
            }
            reader.close();
            callback.onSuccess("Students imported successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error importing students", e);
            callback.onError("Error importing students: " + e.getMessage());
        }
    }

    // Export students to CSV
    public void exportStudents(ExportCallback callback) {
        db.collection("students")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "exports");
                        if (!exportDir.exists()) {
                            exportDir.mkdirs();
                        }

                        String fileName = "students_" + dateFormat.format(new Date()) + ".csv";
                        File file = new File(exportDir, fileName);
                        CSVWriter writer = new CSVWriter(new FileWriter(file));

                        // Write header
                        String[] header = {"Name", "Class", "Email", "Phone", "Age", "Address"};
                        writer.writeNext(header);

                        // Write data
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String[] row = {
                                    document.getString("name"),
                                    document.getString("studentClass"),
                                    document.getString("email"),
                                    document.getString("phone"),
                                    String.valueOf(document.getLong("age")),
                                    document.getString("address")
                            };
                            writer.writeNext(row);
                        }
                        writer.close();
                        callback.onSuccess(file.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e(TAG, "Error exporting students", e);
                        callback.onError("Error exporting students: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting students", e);
                    callback.onError("Error getting students: " + e.getMessage());
                });
    }

    // Import certificates from CSV
    public void importCertificates(String studentId, Uri fileUri, ImportCallback callback) {
        try {
            InputStreamReader inputReader = new InputStreamReader(
                    context.getContentResolver().openInputStream(fileUri));
            CSVReader reader = new CSVReader(inputReader);
            List<String[]> allRows = reader.readAll();

            // Skip header row
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                Map<String, Object> certificate = new HashMap<>();
                certificate.put("name", row[0]);
                certificate.put("description", row[1]);
                certificate.put("issuedBy", row[2]);
                certificate.put("issueDate", dateFormat.parse(row[3]));
                if (row.length > 4 && !row[4].isEmpty()) {
                    certificate.put("expiryDate", dateFormat.parse(row[4]));
                }

                // Add to Firestore
                db.collection("students")
                        .document(studentId)
                        .collection("certificates")
                        .add(certificate)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Certificate imported with ID: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error importing certificate", e);
                        });
            }
            reader.close();
            callback.onSuccess("Certificates imported successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error importing certificates", e);
            callback.onError("Error importing certificates: " + e.getMessage());
        }
    }

    // Export certificates to CSV
    public void exportCertificates(String studentId, ExportCallback callback) {
        db.collection("students")
                .document(studentId)
                .collection("certificates")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "exports");
                        if (!exportDir.exists()) {
                            exportDir.mkdirs();
                        }

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
