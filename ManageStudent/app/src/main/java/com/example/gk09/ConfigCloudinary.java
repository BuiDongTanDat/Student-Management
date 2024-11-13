package com.example.gk09;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ConfigCloudinary {

    private static boolean isInitialized = false;

    private static final String CLOUD_NAME = "studentmanagementapk";
    private static final String API_KEY = "255386992642812";
    private static final String API_SECRET = "J9do7fA6VVEVF4ylb8sjIMMA9Q8";

    public static void initCloudinary(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);
            MediaManager.init(context, config);
            isInitialized = true; // Đánh dấu đã khởi tạo
        }
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // You can update a progress bar here if needed
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = resultData.get("secure_url").toString();
                            if (callback != null) {
                                callback.onSuccess(requestId, resultData);
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(context, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            if (callback != null) {
                                callback.onError(requestId, error);
                            }
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            // Handle rescheduling if needed
                        }
                    }).dispatch();
        }
    }


}