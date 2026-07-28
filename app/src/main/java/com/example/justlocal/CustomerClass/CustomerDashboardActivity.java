package com.example.justlocal.CustomerClass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.RecentProductsAdapter;
import com.example.justlocal.AdminClass.EditUserActivity;
import com.example.justlocal.LoginActivity;
import com.example.justlocal.Models.Product;
import com.example.justlocal.Utility.TfliteEmbeddingHelper;
import com.example.justlocal.databinding.ActivityCustomerDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomerDashboardActivity extends AppCompatActivity {

    private ActivityCustomerDashboardBinding binding;
    private static final int NOTIFICATION_PERMISSION_CODE = 3001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCustomerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkNotificationPermission();
        setGreetings();
        loadCustomerName();
        loadRecentProducts();
        updateFCMToken();
        listenForUnreadNotifications();

        binding.cardView1.setOnClickListener(v -> {
            startActivity(new Intent(this, BrowseProductsActivity.class));
        });



        binding.cardOrders.setOnClickListener(v -> {
            String currentUserID = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent = new Intent(this, MyordersActivity.class);
            intent.putExtra("customerID", currentUserID);
            startActivity(intent);
        });

        binding.cardScanProduct.setOnClickListener(v -> {
            startActivity(new Intent(this, ProductAiScannerActivity.class));
        });

        binding.cardReports.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerReportHistoryActivity.class));
        });

        binding.cardLogout.setOnClickListener(v -> {
            logout();
        });

        binding.tvprofile.setOnClickListener(v -> {
            openProfile();
        });

        binding.btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.justlocal.SettingsActivity.class));
        });

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationHistoryActivity.class));
        });
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

    private void listenForUnreadNotifications() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseDatabase.getInstance().getReference("notifications")
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean hasUnread = false;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Boolean isRead = snap.child("read").getValue(Boolean.class);
                            if (isRead != null && !isRead) {
                                hasUnread = true;
                                break;
                            }
                        }
                        
                        if (hasUnread) {
                            binding.btnNotifications.setColorFilter(android.graphics.Color.RED);
                        } else {
                            binding.btnNotifications.setColorFilter(android.graphics.Color.parseColor("#3B82F6"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void openProfile() {
        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("userId", customerId);
        startActivity(intent);
    }

    private void loadRecentProducts() {
        FirebaseDatabase.getInstance().getReference("products")
                .limitToLast(10) // recent 10 products
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Product> productList = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Product product = snap.getValue(Product.class);
                            if (product != null) productList.add(product);
                        }

                        RecentProductsAdapter adapter = new RecentProductsAdapter(productList);
                        binding.rvRecentProducts.setAdapter(adapter);
                        binding.rvRecentProducts.setLayoutManager(
                                new LinearLayoutManager(CustomerDashboardActivity.this, LinearLayoutManager.HORIZONTAL, false)
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
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

    private void loadCustomerName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            binding.customerName.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CustomerDashboardActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                    }
                });
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
