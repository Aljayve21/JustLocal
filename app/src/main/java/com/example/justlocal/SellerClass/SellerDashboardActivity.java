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
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivitySellerDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class SellerDashboardActivity extends AppCompatActivity {

    private ActivitySellerDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }



        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setupViews();
        loadDashboardData();
        setGreetings();
        loadSellerName();

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
                .orderByChild("sellerId")
                .equalTo(sellerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.tvProductCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.tvProductCount.setText("0");
                    }
                });

        // 2️⃣ Sales Count (Delivered Orders)
        FirebaseDatabase.getInstance().getReference("orders")
                .orderByChild("sellerId")
                .equalTo(sellerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int deliveredCount = 0;
                        int pendingCount = 0;
                        StringBuilder recentActivities = new StringBuilder();

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            String status = orderSnap.child("status").getValue(String.class);
                            String productName = orderSnap.child("productName").getValue(String.class);
                            String orderId = orderSnap.getKey();

                            if ("Delivered".equalsIgnoreCase(status)) deliveredCount++;
                            if ("Pending".equalsIgnoreCase(status)) pendingCount++;

                            // Add to recent activity (latest on top)
                            recentActivities.insert(0, "• " + productName + " - " + status + "\n");
                        }

                        // Update counts
                        binding.tvSalesCount.setText(String.valueOf(deliveredCount));
                        binding.tvPendingCount.setText(String.valueOf(pendingCount));

                        // Update recent activity (show last 5)
                        String[] activities = recentActivities.toString().split("\n");
                        StringBuilder lastFive = new StringBuilder();
                        for (int i = 0; i < Math.min(5, activities.length); i++) {
                            lastFive.append(activities[i]).append("\n");
                        }
                        binding.tvRecentActivity.setText(lastFive.toString().trim());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.tvSalesCount.setText("0");
                        binding.tvPendingCount.setText("0");
                        binding.tvRecentActivity.setText("No recent activity");
                    }
                });
    }


    private void setupRecentActivity() {
        // Mock recent activities
        String[] activities = {
                "New order for iPhone Case",
                "Product 'Samsung Charger' approved",
                "Payment received ₱1,250"
        };

        StringBuilder sb = new StringBuilder();
        for (String activity : activities) {
            sb.append("• ").append(activity).append("\n");
        }
        binding.tvRecentActivity.setText(sb.toString().trim());
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
        Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
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