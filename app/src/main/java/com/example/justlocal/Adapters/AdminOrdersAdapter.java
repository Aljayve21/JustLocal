package com.example.justlocal.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.justlocal.AdminClass.Activities.AdminOrderDetailsActivity;
import com.example.justlocal.Models.Notification;
import com.example.justlocal.Models.Order;
import com.example.justlocal.databinding.AdminOrderItemCardBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder> {

    private final Context context;
    private final List<Order> orders;
    private final FirebaseFirestore db;
    private final Map<String, String> userNameCache; // cache for seller & customer names

    public AdminOrdersAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
        this.db = FirebaseFirestore.getInstance();
        this.userNameCache = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminOrderItemCardBinding binding = AdminOrderItemCardBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.binding.tvOrderID.setText("Order #" + order.getOrderID());
        holder.binding.tvPaymentMethod.setText(order.getPaymentMethod());
        holder.binding.tvDeliveryMethod.setText(order.getDeliveryMethod());
        holder.binding.tvTotalAmount.setText("₱" + String.format(Locale.getDefault(), "%,.2f",
                Double.parseDouble(order.getTotalAmount())));
        holder.binding.tvOrderDate.setText(order.getOrderDate());
        holder.binding.tvOrderStatus.setText(order.getStatus());

        // ✅ Kung completed, itago yung button
        if ("Completed".equalsIgnoreCase(order.getStatus()) || "Delivered".equalsIgnoreCase(order.getStatus())) {
            holder.binding.btnTrackOrder.setText("Completed");
            holder.binding.btnTrackOrder.setEnabled(false);
        } else {
            holder.binding.btnTrackOrder.setText("Mark as Delivered");
            holder.binding.btnTrackOrder.setEnabled(true);
            holder.binding.btnTrackOrder.setOnClickListener(v -> markAsDelivered(order));
        }


        // Fetch Seller Name
        setUserName(order.getSellerID(), holder.binding.tvSellerName);

        // Fetch Customer Name
        setUserName(order.getCustomerID(), holder.binding.tvCustomerName);
    }


    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final AdminOrderItemCardBinding binding;

        public ViewHolder(AdminOrderItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * Mark order as delivered and notify customer.
     */
    private void markAsDelivered(Order order) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders").child(order.getOrderID());
        ordersRef.child("status").setValue("Delivered")
                .addOnSuccessListener(unused -> {
                    sendNotification(order.getCustomerID(), "Order Delivered",
                            "Your order #" + order.getOrderID() + " has been marked as delivered. Thank you for shopping!");
                });
    }

    private void sendNotification(String userId, String title, String message) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(userId);
        String notifId = notifRef.push().getKey();
        if (notifId == null) return;

        Notification notification = new Notification(
                notifId,
                title,
                message,
                System.currentTimeMillis(),
                "order"
        );

        notifRef.child(notifId).setValue(notification);
    }

    /**
     * Fetch user name from Firestore with caching.
     */
    private void setUserName(String userId, android.widget.TextView textView) {
        if (userId == null || userId.isEmpty()) {
            textView.setText("Unknown");
            return;
        }

        com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            if (fullName != null && !fullName.isEmpty()) {
                                textView.setText(fullName);
                            } else {
                                textView.setText("Unknown");
                            }
                        } else {
                            textView.setText("Unknown");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        textView.setText("Unknown");
                    }
                });
    }

}
