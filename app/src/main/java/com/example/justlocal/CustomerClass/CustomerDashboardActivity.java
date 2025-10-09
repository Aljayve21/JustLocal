package com.example.justlocal.CustomerClass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.justlocal.LoginActivity;
import com.example.justlocal.Models.Product;
import com.example.justlocal.Utility.TfliteEmbeddingHelper;
import com.example.justlocal.databinding.ActivityCustomerDashboardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CustomerDashboardActivity extends AppCompatActivity {

    private ActivityCustomerDashboardBinding binding;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_CODE = 2001;

    private TfliteEmbeddingHelper embeddingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            // Load MobileNet model
            embeddingHelper = new TfliteEmbeddingHelper(
                    this,
                    "mobilenet_v2_feature_vector.tflite",
                    224
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_LONG).show();
        }

        binding.cardView1.setOnClickListener(v -> {
            startActivity(new Intent(this, BrowseProductsActivity.class));
        });

        binding.cardOrders.setOnClickListener(v -> {
            String currentUserID = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent = new Intent(this, MyordersActivity.class);
            intent.putExtra("customerID", currentUserID);
            startActivity(intent);
        });

        binding.cardScanProduct.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        binding.cardReports.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerReportHistoryActivity.class));
        });

        binding.cardLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null) {
                matchProductByImage(photo);
            }
        }
    }

    private void matchProductByImage(Bitmap bitmap) {
        if (embeddingHelper == null) {
            Toast.makeText(this, "Model not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        float[] queryEmbedding = embeddingHelper.getEmbedding(bitmap);
        if (queryEmbedding == null) {
            Toast.makeText(this, "Failed to extract features", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double bestScore = -1;
                        Product bestMatch = null;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Product product = snap.getValue(Product.class);
                            if (product == null) continue;

                            List<Double> embList = product.getEmbedding();
                            if (embList == null || embList.isEmpty()) continue;

                            float[] dbEmbedding = new float[embList.size()];
                            for (int i = 0; i < embList.size(); i++) {
                                dbEmbedding[i] = embList.get(i).floatValue();
                            }

                            double score = cosineSimilarity(queryEmbedding, dbEmbedding);

                            // 🔍 Debug log
                            android.util.Log.d("SIMILARITY_CHECK",
                                    "Product: " + product.getProductName() + " | Score: " + score);

                            if (score > bestScore) {
                                bestScore = score;
                                bestMatch = product;
                            }
                        }

                        // Lower threshold muna for testing
                        if (bestMatch != null && bestScore > 0.15) {
                            Toast.makeText(CustomerDashboardActivity.this,
                                    "Searching images....", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(CustomerDashboardActivity.this, BrowseProductsActivity.class);
                            intent.putExtra("searchQuery", bestMatch.getProductName());
                            startActivity(intent);
                        } else {
                            Toast.makeText(CustomerDashboardActivity.this, "No similar product found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear user session
        getSharedPreferences("user_session", MODE_PRIVATE)
                .edit().clear().apply();

        // Return to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private double cosineSimilarity(float[] v1, float[] v2) {
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (embeddingHelper != null) {
            embeddingHelper.close();
        }
    }
}
