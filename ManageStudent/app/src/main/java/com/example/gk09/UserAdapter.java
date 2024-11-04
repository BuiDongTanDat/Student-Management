package com.example.gk09;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<QueryDocumentSnapshot> userList;
    private Context context;

    public UserAdapter(List<QueryDocumentSnapshot> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        QueryDocumentSnapshot document = userList.get(position);
        User user = document.toObject(User.class);

        holder.tvNameUser.setText(user.getName());
        holder.tvStatus.setText(user.isStatus() ? "Active" : "Inactive");

        Picasso.get().load(user.getImage()).into(holder.imageViewUser);

        // Handle button clicks (example)
        holder.btnViewHistory.setOnClickListener(v -> {
            // Handle view history action
        });
        holder.btnEditUser.setOnClickListener(v -> {
            // Handle edit user action
        });
        holder.btnTrashUser.setOnClickListener(v -> {
            // Handle delete user action
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameUser, tvStatus;
        ImageView imageViewUser;
        ImageButton btnViewHistory, btnEditUser, btnTrashUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameUser = itemView.findViewById(R.id.tvNameUser);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imageViewUser = itemView.findViewById(R.id.avtUserItem);
            btnViewHistory = itemView.findViewById(R.id.btnViewHistory);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnTrashUser = itemView.findViewById(R.id.btnTrashUser);
        }
    }
}
