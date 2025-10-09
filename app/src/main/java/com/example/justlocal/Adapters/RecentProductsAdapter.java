package com.example.justlocal.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Product;
import com.example.justlocal.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentProductsAdapter extends RecyclerView.Adapter<RecentProductsAdapter.ProductViewHolder> {

    private List<Product> productList;

    public RecentProductsAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_horizontal, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getProductName());
        holder.tvPrice.setText("₱" + product.getPrice());

        // Decode Base64 image
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                byte[] decodedBytes = android.util.Base64.decode(product.getImage(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.ivProductImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                holder.ivProductImage.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            // Add intent to product detail activity if needed
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProductImage;
        TextView tvProductName, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}