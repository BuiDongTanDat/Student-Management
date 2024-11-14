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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {
    private static final String TAG = "CertificateAdapter";
    private List<QueryDocumentSnapshot> certificateList;
    private StudentDetails activity;
    private SimpleDateFormat dateFormat;
    private Context context;

    public CertificateAdapter(List<QueryDocumentSnapshot> certificateList, StudentDetails activity, Context context) {
        this.certificateList = certificateList;
        this.activity = activity;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.context = context;
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.certificate_item, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        try {
            QueryDocumentSnapshot document = certificateList.get(position);
            Certificate certificate = document.toObject(Certificate.class);

            holder.tvCertificateName.setText(certificate.getName());
            holder.tvIssuedBy.setText(certificate.getIssuedBy());
            if (certificate.getIssueDate() != null) {
                holder.tvIssueDate.setText(dateFormat.format(certificate.getIssueDate()));
            }


            // Get role from SharedPreferences (if needed)
            SharedPreferences sharedPreferences = context.getSharedPreferences("User Session", Context.MODE_PRIVATE);
            String role = sharedPreferences.getString("role", null);

            if (role != null) {
                Log.d(TAG, "Role từ SharedPreferences: " + role);
                if ("employee".equals(role)) {
                    holder.btnEdit.setVisibility(View.GONE);
                    holder.btnDelete.setVisibility(View.GONE);
                }
            }

            // Edit button click handler
            holder.btnEdit.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(activity, UpdateCertificate.class);
                    intent.putExtra("certificateId", document.getId());
                    intent.putExtra("studentId", activity.getStudentId());
                    activity.startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching edit: " + e.getMessage());
                    Toast.makeText(activity, "Error editing certificate", Toast.LENGTH_SHORT).show();
                }
            });

            // Delete button click handler
            holder.btnDelete.setOnClickListener(v -> {
                if (activity != null) {
                    activity.deleteCertificate(document.getId());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return certificateList != null ? certificateList.size() : 0;
    }

    static class CertificateViewHolder extends RecyclerView.ViewHolder {
        TextView tvCertificateName, tvIssuedBy, tvIssueDate;
        ImageButton btnEdit, btnDelete;

        CertificateViewHolder(View itemView) {
            super(itemView);
            tvCertificateName = itemView.findViewById(R.id.tvCertificateName);
            tvIssuedBy = itemView.findViewById(R.id.tvIssuedBy);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
