package com.example.justlocal.CustomerClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BrowseProductsActivity extends AppCompatActivity {

    private ActivityBrowseProductsBinding binding;
    private ProductAdapter productAdapter;
    private DatabaseReference productRef, usersRef;

    private String productId;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowseProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }



        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );




        setupRecyclerView();
        setupSearchBar();
        fetchProducts();

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, filteredProducts);
        productAdapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {

                Toast.makeText(BrowseProductsActivity.this, "Clicked: " + product.getProductName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(BrowseProductsActivity.this, ViewProductsActivity.class);
                intent.putExtra("productID", product.getProductID());
                startActivity(intent);
            }

            @Override
            public void onAddToCartClick(Product product) {
                Toast.makeText(BrowseProductsActivity.this, "Add to cart: " + product.getProductName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFavoriteClick(Product product) {
                Toast.makeText(BrowseProductsActivity.this, "Favorited: " + product.getProductName(), Toast.LENGTH_SHORT).show();
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

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void fetchProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("products")
                .orderByChild("status")
                .equalTo("approved")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        allProducts.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Product product = snap.getValue(Product.class);
                            if (product != null) {
                                allProducts.add(product);
                            }
                        }

                        loadWishlist();

                        filterProducts(binding.etSearch.getText().toString().trim());
                        binding.progressBar.setVisibility(View.GONE);
                    }



                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(BrowseProductsActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });


    }

    private void loadWishlist() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference wishlistRef = FirebaseDatabase.getInstance().getReference("wishlists");

        wishlistRef.orderByChild("customerID").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> favoritedProductIds = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String prodId = snap.child("productID").getValue(String.class);
                            if (prodId != null) favoritedProductIds.add(prodId);
                        }

                        for (Product p : allProducts) {
                            p.setFavorited(favoritedProductIds.contains(p.getProductID()));
                        }

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
            if (p.getProductName() != null && p.getProductName().toLowerCase().contains(keyword.toLowerCase())) {
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
