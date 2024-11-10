package com.example.gk09;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UserAdd extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 333;

    private ImageView addImageAvt;
    private EditText addName, addPass, addEmail, addAge, addPhone, addRole;
    private Switch switchAdd;
    private ProgressBar progressAdd;
    private Button btnSaveAddUser;
    private TextView addCamera;

    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private String[] roleList = {"manager", "employee"};
    private int selectedPlaceIndex = 0;

    private FireStoreHelper fireStoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userformforadd);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        addImageAvt = findViewById(R.id.addImageAvt);
        addName = findViewById(R.id.addName);
        addPass = findViewById(R.id.addPass);
        addEmail = findViewById(R.id.addEmail);
        addAge = findViewById(R.id.addAge);
        addPhone = findViewById(R.id.addPhone);
        addRole = findViewById(R.id.addRole);
        switchAdd = findViewById(R.id.swithAdd);
        progressAdd = findViewById(R.id.progressAdd);
        btnSaveAddUser = findViewById(R.id.btnSaveAddUser);
        addCamera = findViewById(R.id.addCamera);




        addRole.setOnClickListener(view -> showRoleSelectionDialog());
        addImageAvt.setOnClickListener(view -> openImageGallery());
        addCamera.setOnClickListener(view -> openImageGallery());

        fireStoreHelper = new FireStoreHelper();

        btnSaveAddUser.setOnClickListener(view -> {
            progressAdd.setVisibility(View.VISIBLE);
            btnSaveAddUser.setEnabled(false);

            String email = addEmail.getText().toString();

            // Check if the email exists before creating the User object
            fireStoreHelper.checkEmailExists(email, new FireStoreHelper.EmailExistsCallback() {
                @Override
                public void onEmailExists() {
                    progressAdd.setVisibility(View.GONE);
                    btnSaveAddUser.setEnabled(true);
                    Toast.makeText(UserAdd.this, "Email already exists. Please use a different email.", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onEmailDoesNotExist() {
                    User addUser  = getUser ();
                    if (addUser  != null) {
                        if (selectedImageUri != null) {
                            uploadAndSaveImage(addUser );
                        } else {
                            addUserInFirestore(addUser);
                        }
                    }
                    else {
                        progressAdd.setVisibility(View.GONE);
                        btnSaveAddUser.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressAdd.setVisibility(View.GONE);
                    btnSaveAddUser .setEnabled(true);
                    Toast.makeText(UserAdd.this, "Error checking email: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


    }

    private void uploadAndSaveImage(User addUser ) {
        ConfigCloudinary.initCloudinary(this);
        ConfigCloudinary.uploadImage(this, selectedImageUri, new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                progressAdd.setVisibility(View.VISIBLE);
                Toast.makeText(UserAdd.this, "Uploading...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {}

            @Override
            public void onSuccess(String requestId, Map resultData) {

                uploadedImageUrl = resultData.get("secure_url").toString();
                addUser.setImage(uploadedImageUrl);
                addUserInFirestore(addUser );
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressAdd.setVisibility(View.GONE);
                Toast.makeText(UserAdd.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {}
        });
    }

    private void addUserInFirestore(User addUser ) {
        fireStoreHelper.createUser(addUser , new FireStoreHelper.CreateUserCallback() {
            @Override
            public void onSuccess(String uid) {
                progressAdd.setVisibility(View.GONE);
                btnSaveAddUser.setEnabled(true);
                Toast.makeText(UserAdd.this, "User  added successfully", Toast.LENGTH_SHORT).show();
                // Optionally, clear the fields or navigate back
                clearFields();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressAdd.setVisibility(View.GONE);
                Toast.makeText(UserAdd.this, "Failed to add user: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearFields() {
        addName.setText("");
        addPass.setText("");
        addEmail.setText("");
        addAge.setText("");
        addPhone.setText("");
        addRole.setText("");
    }



    private void updateImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            addImageAvt.setImageResource(R.drawable.avtdf);
        }
        else{
        Glide.with(this).load(imageUrl).into(addImageAvt);}
    }

    private void showRoleSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a role");
        builder.setSingleChoiceItems(roleList, selectedPlaceIndex, (dialogInterface, i) -> {
            selectedPlaceIndex = i;
            addRole.setText(roleList[selectedPlaceIndex]);
            dialogInterface.dismiss();
            selectedPlaceIndex = 0;
        });
        builder.create().show();
    }

    private void openImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                updateImage(selectedImageUri.toString());
            }
        }
    }


    //Check validated all fields
    private User getUser() {
        User temp = new User();

        String name = addName.getText().toString();
        String password = addPass.getText().toString();
        String email = addEmail.getText().toString();
        String ageString = addAge.getText().toString();
        String phone = addPhone.getText().toString();
        String role = addRole.getText().toString();


        // Check for empty fields
        if (name.isEmpty() || email.isEmpty() || ageString.isEmpty() || phone.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            progressAdd.setVisibility(View.GONE);
            return null;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            progressAdd.setVisibility(View.GONE);
            return null;
        }

        // Validate password length (assuming the password is editable)
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            progressAdd.setVisibility(View.GONE);
            return null;
        }

        // Validate age
        int age;
        try {
            age = Integer.parseInt(ageString);
            if (age <= 0 || age > 100) {
                Toast.makeText(this, "Age must be between 1 and 100", Toast.LENGTH_SHORT).show();
                progressAdd.setVisibility(View.GONE);
                return null;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            progressAdd.setVisibility(View.GONE);
            return null;
        }

        // Set values to the User object
        temp.setId(null);
        temp.setName(name);
        temp.setPassword(password); // Assuming the password is not changed
        temp.setEmail(email);
        temp.setAge(age);
        temp.setPhone(phone);
        temp.setRole(role);
        temp.setStatus(switchAdd.isChecked());
        temp.setImage(uploadedImageUrl != null ? uploadedImageUrl : null);

        return temp;
    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // When the Up button is clicked, finish the current activity
                // This will take the user back to the previous activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
