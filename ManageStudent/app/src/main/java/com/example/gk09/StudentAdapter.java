package com.example.gk09;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private static final String TAG = "StudentAdapter";
    private List<QueryDocumentSnapshot> studentList;
    private StudentManage activity;

    public StudentAdapter(List<QueryDocumentSnapshot> studentList, StudentManage activity) {
        this.studentList = studentList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        try {
            QueryDocumentSnapshot document = studentList.get(position);

            String name = document.getString("name");
            String studentClass = document.getString("studentClass");
            String email = document.getString("email");
            String imageUrl = document.getString("imageUrl");

            // Set basic info
            holder.studentName.setText(name != null ? name : "No Name");
            holder.studentClass.setText(studentClass != null ? studentClass : "No Class");
            holder.studentEmail.setText(email != null ? email : "No Email");

            // Handle image
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.studentImage);
            } else {
                holder.studentImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Item click for view details
            holder.itemView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(activity, StudentDetails.class);
                    intent.putExtra("studentId", document.getId());
                    intent.putExtra("name", name);
                    intent.putExtra("studentClass", studentClass);
                    intent.putExtra("email", email);
                    intent.putExtra("phone", document.getString("phone"));
                    intent.putExtra("address", document.getString("address"));
                    if (document.contains("age")) {
                        intent.putExtra("age", document.getLong("age"));
                    }
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening details: " + e.getMessage());
                    Toast.makeText(activity, "Error viewing student details", Toast.LENGTH_SHORT).show();
                }
            });

            // Handle edit button click
            holder.btnEdit.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(activity, UpdateStudent.class);
                    intent.putExtra("studentId", document.getId());
                    intent.putExtra("name", name);
                    intent.putExtra("studentClass", studentClass);
                    intent.putExtra("email", email);
                    intent.putExtra("phone", document.getString("phone"));
                    intent.putExtra("address", document.getString("address"));
                    if (document.contains("age")) {
                        intent.putExtra("age", document.getLong("age"));
                    }
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching edit: " + e.getMessage());
                    Toast.makeText(activity, "Error editing student", Toast.LENGTH_SHORT).show();
                }
            });

            // Handle delete button click
            holder.btnDelete.setOnClickListener(v -> {
                if (activity != null) {
                    activity.showDeleteConfirmation(document.getId());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, studentClass, studentEmail;
        ImageView studentImage;
        ImageButton btnEdit, btnDelete;

        StudentViewHolder(View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            studentClass = itemView.findViewById(R.id.studentClass);
            studentEmail = itemView.findViewById(R.id.studentEmail);
            studentImage = itemView.findViewById(R.id.studentImage);
            btnEdit = itemView.findViewById(R.id.btnEdit);  // Make sure this ID matches your layout
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

