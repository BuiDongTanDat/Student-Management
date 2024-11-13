package com.example.gk09;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FireStoreHelper {

    private final FirebaseFirestore db;

    public FireStoreHelper() {
        db = FirebaseFirestore.getInstance();
    }


    // Check Login
    public void checkLogin(String email, String password, LoginCallback callback) {
        if (email == null || password == null) {
            callback.onFailure("Email or password cannot be empty.");
            return;
        }

        // Find the user by email through the document UID
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String storedPassword = document.getString("password");
                        Boolean status = document.getBoolean("status"); // Check the status field

                        // Check if the user is active
                        if (status != null && !status) {
                            callback.onFailure("Account is inactive.");
                            return;
                        }

                        // Check if the password matches (assuming password is stored in hashed form)
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Successful login, return the user UID
                            String uid = document.getId(); // The UID of the document (user)
                            callback.onSuccess(uid);
                        } else {
                            // Incorrect password
                            callback.onFailure("Incorrect password.");
                        }
                    } else {
                        // User not found
                        callback.onFailure("User  not found.");
                    }
                });
    }

    public interface LoginCallback {
        void onSuccess(String uid); // Returns the UID of the user upon successful login
        void onFailure(String errorMessage); // Returns error message if login fails
    }


    //Load data of user
    public void loadUser (String uid, FirestoreCallback callback) {
        if (uid != null) {
            db.collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    String name = document.getString("name");
                    String imageUrl = document.getString("image");
                    String email = document.getString("email");
                    String role = document.getString("role");
                    String password = document.getString("password");
                    String phone = document.getString("phone");
                    boolean status = document.getBoolean("status");
                    int age = document.getLong("age").intValue();

                    // Return data via callback
                    callback.onSuccess(new User(uid, name, email, password, phone, role, imageUrl, status, age));
                } else {
                    Log.w("DB_ERROR", "User  document does not exist.");
                    callback.onFailure("User  document does not exist.");
                }
            });
        } else {
            callback.onFailure("UID is null");
        }
    }

    public interface FirestoreCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }


    //Update user
    public void updateUser(String uid, User updatedUser , UpdateUserCallBack callback) {
        if (uid == null || updatedUser  == null) {
            callback.onFailure("User  data is invalid.");
            return;
        }
        // Update user data in Firestore
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.set(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User  data updated successfully.");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to update user data.", e);
                    callback.onFailure("Failed to update user data.");
                });
    }

    public interface UpdateUserCallBack {
        void onSuccess();
        void onFailure(String errorMessage);
    }


    //Save login history
    public void logLoginHistory(String uid) {
        if (uid != null) {
            DocumentReference historyRef = db.collection("history").document(uid);

            historyRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<String> logins = (List<String>) task.getResult().get("logins");

                    if (logins == null) {
                        // If no logins exist, create a new list
                        logins = new ArrayList<>();
                    }

                    // Add the current login time to the list
                    logins.add(getCurrentDateTime());

                    // Update the logins field in Firestore
                    historyRef.set(Collections.singletonMap("logins", logins), SetOptions.merge())
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Login history logged successfully."))
                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to log login history.", e));
                } else {
                    Log.e("Firestore", "Failed to retrieve login history.", task.getException());
                }
            });
        } else {
            Log.e("Firestore", "UID is null. Cannot log login history.");
        }
    }


    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    //Load login history
    public void loadLoginHistory(String uid, LoginHistoryCallback callback) {
        if (uid != null) {
            db.collection("history")
                    .document(uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            List<String> logins = (List<String>) task.getResult().get("logins");
                            callback.onSuccess(logins);
                        } else {
                            callback.onFailure("No login history found.");
                        }
                    });
        } else {
            callback.onFailure("UID is null.");
        }
    }

    public interface LoginHistoryCallback {
        void onSuccess(List<String> logins);
        void onFailure(String errorMessage);
    }


    //Create new user
    public void createUser(User newUser, CreateUserCallback callback) {
        if (newUser == null) {
            callback.onFailure("User data is invalid.");
            return;
        }

        // Tạo một UID mới từ Firestore
        String generatedUid = db.collection("users").document().getId();

        // Đặt UID này làm ID tài liệu cho người dùng mới
        db.collection("users")
                .document(generatedUid)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User created successfully");
                    newUser.setId(generatedUid); // Cập nhật UID vào đối tượng newUser
                    callback.onSuccess(generatedUid);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to create user.", e);
                    callback.onFailure("Failed to create user: " + e.getMessage());
                });
    }


    public interface CreateUserCallback {
        void onSuccess(String uid); // Returns the UID of the newly created user
        void onFailure(String errorMessage); // Returns error message if creation fails
    }


    //Check email
    public void checkEmailExists(String email, EmailExistsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            callback.onEmailExists(); // Email exists
                        } else {
                            callback.onEmailDoesNotExist(); // Email does not exist
                        }
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface EmailExistsCallback {
        void onEmailExists();
        void onEmailDoesNotExist();
        void onFailure(String errorMessage);
    }


    //Delete user include delete their history login
    public void deleteUser(String uid, DeleteUserCallback callback) {
        if (uid != null) {
            db.collection("users").document(uid).delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "User  deleted successfully.");
                        // Proceed to delete the login history
                        db.collection("history").document(uid).delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Log.d("Firestore", "Login history deleted successfully.");
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Failed to delete login history.", e);
                                    callback.onFailure("Failed to delete login history: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Failed to delete user.", e);
                        callback.onFailure("Failed to delete user: " + e.getMessage());
                    });
        } else {
            Log.e("Firestore", "UID is null. Cannot delete user.");
            callback.onFailure("UID is null. Cannot delete user.");
        }
    }

    // Callback interface for deleteUser
    public interface DeleteUserCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

}
