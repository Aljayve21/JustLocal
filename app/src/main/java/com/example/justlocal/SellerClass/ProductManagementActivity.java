package com.example.justlocal.SellerClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.AdminClass.EditUserActivity;
import com.example.justlocal.AdminClass.UserManagementActivity;
import com.example.justlocal.Models.Product;
import com.example.justlocal.Models.User;
import com.example.justlocal.R;
import com.example.justlocal.UserAdapter.ProductAdapter;
import com.example.justlocal.UserAdapter.UserAdapter;
import com.example.justlocal.databinding.ActivityProductManagementBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {

    private ActivityProductManagementBinding binding;
    private ProductAdapter adapter;
    private List<Product> products = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    private String currentProductFilter = null;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }



        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setupViews();
        loadProducts();
    }

    private void setupViews() {
        adapter = new ProductAdapter(filteredProducts, this::onProductClick, this::onProductEditClick, this::onProductDeleteClick);
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUsers.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());
//        binding.btnFilter.setOnClickListener(v -> showFilterDialog());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filteredProducts(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed
            }
        });

        setupFilterChips();
    }

    private void setupFilterChips() {
        View.OnClickListener chipListener = v -> {
            resetChipSelection();
            v.setSelected(true);
            String filter = ((TextView) v).getText().toString();
            currentProductFilter = filter.equals("All Products") ? null : filter;
            filteredProducts(binding.etSearch.getText().toString());
        };

        binding.chipAll.setOnClickListener(chipListener);
        binding.chipApproved.setOnClickListener(chipListener);
        binding.chipPending.setOnClickListener(chipListener);
        binding.chipAll.setSelected(true);
    }

    private void resetChipSelection() {
        binding.chipAll.setSelected(false);
        binding.chipApproved.setSelected(false);
        binding.chipPending.setSelected(false);
    }

    private void loadProducts() {
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Product product = userSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setFirebaseKey(userSnapshot.getKey()); // Set the unique Firebase key
                        products.add(product);
                    }
                }

                filteredProducts(binding.etSearch.getText().toString());
                updateStats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductManagementActivity.this, "Failed to load products: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void filteredProducts(String query) {
        filteredProducts.clear();

        String trimmedQuery = query != null ? query.trim().toLowerCase() : "";
        boolean isFilteringByStatus = currentProductFilter != null && !currentProductFilter.trim().isEmpty();

        for (Product product : products) {
            boolean matchesQuery = trimmedQuery.isEmpty()
                    || (product.getProductName() != null && product.getProductName().toLowerCase().contains(trimmedQuery))
                    || (product.getProductDescription() != null && product.getProductDescription().toLowerCase().contains(trimmedQuery))
                    || String.valueOf(product.getPrice()).contains(trimmedQuery)
                    || String.valueOf(product.getQuantity()).contains(trimmedQuery);

            boolean matchesStatus = !isFilteringByStatus
                    || (product.getStatus() != null && product.getStatus().equalsIgnoreCase(currentProductFilter));

            if (matchesQuery && matchesStatus) {
                filteredProducts.add(product);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void updateStats() {
        int total = products.size();
        int approved = 0;
        int pending = 0;

        for (Product product : products) {
            if ("approved".equalsIgnoreCase(product.getStatus())) {
                approved++;
            } else {
                pending++;
            }
        }

        binding.tvTotalProducts.setText(String.valueOf(total));
        binding.tvApproveProducts.setText(String.valueOf(approved));
        binding.tvPendingApprovals.setText(String.valueOf(pending));
    }

    private void onProductClick(Product product) {
        Toast.makeText(this, "Clicked: " + product.getProductName(), Toast.LENGTH_SHORT).show();
        // Implement further actions, such as opening a detailed view
    }

    private void showFilterDialog() {
        Toast.makeText(this, "Filter dialog clicked", Toast.LENGTH_SHORT).show();
        // Implement filter dialog functionality
    }

    private void onProductEditClick(Product product) {
        // Open edit activity, pass User details via Intent
//        Intent intent = new Intent(this, EditProductActivity.class);
//        intent.putExtra("userId", user.getId()); // Pass user ID only // Correct label// Make sure User implements Serializable or Parcelable
//        startActivity(intent);


    }


    private void onProductDeleteClick(Product product) {
        // Confirm deletion or delete directly
        new AlertDialog.Builder(this)
                .setTitle("Delete product")
                .setMessage("Are you sure you want to delete " + product.getProductName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    productsRef.child(product.getProductID().replace(".", ",")).removeValue() // Adjust key if needed
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }




}