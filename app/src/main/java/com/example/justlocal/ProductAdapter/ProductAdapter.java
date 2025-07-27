package com.example.justlocal.ProductAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Product;
import com.example.justlocal.Models.User;
import com.example.justlocal.Models.Wishlist;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ItemProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;

    private Map<String, String> sellerNameCache = new HashMap<>(); // cache for seller names

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
        void onFavoriteClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemProductBinding binding = ItemProductBinding.inflate(inflater, parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        ItemProductBinding b = holder.binding;

        b.tvProductName.setText(product.getProductName());
        b.tvProductDescription.setText(product.getProductDescription());
        b.tvPrice.setText("₱" + product.getPrice());
        b.tvQuantity.setText(product.getQuantity() + " available");

        // Load and display image from Base64
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(product.getImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                b.ivProductImage.setImageBitmap(decodedByte);
            } catch (Exception e) {
                b.ivProductImage.setImageResource(R.drawable.placeholder_product);
            }
        } else {
            b.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }

        // Stock status display
        try {
            int qty = Integer.parseInt(product.getQuantity());
            if (qty > 20) {
                b.tvStockStatus.setText("In Stock");
                b.tvStockStatus.setBackgroundResource(R.drawable.stock_badge_green);
            } else if (qty > 0) {
                b.tvStockStatus.setText("Low Stock");
                b.tvStockStatus.setBackgroundResource(R.drawable.stock_badge_orange);
            } else {
                b.tvStockStatus.setText("Out of Stock");
                b.tvStockStatus.setBackgroundResource(R.drawable.stock_badge_red);
            }
        } catch (NumberFormatException e) {
            b.tvStockStatus.setText("Unknown");
            b.tvStockStatus.setBackgroundResource(R.drawable.stock_badge_orange);
        }

        // Load seller name from Firebase using sellerID
        String sellerId = product.getSellerID();
        if (sellerNameCache.containsKey(sellerId)) {
            b.tvSellerName.setText(sellerNameCache.get(sellerId));
        } else {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(sellerId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String fullName = "Unknown Seller";
                            if (snapshot.exists()) {
                                fullName = snapshot.child("fullName").getValue(String.class);
                                if (fullName == null || fullName.isEmpty()) {
                                    fullName = "Unknown Seller";
                                }
                            }
                            sellerNameCache.put(sellerId, fullName); // cache it
                            b.tvSellerName.setText(fullName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            b.tvSellerName.setText("Unknown Seller");
                        }
                    });
        }

        // Click listeners
        b.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });

        b.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(product);
        });

// Update button tint based on favorite state
        if (product.isFavorited()) {
            b.btnFavorite.setColorFilter(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            b.btnFavorite.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
        }

        b.btnFavorite.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (userId == null) {
                Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference wishlistRef = FirebaseDatabase.getInstance().getReference("wishlists");

            if (product.isFavorited()) {
                // Remove from wishlist
                wishlistRef.orderByChild("customerID").equalTo(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    String prodId = snap.child("productID").getValue(String.class);
                                    if (prodId != null && prodId.equals(product.getProductID())) {
                                        snap.getRef().removeValue();
                                        product.setFavorited(false);
                                        b.btnFavorite.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
                                        Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            } else {
                // Add to wishlist
                String wishlistId = wishlistRef.push().getKey();
                if (wishlistId == null) return;

                Wishlist wishlist = new Wishlist(wishlistId, userId, product.getProductID(), "active");

                wishlistRef.child(wishlistId).setValue(wishlist)
                        .addOnSuccessListener(task -> {
                            product.setFavorited(true);
                            b.btnFavorite.setColorFilter(context.getResources().getColor(android.R.color.holo_red_dark));
                            Toast.makeText(context, "Added to Wishlist", Toast.LENGTH_SHORT).show();
                        });
            }

            // Optional: notify listener
            if (listener != null) listener.onFavoriteClick(product);
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
