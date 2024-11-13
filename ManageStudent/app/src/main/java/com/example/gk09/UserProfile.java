package com.example.gk09;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;


public class UserProfile extends AppCompatActivity {

    TextView usernameUP, passwordUP, emailUP, ageUP, phoneUP, roleUP,tvStatusUP;
    Button btnLogout;
    ImageView avtUserUP;
    ProgressBar processBarUP;
    int EDIT_REQUEST = 1;
    User currUser;
    FireStoreHelper fireStoreHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.userdetail);



        fireStoreHelper = new FireStoreHelper();

        usernameUP = findViewById(R.id.usernameUP);
        passwordUP = findViewById(R.id.passwordUP);
        emailUP = findViewById(R.id.emailUP);
        ageUP = findViewById(R.id.ageUP);
        phoneUP = findViewById(R.id.phoneUP);
        roleUP = findViewById(R.id.roleUP);
        tvStatusUP = findViewById(R.id.tvStatusUP);
        btnLogout = findViewById(R.id.btnLogout);
        avtUserUP = findViewById(R.id.avtUserUP);
        processBarUP = findViewById(R.id.ProgressBarUP);



        // Get the user object from the intent
        Intent intent = getIntent();
        currUser = (User) intent.getSerializableExtra("user");

        Log.d("Pass", currUser.getPassword());

        if (currUser != null) {
            // Set the user details in the UI
            usernameUP.setText(currUser.getName());
            Log.d("Pass", currUser.getPassword());
            Log.d("Sttus", String.valueOf(currUser.isStatus()));

            passwordUP.setText(currUser.getPassword());
            emailUP.setText(currUser.getEmail());
            ageUP.setText(String.valueOf(currUser.getAge()));
            phoneUP.setText(currUser.getPhone());
            roleUP.setText(currUser.getRole());
            boolean status = currUser.isStatus();
            if (status) {
                tvStatusUP.setText("Account activated");
                tvStatusUP.setTextColor(getResources().getColor(R.color.green));
            } else {
                tvStatusUP.setText("Account not activated");
                tvStatusUP.setTextColor(getResources().getColor(R.color.red));
            }

            if(currUser.getImage() != null){
            Glide.with(UserProfile.this).load(currUser.getImage()).into(avtUserUP);}
            else{
                avtUserUP.setImageResource(R.drawable.avtdf);
            }
        }


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

                                //Remove Login session of current user
                                SharedPreferences sharedPreferences = getSharedPreferences("User Session", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("uid");
                                editor.apply();

                                // Chuyển về màn hình đăng nhập (MainActivity)
                                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mnEdit){
            Intent intent = new Intent(UserProfile.this, UserEdit.class);
            intent.putExtra("user", currUser);
            intent.putExtra("currRole", currUser.getRole());
            startActivityForResult(intent, EDIT_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_REQUEST && resultCode == RESULT_OK && data != null) {
            User updatedUser = (User ) data.getSerializableExtra("updatedUser");
            if (updatedUser != null) {
                // Update the UI with the new user data
                usernameUP.setText(updatedUser .getName());
                passwordUP.setText(updatedUser .getPassword()); // Be cautious with this
                emailUP.setText(updatedUser .getEmail());
                ageUP.setText(String.valueOf(updatedUser .getAge()));
                phoneUP.setText(updatedUser .getPhone());
                roleUP.setText(updatedUser .getRole());
                boolean status = updatedUser .isStatus();
                if (status) {
                    tvStatusUP.setText("Account activated");
                    tvStatusUP.setTextColor(getResources().getColor(R.color.green));
                } else {
                    tvStatusUP.setText("Account not activated");
                    tvStatusUP.setTextColor(getResources().getColor(R.color.red));
                }

                currUser  = updatedUser ; // Update the current user reference

                // Load updated image if available
                if (updatedUser.getImage() != null) {
                    Glide.with(UserProfile.this).load(updatedUser .getImage()).into(avtUserUP);
                } else {
                    avtUserUP.setImageResource(R.drawable.avtdf); // Default image
                }
            }
        }
    }

}