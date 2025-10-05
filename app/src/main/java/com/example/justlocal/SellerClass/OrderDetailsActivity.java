package com.example.justlocal.SellerClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justlocal.DeliveryTracking.DeliveryTrackingActivity;
import com.example.justlocal.Models.Order;
import com.example.justlocal.databinding.ActivityOrderDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    private ActivityOrderDetailsBinding binding;
    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        orderID = getIntent().getStringExtra("orderID");
        if (orderID == null || orderID.isEmpty()) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupBackButton();
        loadOrderDetails();
        setupAcceptButton();
        setupRejectButton();
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupAcceptButton() {
        binding.btnAcceptOrder.setOnClickListener(v -> {
            if (orderID == null || orderID.isEmpty()) {
                Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders").child(orderID);
            DatabaseReference deliveryInfoRef = FirebaseDatabase.getInstance().getReference("delivery_info").child(orderID);
            DatabaseReference timelineRef = FirebaseDatabase.getInstance().getReference("delivery_timeline").child(orderID);

            // Update order status
            ordersRef.child("status").setValue("accepted");

            // Add delivery_info (basic setup, can be updated in DeliveryTrackingActivity)
            HashMap<String, Object> deliveryInfo = new HashMap<>();
            deliveryInfo.put("carrier", "Not Assigned");
            deliveryInfo.put("trackingNo", "Pending");
            deliveryInfo.put("status", "Processing");
            deliveryInfoRef.setValue(deliveryInfo);

            // Add timeline entry
            String currentTime = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(new Date());
            HashMap<String, Object> timelineData = new HashMap<>();
            timelineData.put("status", "Order Accepted");
            timelineData.put("timestamp", currentTime);
            timelineRef.push().setValue(timelineData);

            Toast.makeText(this, "Order accepted successfully", Toast.LENGTH_SHORT).show();

            // Redirect to DeliveryTrackingActivity
            Intent intent = new Intent(OrderDetailsActivity.this, DeliveryTrackingActivity.class);
            intent.putExtra("orderID", orderID);
            intent.putExtra("userRole", "seller");
            startActivity(intent);
            finish();
        });
    }

    private void setupRejectButton() {
        binding.btnRejectOrder.setOnClickListener(v -> {
            if (orderID == null || orderID.isEmpty()) {
                Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseDatabase.getInstance().getReference("orders")
                    .child(orderID)
                    .child("status")
                    .setValue("rejected")
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Order rejected successfully", Toast.LENGTH_SHORT).show();
                        binding.tvStatus.setText("Status: rejected");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to reject order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadOrderDetails() {
        FirebaseDatabase.getInstance().getReference("orders")
                .child(orderID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Order order = snapshot.getValue(Order.class);
                        if (order != null) {
                            binding.tvOrderID.setText("Order ID: " + order.getOrderID());
                            binding.tvCustomerID.setText("Customer ID: " + order.getCustomerID());
                            binding.tvPaymentMethod.setText("Payment Method: " + order.getPaymentMethod());
                            binding.tvDeliveryMethod.setText("Delivery Method: " + order.getDeliveryMethod());
                            binding.tvTrackingNo.setText("Tracking No: " + order.getTrackingNo());
                            binding.tvOrderDate.setText("Order Date: " + order.getOrderDate());
                            binding.tvStatus.setText("Status: " + order.getStatus());

                            try {
                                double total = Double.parseDouble(String.valueOf(order.getTotalAmount()));
                                binding.tvTotalAmount.setText("Total Amount: ₱" + String.format("%.2f", total));
                            } catch (NumberFormatException e) {
                                binding.tvTotalAmount.setText("Total Amount: ₱0.00");
                            }

                            // 🔽 Fetch user contact using customerID
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(order.getCustomerID())
                                    .child("phone")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String contact = snapshot.getValue(String.class);
                                            if (contact != null && !contact.isEmpty()) {
                                                binding.tvContactNo.setText("Contact: " + contact);
                                            } else {
                                                binding.tvContactNo.setText("Contact: Not available");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            binding.tvContactNo.setText("Contact: Error");
                                        }
                                    });
                        } else {
                            Toast.makeText(OrderDetailsActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderDetailsActivity.this, "Error loading order: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
