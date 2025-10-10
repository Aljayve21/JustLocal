package com.example.justlocal.CSRClass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.AdminClass.EditUserActivity;
import com.example.justlocal.LoginActivity;
import com.example.justlocal.R;
import com.example.justlocal.SellerClass.SellerDashboardActivity;
import com.example.justlocal.databinding.ActivityCsrdashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class CSRDashboardActivity extends AppCompatActivity {

    private ActivityCsrdashboardBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCsrdashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
        setGreetings();
        loadCSRName();
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

    private void loadCSRName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            binding.CSRName.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CSRDashboardActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 🧭 Setup Click Listeners for Quick Actions
     */
    private void setupClickListeners() {

        // 🟦 Support Tickets → Go to CSRSupportTicketActivity
        binding.cardView1.setOnClickListener(v -> {
            Intent intent = new Intent(CSRDashboardActivity.this, CSRSupportTicketActivity.class);
            startActivity(intent);
        });

        // 📝 Complaints Card
        binding.cardScanProduct.setOnClickListener(v -> {
            openProfile();
        });

        // 🛠 Resolve Issues Card
        binding.cardCart.setOnClickListener(v -> {
            // TODO: Add intent for Resolve Issues screen
        });

        // 📜 Ticket History Card
        binding.cardOrders.setOnClickListener(v -> {
            logout();// TODO: Add intent for Ticket History screen
        });
    }

    private void openProfile() {
        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("userId", customerId);
        startActivity(intent);
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
}
