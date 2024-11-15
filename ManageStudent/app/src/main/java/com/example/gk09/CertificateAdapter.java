package com.example.gk09;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {
    private static final String TAG = "CertificateAdapter";
    private List<QueryDocumentSnapshot> certificateList;
    private String studentId;
    private StudentDetails activity;
    private SimpleDateFormat dateFormat;
    private Context context;

    public CertificateAdapter(List<QueryDocumentSnapshot> certificateList, String studentId, StudentDetails activity, Context context) {
        this.certificateList = certificateList != null ? certificateList : new ArrayList<>();
        this.studentId = studentId;
        this.activity = activity;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Log.d(TAG, "Adapter created with " + this.certificateList.size() + " certificates");
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.certificate_item, parent, false);
        Log.d(TAG, "Created new ViewHolder");
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        try {
            QueryDocumentSnapshot document = certificateList.get(position);
            Log.d(TAG, "Binding position " + position + ": " + document.getData().toString());
            String certificateId = document.getId();

            // Set the certificate name
            String name = document.getString("name");
            holder.certificateName.setText(name != null ? name : "Unnamed Certificate");
            Log.d(TAG, "Set name: " + name);

            // Set the issued by
            String issuedBy = document.getString("issuedBy");
            holder.issuedBy.setText(issuedBy != null ? "Issued by: " + issuedBy : "");
            Log.d(TAG, "Set issuedBy: " + issuedBy);

            // Set the issue date
            if (document.getDate("issueDate") != null) {
                String dateText = "Issued: " + dateFormat.format(document.getDate("issueDate"));
                holder.issueDate.setText(dateText);
                holder.issueDate.setVisibility(View.VISIBLE);
                Log.d(TAG, "Set issueDate: " + dateText);
            } else {
                holder.issueDate.setVisibility(View.GONE);
                Log.d(TAG, "No issue date available");
            }

            // Handle edit button click
            holder.btnEdit.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(activity, UpdateCertificate.class);
                    intent.putExtra("studentId", studentId);
                    intent.putExtra("certificateId", certificateId);
                    intent.putExtra("name", name);
                    intent.putExtra("description", document.getString("description"));
                    intent.putExtra("issuedBy", issuedBy);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching update: " + e.getMessage());
                }
            });

            // Handle delete button click
            holder.btnDelete.setOnClickListener(v -> {
                if (activity != null) {
                    activity.showDeleteConfirmation(certificateId);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder: " + e.getMessage(), e);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("User Session", Context.MODE_PRIVATE);
        String role = sharedPreferences.getString("role", null);
        if (role != null && role.equals("employee")) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        int size = certificateList.size();
        Log.d(TAG, "getItemCount: " + size);
        return size;
    }

    static class CertificateViewHolder extends RecyclerView.ViewHolder {
        TextView certificateName, issuedBy, issueDate;
        ImageButton btnEdit, btnDelete;
        CardView cardView;

        CertificateViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            certificateName = itemView.findViewById(R.id.tvCertificateName);
            issuedBy = itemView.findViewById(R.id.tvIssuedBy);
            issueDate = itemView.findViewById(R.id.tvIssueDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Verify all views are found
            if (cardView == null) Log.e("CertificateAdapter", "cardView not found");
            if (certificateName == null) Log.e("CertificateAdapter", "certificateName not found");
            if (issuedBy == null) Log.e("CertificateAdapter", "issuedBy not found");
            if (issueDate == null) Log.e("CertificateAdapter", "issueDate not found");
            if (btnEdit == null) Log.e("CertificateAdapter", "btnEdit not found");
            if (btnDelete == null) Log.e("CertificateAdapter", "btnDelete not found");
        }
    }

    public void updateData(List<QueryDocumentSnapshot> newData) {
        Log.d(TAG, "Updating adapter data");
        if (newData != null) {
            this.certificateList = new ArrayList<>(newData);
            Log.d(TAG, "New data size: " + this.certificateList.size());
            for (QueryDocumentSnapshot doc : this.certificateList) {
                Log.d(TAG, "Certificate in list: " + doc.getId() + " - " + doc.getData());
            }
        } else {
            this.certificateList = new ArrayList<>();
            Log.d(TAG, "New data is null, clearing list");
        }
        notifyDataSetChanged();
        Log.d(TAG, "Notified adapter of data change");
    }
}