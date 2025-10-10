package com.example.justlocal.AdminClass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.justlocal.DashboardAdapter.DashboardMenuAdapter;
import com.example.justlocal.DashboardAdapter.DashboardMenuItem;
import com.example.justlocal.LoginActivity;
import com.example.justlocal.R;
import com.example.justlocal.SellerClass.SellerDashboardActivity;
import com.example.justlocal.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private DashboardMenuAdapter dashboardMenuAdapter;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseDatabase.getInstance().getReference();

        setupListeners();
        setupRecyclerView();
        setGreetings();
        loadAdminName();

        // Initial load of dashboard counters
        loadDashboardCounters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh counters when coming back to dashboard
        loadDashboardCounters();
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

    private void loadAdminName() {
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
                        Toast.makeText(AdminDashboardActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void setupListeners() {
        binding.btnViewComplaints.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminComplaintsActivity.class);
            startActivity(intent);
        });

        binding.btnGenerateReport.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminGenerateReportActivity.class);
            startActivity(intent);
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

    // ===== Dashboard Counters =====
    private void loadDashboardCounters() {
        showLoading(true);

        final boolean[] done = {false, false};

        // 1) Total users
        db.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total = snapshot.getChildrenCount();
                binding.tvTotalUsers.setText(String.valueOf(total));
                done[0] = true;
                maybeHideLoading(done);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                binding.tvTotalUsers.setText("0");
                done[0] = true;
                maybeHideLoading(done);
            }
        });

        // 2) Pending approvals (products with status == "Pending")
        db.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                int pending = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String status = snap.child("status").getValue(String.class);
                    if (status != null && status.equalsIgnoreCase("Pending")) {
                        pending++;
                    }
                }
                binding.tvPendingApprovals.setText(String.valueOf(pending));
                done[1] = true;
                maybeHideLoading(done);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                binding.tvPendingApprovals.setText("0");
                done[1] = true;
                maybeHideLoading(done);
            }
        });
    }

    private void maybeHideLoading(boolean[] done) {
        if (done[0] && done[1]) {
            showLoading(false);
        }
    }

    private void showLoading(boolean show) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
