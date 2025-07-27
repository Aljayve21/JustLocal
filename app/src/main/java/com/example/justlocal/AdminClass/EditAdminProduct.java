package com.example.justlocal.AdminClass;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.justlocal.Models.Product;
import com.example.justlocal.Models.User;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityEditAdminProductBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditAdminProduct extends AppCompatActivity {

    private ActivityEditAdminProductBinding binding;
    private DatabaseReference productRef, usersRef;
    private String productId;
    private Product currentProduct;
    private String adminName = "Admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAdminProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Button listeners and Inputs
        binding.btnBack.setOnClickListener(v -> finish());

        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "No product ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnApprove.setOnClickListener(v -> updateProductStatus("approved"));
        binding.btnReject.setOnClickListener(v -> updateProductStatus("rejected"));

        loadProduct();

    }

    private void loadProduct() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentProduct = snapshot.getValue(Product.class);
                if (currentProduct != null) {
                    showProductDetails(currentProduct);
                } else {
                    Toast.makeText(EditAdminProduct.this, "Product not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditAdminProduct.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductDetails(Product product) {
        binding.tvProductName.setText(product.getProductName());
        binding.tvProductDescription.setText(product.getProductDescription());
        binding.tvProductPrice.setText("₱" + product.getPrice());
        binding.tvProductQuantity.setText(String.valueOf(product.getQuantity()));
        loadSellerName(product.getSellerID());
        binding.tvCurrentStatus.setText(product.getStatus() != null ? product.getStatus() : "Pending");

        // Show badge styling by status
        if ("approved".equalsIgnoreCase(product.getStatus())) {
            binding.tvStatusBadge.setText("APPROVED");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.status_approved_bg);
            binding.tvCurrentStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if ("rejected".equalsIgnoreCase(product.getStatus())) {
            binding.tvStatusBadge.setText("REJECTED");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.status_rejected_bg);
            binding.tvCurrentStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            binding.tvStatusBadge.setText("PENDING");
            binding.tvStatusBadge.setBackgroundResource(R.drawable.status_pending_bg);
        }

        // Decode base64 image
        if (product.getImage() != null) {
            Bitmap bitmap = decodeBase64ToBitmap(product.getImage());
            if (bitmap != null) {
                binding.ivProductImage.setImageBitmap(bitmap);
            } else {
                binding.ivProductImage.setImageResource(R.drawable.placeholder_image);
            }
        }

        // Show approved by only if already approved
        if ("approved".equalsIgnoreCase(product.getStatus()) && product.getApprovedBy() != null) {
            binding.llApprovedBy.setVisibility(View.VISIBLE);
            binding.tvApprovedBy.setText(product.getApprovedBy());
        } else {
            binding.llApprovedBy.setVisibility(View.GONE);
        }
    }

    private void loadSellerName(String sellerId) {
        if (sellerId == null || sellerId.isEmpty()) {
            binding.tvSellerName.setText("Unknown Seller");
            return;
        }

        usersRef.orderByChild("id").equalTo(sellerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                User seller = userSnapshot.getValue(User.class);
                                if (seller != null && seller.getFullName() != null) {
                                    binding.tvSellerName.setText(seller.getFullName());
                                    return;
                                }
                            }
                            binding.tvSellerName.setText("Unknown Seller");
                        } else {
                            binding.tvSellerName.setText("Unknown Seller");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        binding.tvSellerName.setText("Unknown Seller");
                    }
                });

    }

    private Bitmap decodeBase64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT);
            return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void updateProductStatus(String status) {
        if (currentProduct == null) return;

        productRef.child("status").setValue(status);
        if ("approved".equals(status)) {
            productRef.child("approvedBy").setValue(adminName);
        }

        Toast.makeText(this, "Product " + status, Toast.LENGTH_SHORT).show();
        finish();
    }


}