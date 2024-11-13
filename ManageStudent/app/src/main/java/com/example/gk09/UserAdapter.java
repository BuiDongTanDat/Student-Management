package com.example.gk09;

import android.app.Activity;
import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    FireStoreHelper fireStoreHelper = new FireStoreHelper();
    private String currRole;

    public UserAdapter(List<User> userList, Context context, String currRole) {
        this.userList = userList;
        this.context = context;
        this.currRole = currRole;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        Log.d("UserAdapter", "Binding user: " + user.getId());
        holder.tvNameUser.setText(user.getName());
        holder.tvRole.setText(user.getRole());

        if (user.isStatus()){
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.green));
        }else {
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

        if(user.getImage() != null) {
            Glide.with(context).load(user.getImage()).into(holder.imageViewUser);
        } else {
            holder.imageViewUser.setImageResource(R.drawable.avtdf);
        }
        // Handle button clicks (example)
        holder.btnViewHistory.setOnClickListener(v -> {
            fireStoreHelper.loadLoginHistory(user.getId(), new FireStoreHelper.LoginHistoryCallback() {
                @Override
                public void onSuccess(List<String> logins) {
                    // Show AlertDialog with login history
                    showLoginHistoryDialog(logins);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        holder.btnEditUser.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserEdit.class);
            intent.putExtra("user", user);
            intent.putExtra("currRole", currRole);
            ((Activity) context).startActivityForResult(intent, 222);
        });


        holder.btnTrashUser .setOnClickListener(v -> {
            // Show confirmation dialog before deletion
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        fireStoreHelper.deleteUser (user.getId(), new FireStoreHelper.DeleteUserCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("DeleteUser  ", "User  and login history deleted successfully.");
                                // Remove the user from the list and notify the adapter
                                userList.remove(position);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Log.e("DeleteUser", "Error deleting user: " + errorMessage);
                                Toast.makeText(context, "Error deleting user: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserView.class);
            intent.putExtra("userForView", user);
            context.startActivity(intent);
        });

        if (!currRole.equals("admin")) {
            holder.btnEditUser.setVisibility(View.GONE);
            holder.btnTrashUser.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameUser, tvRole, tvStatus;
        ImageView imageViewUser;
        ImageButton btnViewHistory, btnEditUser, btnTrashUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameUser = itemView.findViewById(R.id.tvNameUser);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRole = itemView.findViewById(R.id.tvRole);
            imageViewUser = itemView.findViewById(R.id.avtUserItem);
            btnViewHistory = itemView.findViewById(R.id.btnViewHistory);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnTrashUser = itemView.findViewById(R.id.btnTrashUser);
        }
    }

    private void showLoginHistoryDialog(List<String> logins) {
        StringBuilder historyBuilder = new StringBuilder();
        for (String login : logins) {
            historyBuilder.append(login).append("\n");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Login History")
                .setMessage(historyBuilder.toString().trim())
                .setPositiveButton("CLOSE", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
