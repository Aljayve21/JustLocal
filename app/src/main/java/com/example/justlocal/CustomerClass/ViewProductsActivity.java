package com.example.justlocal.CustomerClass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justlocal.Models.Order;
import com.example.justlocal.Models.Product;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityViewProductsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ViewProductsActivity extends AppCompatActivity {

    private ActivityViewProductsBinding binding;
    private Product product;
    private Order order;
    private int selectedQuantity = 1;
    private boolean isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // Get product ID from Intent
        String productID = getIntent().getStringExtra("productID");
        if (productID == null || productID.isEmpty()) {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show();
            Log.d("PRODUCT_ID_CHECK", "Received productID: " + productID);

            finish();
            return;
        }

        binding.btnBuyNow.setOnClickListener(v -> {
            buyNow();
        });

        // Load product data
        loadProductFromFirebase(productID);

        setupClickListeners();
    }

    private void loadProductFromFirebase(String productID) {
        FirebaseDatabase.getInstance().getReference("products")
                .child(productID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        product = snapshot.getValue(Product.class);
                        if (product != null) {
                            setupUI();
                        } else {
                            Toast.makeText(ViewProductsActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewProductsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void reportProduct() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "You need to login to report.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Simple dialog to enter complaint message
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Report Product");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Describe the issue...");
        input.setMinLines(3);
        input.setMaxLines(5);
        input.setPadding(32, 32, 32, 32);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate unique complaint ID
            String complaintID = FirebaseDatabase.getInstance().getReference("complaints").push().getKey();

            if (complaintID == null) {
                Toast.makeText(this, "Failed to create report ID.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Build complaint data
            HashMap<String, Object> complaintData = new HashMap<>();
            complaintData.put("complaintID", complaintID);
            complaintData.put("customerID", currentUserId);
            complaintData.put("sellerID", product.getSellerID());
            complaintData.put("productID", product.getProductID());
            complaintData.put("orderID", order.getOrderID()); // optional if not linked to specific order
            complaintData.put("message", message);
            complaintData.put("status", "Open");
            complaintData.put("repliedBy", "");
            complaintData.put("createdAt", System.currentTimeMillis());

            FirebaseDatabase.getInstance().getReference("complaints")
                    .child(complaintID)
                    .setValue(complaintData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Report submitted successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void setupUI() {
        binding.txtProductName.setText(product.getProductName());
        binding.tvProductName.setText(product.getProductName());
        binding.tvProductDescription.setText(product.getProductDescription());
        binding.tvPrice.setText(product.getPrice());
        binding.tvQuantity.setText(product.getQuantity() + "pcs available");
        binding.tvProductStatus.setText("Status: " + product.getStatus());
        binding.tvApprovedBy.setText("Approved by: " + product.getApprovedBy());
        binding.tvSellerName.setText("Loading...");

        // Fetch seller name using sellerID
        FirebaseDatabase.getInstance().getReference("users")
                .child(product.getSellerID())
                .child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fullName = snapshot.getValue(String.class);
                        binding.tvSellerName.setText(fullName != null ? fullName : "Unknown Seller");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.tvSellerName.setText("Unknown Seller");
                    }
                });

        // Stock badge
        int quantity = Integer.parseInt(product.getQuantity());
        if (quantity > 0) {
            binding.tvStockStatus.setText("In Stock");
            binding.tvStockStatus.setBackgroundResource(R.drawable.stock_badge_green);
        } else {
            binding.tvStockStatus.setText("Out of Stock");
            binding.tvStockStatus.setBackgroundResource(R.drawable.stock_badge_red);
        }

        loadProductImage(product.getImage());
        isFavorited = product.isFavorited();
        updateFavoriteButton();
        binding.tvSelectedQuantity.setText(String.valueOf(selectedQuantity));
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.btnAddToCart.setOnClickListener(v -> addToCart());
        binding.btnAddToCartFull.setOnClickListener(v -> addToCart());
        binding.btnBuyNow.setOnClickListener(v -> buyNow());
        binding.btnDecreaseQuantity.setOnClickListener(v -> decreaseQuantity());
        binding.btnIncreaseQuantity.setOnClickListener(v -> increaseQuantity());
        binding.btnReportProduct.setOnClickListener(v -> reportProduct());
    }

    private void loadProductImage(String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                binding.ivProductImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                binding.ivProductImage.setImageResource(R.drawable.placeholder_product);
                e.printStackTrace();
            }
        } else {
            binding.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }
    }

    private void toggleFavorite() {
        isFavorited = !isFavorited;
        product.setFavorited(isFavorited);
        updateFavoriteButton();
        Toast.makeText(this, isFavorited ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
        // TODO: Save to Firebase wishlist if needed
    }

    private void updateFavoriteButton() {
        binding.btnFavorite.setImageResource(isFavorited ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void addToCart() {
        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (customerId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantityToAdd = selectedQuantity;

        HashMap<String, Object> cartItem = new HashMap<>();
        cartItem.put("productID", product.getProductID());
        cartItem.put("productName", product.getProductName());
        cartItem.put("price", product.getPrice());
        cartItem.put("quantity", quantityToAdd);
        cartItem.put("image", product.getImage()); // optional

        FirebaseDatabase.getInstance().getReference("carts")
                .child(customerId)
                .child(product.getProductID())
                .setValue(cartItem)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }




    private void buyNow() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("addresses");
        Query query = addressRef.orderByChild("userID").equalTo(currentUserId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasAddress = false;

                for (DataSnapshot addressSnap : snapshot.getChildren()) {
                    // You may validate further if needed (e.g., is_Default)
                    Log.d("BUY_NOW", "Address found: " + addressSnap.getKey());
                    hasAddress = true;
                    break;
                }

                if (hasAddress) {
                    // Redirect to OrderPaymentActivity
                    Intent intent = new Intent(ViewProductsActivity.this, OrderPaymentActivity.class);
                    intent.putExtra("productID", product.getProductID()); // Optional: send product info
                    intent.putExtra("quantity", selectedQuantity); // Optional

                    Log.d("BuyNow", "Selected quantity: " + selectedQuantity);

                    startActivity(intent);
                } else {
                    // Redirect to UserAddressActivity
                    Toast.makeText(ViewProductsActivity.this, "Please add your address before buying.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ViewProductsActivity.this, UserAddressActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProductsActivity.this, "Error checking address: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void decreaseQuantity() {
        if (selectedQuantity > 1) {
            selectedQuantity--;
            binding.tvSelectedQuantity.setText(String.valueOf(selectedQuantity));
        }
    }

    private void increaseQuantity() {
        int availableQuantity = Integer.parseInt(product.getQuantity());
        if (selectedQuantity < availableQuantity) {
            selectedQuantity++;
            binding.tvSelectedQuantity.setText(String.valueOf(selectedQuantity));
        } else {
            Toast.makeText(this, "Maximum available quantity reached", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
