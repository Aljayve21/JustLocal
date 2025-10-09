package com.example.justlocal.AdminClass;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.justlocal.DashboardAdapter.DashboardMenuAdapter;
import com.example.justlocal.DashboardAdapter.DashboardMenuItem;
import com.example.justlocal.LoginActivity;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.R;
import com.example.justlocal.SellerClass.ProductManagementActivity;
import com.example.justlocal.databinding.ActivityAdminDashboardBinding;
import com.example.justlocal.AdminClass.UserManagementActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private DashboardMenuAdapter dashboardMenuAdapter;
    private Complaint complaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<DashboardMenuItem> menuItems = createMenuItems();
        dashboardMenuAdapter = new DashboardMenuAdapter(menuItems, this::onMenuItemClick);
        binding.rvDashboardMenu.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvDashboardMenu.setAdapter(dashboardMenuAdapter);
    }


    private List<DashboardMenuItem> createMenuItems() {
        List<DashboardMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new DashboardMenuItem("User Management", R.drawable.ic_users, "users"));
        menuItems.add(new DashboardMenuItem("Product Management", R.drawable.ic_products, "products", 15));
        menuItems.add(new DashboardMenuItem("Ordering Management", R.drawable.ic_orders, "orders"));
        menuItems.add(new DashboardMenuItem("Payment Tracking", R.drawable.ic_payments, "payments"));
        menuItems.add(new DashboardMenuItem("Logout", R.drawable.ic_logout, "logout"));

        return menuItems;
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


    private void setupListeners() {
        binding.btnViewComplaints.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminComplaintsActivity.class);
//            intent.putExtra("complaintID", complaint.getComplaintID());
            startActivity(intent);
        });

        binding.btnGenerateReport.setOnClickListener(v -> {
            // Generate report logic
        });
    }

    private void onMenuItemClick(DashboardMenuItem item) {
        Intent intent = null;
        switch (item.getAction()) {
            case "users":
                intent = new Intent(this, UserManagementActivity.class);
                break;
            case "products":
                intent = new Intent(this, AdminProductManagementActivity.class);
                break;
            case "orders":
                intent = new Intent(this, AdminOrderMonitoringActivity.class);
                break;
//            case "complaints":
//                intent = new Intent(this, AdminComplaintsActivity.class);
//                break;
            case "payments":
                intent = new Intent(this, AdminPaymentMonitoringActivity.class);
                break;
            case "logout":
                logout();
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}