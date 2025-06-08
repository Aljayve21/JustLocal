package com.example.justlocal.SellerClass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.LoginActivity;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivitySellerDashboardBinding;

public class SellerDashboardActivity extends AppCompatActivity {

    private ActivitySellerDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        loadDashboardData();

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
        // Mock data - replace with actual API calls
        binding.tvProductCount.setText("24");
        binding.tvSalesCount.setText("156");
        binding.tvPendingCount.setText("8");

        // Load recent activity
        setupRecentActivity();
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
//        startActivity(new Intent(this, OrderManagementActivity.class));
    }

    private void openProfile() {
//        startActivity(new Intent(this, ProfileSettingsActivity.class));
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