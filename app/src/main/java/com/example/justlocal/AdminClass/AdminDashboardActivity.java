package com.example.justlocal.AdminClass;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.justlocal.DashboardAdapter.DashboardMenuAdapter;
import com.example.justlocal.DashboardAdapter.DashboardMenuItem;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityAdminDashboardBinding;
import com.example.justlocal.AdminClass.UserManagementActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private DashboardMenuAdapter dashboardMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        return menuItems;
    }


    private void setupListeners() {
        binding.btnViewComplaints.setOnClickListener(v -> {
            // Open complaints screen
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
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}