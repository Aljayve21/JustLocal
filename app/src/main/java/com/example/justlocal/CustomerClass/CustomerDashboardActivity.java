package com.example.justlocal.CustomerClass;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.Models.Order;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityCustomerDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerDashboardActivity extends AppCompatActivity {

    private ActivityCustomerDashboardBinding binding;
  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityCustomerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.cardView1.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, BrowseProductsActivity.class);
            startActivity(intent);
        });

        binding.cardOrders.setOnClickListener(v -> {
            String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent = new Intent(CustomerDashboardActivity.this, MyordersActivity.class);
            intent.putExtra("customerID", currentUserID);
            startActivity(intent);
        });
        }




    }


