package com.example.gk09;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private List<QueryDocumentSnapshot> studentList;
    private StudentManage activity;

    public StudentAdapter(List<QueryDocumentSnapshot> studentList, StudentManage activity) {
        this.studentList = studentList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        try {
            QueryDocumentSnapshot document = studentList.get(position);

            String name = document.getString("name");
            holder.studentName.setText(name != null ? name : "No Name");

            String studentClass = document.getString("studentClass");
            holder.studentClass.setText(studentClass != null ? studentClass : "No Class");

            String email = document.getString("email");
            holder.studentEmail.setText(email != null ? email : "No Email");

            String imageUrl = document.getString("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.studentImage);
            } else {
                holder.studentImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(activity, UpdateStudent.class);
                intent.putExtra("studentId", document.getId());
                intent.putExtra("name", document.getString("name"));
                intent.putExtra("studentClass", document.getString("studentClass"));
                intent.putExtra("email", document.getString("email"));
                intent.putExtra("phone", document.getString("phone"));
                intent.putExtra("address", document.getString("address"));

                Long age = document.getLong("age");
                intent.putExtra("age", age != null ? age : 0L);  // Use Long instead of int

                activity.startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> {
                activity.showDeleteConfirmation(document.getId());
            });

        } catch (Exception e) {
            Log.e("StudentAdapter", "Error binding view holder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, studentClass, studentEmail;
        ImageView studentImage;
        ImageButton btnDelete;

        StudentViewHolder(View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            studentClass = itemView.findViewById(R.id.studentClass);
            studentEmail = itemView.findViewById(R.id.studentEmail);
            studentImage = itemView.findViewById(R.id.studentImage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
