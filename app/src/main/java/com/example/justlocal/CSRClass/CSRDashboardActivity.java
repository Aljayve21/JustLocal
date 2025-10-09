package com.example.justlocal.CSRClass;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityCsrdashboardBinding;

public class CSRDashboardActivity extends AppCompatActivity {

    private ActivityCsrdashboardBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCsrdashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
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
            // TODO: Add intent for Complaints Management
        });

        // 🛠 Resolve Issues Card
        binding.cardCart.setOnClickListener(v -> {
            // TODO: Add intent for Resolve Issues screen
        });

        // 📜 Ticket History Card
        binding.cardOrders.setOnClickListener(v -> {
            // TODO: Add intent for Ticket History screen
        });
    }
}
