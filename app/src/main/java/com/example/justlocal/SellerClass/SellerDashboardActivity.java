package com.example.justlocal.SellerClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.AdminClass.EditUserActivity;
import com.example.justlocal.CustomerClass.BrowseProductsActivity;
import com.example.justlocal.CustomerClass.CustomerDashboardActivity;
import com.example.justlocal.CustomerClass.ViewProductsActivity;
import com.example.justlocal.LoginActivity;
import com.example.justlocal.Models.Order;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivitySellerDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class SellerDashboardActivity extends AppCompatActivity {

    private ActivitySellerDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySellerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        setupViews();
        loadDashboardData();
        setGreetings();
        loadSellerName();
        updateFCMToken();

    }

    private void updateFCMToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    String token = task.getResult();
                    String userId = FirebaseAuth.getInstance().getUid();
                    if (userId != null) {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(userId)
                                .child("fcmToken")
                                .setValue(token);
                    }
                });
    }

    private void setGreetings() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning!";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good Afternoon!";
        } else {
            greeting = "Good Evening!";
        }

        binding.txtGreetings.setText(greeting);
    }

    private void loadSellerName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            binding.tvSellerName.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SellerDashboardActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupViews() {
        // Header actions
        binding.btnNotifications.setOnClickListener(v -> openNotifications());
        binding.btnSettings.setOnClickListener(v -> openSettings());

        // Dashboard cards
        binding.cardProducts.setOnClickListener(v -> openProductManagement());
        binding.cardOrders.setOnClickListener(v -> openOrderManagement());
        binding.cardProfile.setOnClickListener(v -> openProfile());
        binding.cardLogout.setOnClickListener(v -> logout());

        // Quick actions
        binding.fabAddProduct.setOnClickListener(v -> addNewProduct());
    }

    private void loadDashboardData() {
        String sellerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1️⃣ Product Count
        FirebaseDatabase.getInstance().getReference("products")
                .orderByChild("sellerID")
                .equalTo(sellerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding != null) {
                            binding.tvProductCount.setText(String.valueOf(snapshot.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (binding != null) binding.tvProductCount.setText("0");
                    }
                });

        // 2️⃣ Sales Count (Delivered Orders) and Recent Activity
        FirebaseDatabase.getInstance().getReference("orders")
                .orderByChild("sellerID")
                .equalTo(sellerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null) return;

                        int deliveredCount = 0;
                        int pendingCount = 0;
                        List<Order> orderList = new ArrayList<>();

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Order order = orderSnap.getValue(Order.class);
                            if (order != null) {
                                String status = order.getStatus();
                                if ("Delivered".equalsIgnoreCase(status)) deliveredCount++;
                                if ("Pending".equalsIgnoreCase(status)) pendingCount++;
                                orderList.add(order);
                            }
                        }

                        // Update counts
                        binding.tvSalesCount.setText(String.valueOf(deliveredCount));
                        binding.tvPendingCount.setText(String.valueOf(pendingCount));

                        // Sort by timestamp descending for recent activity
                        Collections.sort(orderList, (o1, o2) -> Long.compare(o2.getOrderTimestamp(), o1.getOrderTimestamp()));

                        // Update recent activity (show last 5)
                        if (orderList.isEmpty()) {
                            binding.tvRecentActivity.setText("No recent activity");
                        } else {
                            StringBuilder recentActivities = new StringBuilder();
                            int count = 0;
                            for (Order order : orderList) {
                                if (count >= 5) break;
                                String orderIdShort = order.getOrderID();
                                if (orderIdShort != null && orderIdShort.length() > 8) {
                                    orderIdShort = orderIdShort.substring(0, 8);
                                }
                                recentActivities.append("• Order #").append(orderIdShort)
                                        .append(" - ").append(order.getStatus()).append("\n");
                                count++;
                            }
                            binding.tvRecentActivity.setText(recentActivities.toString().trim());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (binding == null) return;
                        binding.tvSalesCount.setText("0");
                        binding.tvPendingCount.setText("0");
                        binding.tvRecentActivity.setText("Failed to load activity");
                    }
                });
    }

    private void openProductManagement() {
        startActivity(new Intent(this, ProductManagementActivity.class));
    }

    private void openOrderManagement() {
        Toast.makeText(this, "Opening Orders for Seller: " + FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, OrderMonitoringActivity.class));
    }

    private void openProfile() {
        String sellerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("userId", sellerId);
        startActivity(intent);
    }

    private void openNotifications() {
        startActivity(new Intent(this, com.example.justlocal.CustomerClass.NotificationHistoryActivity.class));
    }

    private void openSettings() {
        startActivity(new Intent(this, com.example.justlocal.SettingsActivity.class));
    }

    private void addNewProduct() {
        startActivity(new Intent(this, AddProductActivity.class));
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear user session
        getSharedPreferences("user_session", MODE_PRIVATE)
                .edit().clear().apply();

        // Return to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData(); // Refresh data when returning to dashboard
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}