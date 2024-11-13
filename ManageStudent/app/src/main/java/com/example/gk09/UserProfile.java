package com.example.gk09;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class UserProfile extends AppCompatActivity {

    TextView usernameUP, passwordUP, emailUP, ageUP, phoneUP, roleUP,tvStatusUP;
    Button btnLogout;
    ImageView avtUserUP;
    int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.userdetail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usernameUP = findViewById(R.id.usernameUP);
        passwordUP = findViewById(R.id.passwordUP);
        emailUP = findViewById(R.id.emailUP);
        ageUP = findViewById(R.id.ageUP);
        phoneUP = findViewById(R.id.phoneUP);
        roleUP = findViewById(R.id.roleUP);
        tvStatusUP = findViewById(R.id.tvStatusUP);
        btnLogout = findViewById(R.id.btnLogout);
        avtUserUP = findViewById(R.id.avtUserUP);



        // Get the user object from the intent
        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        Log.d("USER", user != null ? user.getName() : "No user logged in");
        if (user != null) {
            // Set the user details in the UI
            usernameUP.setText(user.getName().toString());
            passwordUP.setText(user.getPassword());
            emailUP.setText(user.getEmail());
            ageUP.setText(String.valueOf(user.getAge()));
            phoneUP.setText(user.getPhone());
            roleUP.setText(user.getRole());
            boolean status = user.isStatus();
            if (status) {
                tvStatusUP.setText("Account activated");
                tvStatusUP.setTextColor(getResources().getColor(R.color.green));
            } else {
                tvStatusUP.setText("Account not activated");
                tvStatusUP.setTextColor(getResources().getColor(R.color.red));
            }

            Picasso.get().load(user.getImage()).into(avtUserUP);
        }

        avtUserUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Mở thư viện ảnh
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hiển thị hộp thoại xác nhận đăng xuất
                new AlertDialog.Builder(UserProfile.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Đăng xuất người dùng khỏi Firebase
                                FirebaseAuth.getInstance().signOut();

                                // Chuyển về màn hình đăng nhập (MainActivity)
                                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish(); // Đóng UserProfile Activity
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Hiển thị ảnh đã chọn
                Picasso.get().load(selectedImageUri).into(avtUserUP);
                // Tải ảnh lên Firebase Storage
                uploadImageToFirebaseStorage(selectedImageUri);
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        if (imageUri != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("user/" + uid + "/image");

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Lấy URL của ảnh sau khi tải lên thành công
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Lưu URL vào Firebase Realtime Database
                            saveImageUrlToFirebase(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UserProfile.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveImageUrlToFirebase(String imageUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("image")
                .setValue(imageUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserProfile.this, "Image updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfile.this, "Failed to update image", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}