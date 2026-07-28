package com.example.justlocal.CustomerClass;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.justlocal.Models.Complaint;
import com.example.justlocal.databinding.ActivityCustomerCreateComplaintBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerCreateComplaintActivity extends AppCompatActivity {

    private ActivityCustomerCreateComplaintBinding binding;
    private DatabaseReference complaintsRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerCreateComplaintBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSubmitComplaint.setOnClickListener(v -> submitComplaint());

        // Pre-fill Order ID if passed from Order History
        String prefilledOrderID = getIntent().getStringExtra("orderID");
        if (prefilledOrderID != null) {
            binding.etOrderID.setText(prefilledOrderID);
        }
    }

    private void submitComplaint() {
        String orderID = binding.etOrderID.getText().toString().trim();
        String productID = binding.etProductID.getText().toString().trim();
        String message = binding.etMessage.getText().toString().trim();

        if (orderID.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in Order ID and Message", Toast.LENGTH_SHORT).show();
            return;
        }

        String complaintID = complaintsRef.push().getKey();
        if (complaintID == null) return;

        long currentTime = System.currentTimeMillis();

        Complaint complaint = new Complaint(
                complaintID,
                currentUserID,
                orderID,
                productID,
                message,
                "Pending",
                null,
                null,
                currentTime,
                currentTime,
                0
        );

        complaintsRef.child(complaintID).setValue(complaint)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit complaint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
