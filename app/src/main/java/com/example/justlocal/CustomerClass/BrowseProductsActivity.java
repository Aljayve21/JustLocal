package com.example.justlocal.CustomerClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.justlocal.Models.Product;
import com.example.justlocal.ProductAdapter.ProductAdapter;
import com.example.justlocal.databinding.ActivityBrowseProductsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BrowseProductsActivity extends AppCompatActivity {

    private ActivityBrowseProductsBinding binding;
    private ProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        setupRecyclerView();
        setupSearchBar();
        fetchProducts();

        // Handle back
        binding.btnBack.setOnClickListener(v -> finish());

        // ✅ If opened from AI scanner, apply detected search query
        String detectedQuery = getIntent().getStringExtra("searchQuery");
        if (detectedQuery != null && !detectedQuery.isEmpty()) {
            binding.etSearch.setText(detectedQuery);
            filterProducts(detectedQuery);
        }

    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, filteredProducts);
        productAdapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(BrowseProductsActivity.this, ViewProductsActivity.class);
                intent.putExtra("productID", product.getProductID());
                startActivity(intent);
            }

            @Override
            public void onAddToCartClick(Product product) {
                Toast.makeText(BrowseProductsActivity.this,
                        "Added to cart: " + product.getProductName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFavoriteClick(Product product) {
                Toast.makeText(BrowseProductsActivity.this,
                        "Favorited: " + product.getProductName(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupSearchBar() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterProducts(s.toString().trim());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void fetchProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        allProducts.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Product product = snap.getValue(Product.class);
                            if (product != null &&
                                    product.getStatus() != null &&
                                    product.getStatus().equalsIgnoreCase("approved")) {
                                allProducts.add(product);
                            }
                        }

                        loadWishlist();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(BrowseProductsActivity.this,
                                "Failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadWishlist() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("wishlists")
                .orderByChild("customerID").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> favoritedIds = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String prodId = snap.child("productID").getValue(String.class);
                            if (prodId != null) favoritedIds.add(prodId);
                        }

                        for (Product p : allProducts) {
                            p.setFavorited(favoritedIds.contains(p.getProductID()));
                        }

                        // Filter with current search
                        filterProducts(binding.etSearch.getText().toString().trim());
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void filterProducts(String keyword) {
        filteredProducts.clear();

        for (Product p : allProducts) {
            if (p.getProductName() != null &&
                    p.getProductName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredProducts.add(p);
            }
        }

        productAdapter.updateList(filteredProducts);

        binding.tvProductCount.setText(filteredProducts.size() + " products found");

        if (filteredProducts.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewProducts.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerViewProducts.setVisibility(View.VISIBLE);
        }
    }
}
