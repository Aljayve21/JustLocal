package com.example.justlocal.SellerOrdersAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Order;
import com.example.justlocal.Models.Product;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ItemOrderMonitoringBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SellerOrdersAdapter extends RecyclerView.Adapter<SellerOrdersAdapter.OrderViewHolder> {

    private final Context context;
    private final ArrayList<Order> orders;
    private final Consumer<Order> onClick;

    public SellerOrdersAdapter(Context context, ArrayList<Order> orders, Consumer<Order> onClick) {
        this.context = context;
        this.orders = orders;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(ItemOrderMonitoringBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Set order ID
        holder.binding.tvOrderID.setText("Order #" + order.getOrderID());

        // Set order status and indicator
        String status = order.getStatus() != null ? order.getStatus() : "pending";
        holder.binding.tvOrderStatus.setText("Status: " + status);

        int indicatorRes;
        int statusColor;

        switch (status.toLowerCase()) {
            case "rejected":
                indicatorRes = R.drawable.status_indicator_rejected;
                statusColor = Color.parseColor("#DC2626");
                break;
            case "accepted":
                indicatorRes = R.drawable.status_indicator_accepted;
                statusColor = Color.parseColor("#059669");
                break;
            case "on delivery":
            case "out for delivery":
                indicatorRes = R.drawable.status_indicator_on_delivery;
                statusColor = Color.parseColor("#D97706");
                break;
            case "delivered":
                indicatorRes = R.drawable.status_indicator_delivered;
                statusColor = Color.parseColor("#2563EB");
                break;
            default:
                indicatorRes = R.drawable.status_indicator_pending;
                statusColor = Color.parseColor("#059669");
                break;
        }

        holder.binding.tvOrderStatus.setTextColor(statusColor);
        holder.binding.viewStatusIndicator.setBackgroundResource(indicatorRes);

        // Load product info
        FirebaseDatabase.getInstance().getReference("order_items")
                .orderByChild("orderID").equalTo(order.getOrderID())
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String productID = snap.child("productID").getValue(String.class);
                            FirebaseDatabase.getInstance().getReference("products").child(productID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override public void onDataChange(@NonNull DataSnapshot productSnap) {
                                            Product product = productSnap.getValue(Product.class);
                                            if (product != null) {
                                                holder.binding.tvProductName.setText(product.getProductName());
                                                if (product.getImage() != null && !product.getImage().isEmpty()) {
                                                    Bitmap bitmap = decodeBase64ToBitmap(product.getImage());
                                                    if (bitmap != null) {
                                                        holder.binding.ivProductImage.setImageBitmap(bitmap);
                                                    } else {
                                                        holder.binding.ivProductImage.setImageResource(R.drawable.placeholder_image);
                                                    }
                                                } else {
                                                    holder.binding.ivProductImage.setImageResource(R.drawable.placeholder_image);
                                                }
                                            }
                                        }

                                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                            break;
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Load address
        FirebaseDatabase.getInstance().getReference("addresses")
                .child(order.getAddressID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String street = snapshot.child("street").getValue(String.class);
                        String city = snapshot.child("city").getValue(String.class);
                        if (street != null && city != null) {
                            holder.binding.tvAddressSummary.setText(street + ", " + city);
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Load customer name
        FirebaseDatabase.getInstance().getReference("users")
                .child(order.getCustomerID())
                .child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            holder.binding.tvCustomerName.setText(name);
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Set total amount if available
        try {
            double total = Double.parseDouble(
                    order.getTotalAmount() != null ? order.getTotalAmount() : "0"
            );
            holder.binding.tvTotalAmount.setText("₱" + String.format("%.2f", total));
        } catch (NumberFormatException e) {
            holder.binding.tvTotalAmount.setText("₱0.00");
        }


        // Click action
        holder.itemView.setOnClickListener(v -> onClick.accept(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderMonitoringBinding binding;
        public OrderViewHolder(@NonNull ItemOrderMonitoringBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // Helper to decode base64 image string
    private Bitmap decodeBase64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
