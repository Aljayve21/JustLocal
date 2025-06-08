package com.example.justlocal.UserAdapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Product;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ProductItemLayoutBinding;

import java.util.List;
import java.util.function.Consumer;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final List<Product> products;
    private final Consumer<Product> onItemClick;
    private final Consumer<Product> onEditClick;
    private final Consumer<Product> onDeleteClick;

    public ProductAdapter(List<Product> products, Consumer<Product> onItemClick,
                          Consumer<Product> onEditClick, Consumer<Product> onDeleteClick) {
        this.products = products;
        this.onItemClick = onItemClick;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ProductItemLayoutBinding binding = ProductItemLayoutBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ProductItemLayoutBinding binding;

        ViewHolder(ProductItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.tvProductName.setText(product.getProductName());
            binding.tvProductDescription.setText(product.getProductDescription());
            binding.tvProductPrice.setText("₱" + product.getPrice());
            binding.tvProductQuantity.setText(product.getQuantity() + " pcs");

            // Decode base64 image
            try {
                String base64String = product.getImage();; // assuming this is the base64 string
                if (base64String != null && !base64String.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    binding.ivProductImage.setImageBitmap(bitmap);
                } else {
                    binding.ivProductImage.setImageResource(R.drawable.ic_default_avatar);
                }
            } catch (Exception e) {
                e.printStackTrace();
                binding.ivProductImage.setImageResource(R.drawable.ic_default_avatar);
            }


            binding.getRoot().setOnClickListener(v -> onItemClick.accept(product));
            binding.btnProductActions.setOnClickListener(v -> showProductActions(product));
        }



        private void showProductActions(Product product) {
            PopupMenu popup = new PopupMenu(binding.getRoot().getContext(), binding.btnProductActions);
            popup.inflate(R.menu.product_actions_layout); // Make sure this menu exists
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.product_action_edit) {
                    onEditClick.accept(product);
                    return true;
                } else if (id == R.id.product_action_delete) {
                    onDeleteClick.accept(product);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}
