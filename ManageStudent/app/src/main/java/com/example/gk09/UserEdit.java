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

import java.util.Map;

public class UserEdit extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 111;

    private ImageView edtUserAvatar;
    private EditText edtUserName, edtUserPassword, edtUserEmail, edtUserAge, edtUserPhone, edtUserRole;
    private Switch switchEditStatus;
    private ProgressBar progressBarUE;
    private Button btnSaveEditUser;
    private TextView tvCamera;

    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private User currUser;
    private String[] roleList = {"manager", "employee"};
    private int selectedPlaceIndex = 0;

    private FireStoreHelper fireStoreHelper;

    private String currRole;
    private String oldEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userform);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Get current user login role
        Intent intent = getIntent();
        currRole = intent.getStringExtra("currRole");


        getDataToField();
        loadCurrentUser();

        edtUserRole.setOnClickListener(view -> showRoleSelectionDialog());
        edtUserAvatar.setOnClickListener(view -> openImageGallery());
        tvCamera.setOnClickListener(view -> openImageGallery());

        fireStoreHelper = new FireStoreHelper();

        btnSaveEditUser.setOnClickListener(view -> {
            progressBarUE.setVisibility(View.VISIBLE);
            btnSaveEditUser.setEnabled(false);

            String email = edtUserEmail.getText().toString();

            // Check if the email is the same as the old email
            if (email.equals(oldEmail)) {
                User updatedUser = getUpdatedUser ();
                if (updatedUser != null) {
                    if (selectedImageUri != null) {
                        uploadAndSaveImage(updatedUser );
                    } else {
                        updateUserInFirestore(updatedUser );
                    }
                }
            } else {
                // If the email is different, check if it exists
                fireStoreHelper.checkEmailExists(email, new FireStoreHelper.EmailExistsCallback() {
                    @Override
                    public void onEmailExists() {
                        progressBarUE.setVisibility(View.GONE);
                        btnSaveEditUser .setEnabled(true);
                        Toast.makeText(UserEdit.this, "Email already exists. Please use a different email.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onEmailDoesNotExist() {
                        User updatedUser  = getUpdatedUser ();
                        if (updatedUser  != null) {
                            if (selectedImageUri != null) {
                                uploadAndSaveImage(updatedUser );
                            } else {
                                updateUserInFirestore(updatedUser );
                            }
                        }
                        else {
                            progressBarUE.setVisibility(View.GONE);
                            btnSaveEditUser.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressBarUE.setVisibility(View.GONE);
                        btnSaveEditUser .setEnabled(true);
                        Toast.makeText(UserEdit.this, "Error checking email: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    private void uploadAndSaveImage(User updatedUser ) {
        ConfigCloudinary.initCloudinary(this);
        ConfigCloudinary.uploadImage(this, selectedImageUri, new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                progressBarUE.setVisibility(View.VISIBLE);
                Toast.makeText(UserEdit.this, "Uploading...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {}

            @Override
            public void onSuccess(String requestId, Map resultData) {
                uploadedImageUrl = resultData.get("secure_url").toString();
                updatedUser.setImage(uploadedImageUrl);
                updateUserInFirestore(updatedUser );
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressBarUE.setVisibility(View.GONE);
                Toast.makeText(UserEdit.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {}
        });
    }

    private void updateUserInFirestore(User updatedUser ) {
        fireStoreHelper.updateUser(currUser.getId(), updatedUser , new FireStoreHelper.UpdateUserCallBack() {
            @Override
            public void onSuccess() {
                progressBarUE.setVisibility(View.GONE);
                Toast.makeText(UserEdit.this, "User  updated successfully", Toast.LENGTH_SHORT).show();

                // Set the result with the updated user object
                Intent returnIntent = new Intent();
                returnIntent.putExtra("updatedUser", updatedUser ); // Ensure no trailing spaces in the key
                setResult(RESULT_OK, returnIntent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBarUE.setVisibility(View.GONE);
                Toast.makeText(UserEdit.this, "Failed to update user: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getDataToField() {
        edtUserAvatar = findViewById(R.id.edtUserAvatar);
        edtUserName = findViewById(R.id.edtUserName);
        edtUserPassword = findViewById(R.id.edtUserPassword);
        edtUserEmail = findViewById(R.id.edtUserEmail);
        edtUserAge = findViewById(R.id.edtUserAge);
        edtUserPhone = findViewById(R.id.edtUserPhone);
        edtUserRole = findViewById(R.id.edtUserRole);
        switchEditStatus = findViewById(R.id.switchEditStatus);
        progressBarUE = findViewById(R.id.progressBarUE);
        btnSaveEditUser = findViewById(R.id.btnSaveEditUser);
        tvCamera = findViewById(R.id.tvCamera);
    }

    private void loadCurrentUser() {
        Intent intent = getIntent();
        currUser = (User) intent.getSerializableExtra("user");

        if (currUser != null) {
            progressBarUE.setVisibility(View.VISIBLE);
            edtUserName.setText(currUser.getName());
            edtUserPassword.setText(currUser.getPassword());
            edtUserEmail.setText(currUser.getEmail());
            edtUserAge.setText(String.valueOf(currUser.getAge()));
            edtUserPhone.setText(currUser.getPhone());
            edtUserRole.setText(currUser.getRole());
            switchEditStatus.setChecked(currUser.isStatus());
            updateImage(currUser.getImage());
            progressBarUE.setVisibility(View.GONE);

            oldEmail = currUser.getEmail();
        }

        // Check the current role and enable/disable fields accordingly
        if ("admin".equals(currRole)) {
            enableAllFields();

            //If admin is editing their profile
            if (currRole.equals(currUser.getRole())){
                switchEditStatus.setEnabled(false);
                edtUserRole.setEnabled(false);

            }
        } else {
            disableEditFields();
        }


    }

    private void enableAllFields() {
        edtUserName.setEnabled(true);
        edtUserPassword.setEnabled(true);
        edtUserEmail.setEnabled(true);
        edtUserAge.setEnabled(true);
        edtUserPhone.setEnabled(true);
        edtUserRole.setEnabled(true);
        switchEditStatus.setEnabled(true);

    }

    private void disableEditFields() {
        edtUserName.setEnabled(false);
        edtUserPassword.setEnabled(false);
        edtUserEmail.setEnabled(false);
        edtUserAge.setEnabled(false);
        edtUserPhone.setEnabled(false);
        edtUserRole.setEnabled(false);
        switchEditStatus.setEnabled(false);
    }

    private void updateImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            edtUserAvatar.setImageResource(R.drawable.avtdf);
        }
        else{
        Glide.with(this).load(imageUrl).into(edtUserAvatar);}
    }

    private void showRoleSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a role");
        builder.setSingleChoiceItems(roleList, selectedPlaceIndex, (dialogInterface, i) -> {
            selectedPlaceIndex = i;
            edtUserRole.setText(roleList[selectedPlaceIndex]);
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
    private User getUpdatedUser() {
        User temp = new User();

        String name = edtUserName.getText().toString();
        String password = edtUserPassword.getText().toString();
        String email = edtUserEmail.getText().toString();
        String ageString = edtUserAge.getText().toString();
        String phone = edtUserPhone.getText().toString();
        String role = edtUserRole.getText().toString();



        // Check for empty fields
        if (name.isEmpty() || email.isEmpty() || ageString.isEmpty() || phone.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            progressBarUE.setVisibility(View.GONE);
            return null;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            progressBarUE.setVisibility(View.GONE);
            return null;
        }

        // Validate password length (assuming the password is editable)
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            progressBarUE.setVisibility(View.GONE);
            return null;
        }

        // Validate age
        int age;
        try {
            age = Integer.parseInt(ageString);
            if (age <= 0 || age > 100) {
                Toast.makeText(this, "Age must be between 1 and 100", Toast.LENGTH_SHORT).show();
                progressBarUE.setVisibility(View.GONE);
                return null;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            progressBarUE.setVisibility(View.GONE);
            return null;
        }

        temp.setId(currUser.getId());
        temp.setName(edtUserName.getText().toString());
        temp.setPassword(currUser.getPassword());
        temp.setEmail(edtUserEmail.getText().toString());
        temp.setAge(Integer.parseInt(edtUserAge.getText().toString()));
        temp.setPhone(edtUserPhone.getText().toString());
        temp.setRole(edtUserRole.getText().toString());
        temp.setStatus(switchEditStatus.isChecked());
        temp.setImage(uploadedImageUrl != null ? uploadedImageUrl : currUser.getImage());

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
