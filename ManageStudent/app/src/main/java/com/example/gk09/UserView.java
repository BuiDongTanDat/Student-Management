package com.example.gk09;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class UserView extends AppCompatActivity {

    TextView viewName, viewPass, viewMail, viewAge, viewPhone, viewRole, viewStatus;
    ImageView viewImageAvt;
    ProgressBar viewProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.userformforview);

        viewName = findViewById(R.id.viewName);
        viewPass = findViewById(R.id.viewPass);
        viewMail = findViewById(R.id.viewMail);
        viewAge = findViewById(R.id.viewAge);
        viewPhone = findViewById(R.id.viewPhone);
        viewRole = findViewById(R.id.viewRole);
        viewStatus = findViewById(R.id.viewStatus);
        viewImageAvt = findViewById(R.id.viewImageAvt);
        viewProgress = findViewById(R.id.viewProgress);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("userForView");
        if (user == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            Log.d("UserDetail", "User not found");
            finish();
            return;
        }
        viewName.setText(user.getName());
        viewPass.setText(user.getPassword());
        viewMail.setText(user.getEmail());
        viewAge.setText(String.valueOf(user.getAge()));
        viewPhone.setText(user.getPhone());
        viewRole.setText(user.getRole());
        boolean status = user.isStatus();
        if (status) {
            viewStatus.setText("Account activated");
            viewStatus.setTextColor(getResources().getColor(R.color.green));
        } else {
            viewStatus.setText("Account not activated");
            viewStatus.setTextColor(getResources().getColor(R.color.red));
        }

        if(user.getImage() != null){
            Glide.with(UserView.this).load(user.getImage()).into(viewImageAvt);}
        else{
            viewImageAvt.setImageResource(R.drawable.avtdf);
        }
        viewProgress.setVisibility(View.GONE);


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
