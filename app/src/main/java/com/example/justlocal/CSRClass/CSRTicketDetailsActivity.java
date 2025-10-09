package com.example.justlocal.CSRClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justlocal.Models.Complaint;
import com.example.justlocal.databinding.ActivityCsrTicketDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CSRTicketDetailsActivity extends AppCompatActivity {

    private ActivityCsrTicketDetailsBinding binding;
    private DatabaseReference usersRef;
    private DatabaseReference complaintsRef;

    private String complaintID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCsrTicketDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");

        // ✅ Get complaintID passed from previous activity
        complaintID = getIntent().getStringExtra("complaintID");
        if (complaintID != null) {
            loadComplaintDetails(complaintID);
        } else {
            Toast.makeText(this, "Missing complaint ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Load complaint details then fetch the customer full name
     */
    private void loadComplaintDetails(String complaintID) {
        complaintsRef.child(complaintID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Complaint complaint = snapshot.getValue(Complaint.class);
                    if (complaint != null) {
                        // Set complaint fields
                        binding.tvTicketIDHeader.setText("Ticket #" + complaintID);
                        binding.tvComplaintMessage.setText(complaint.getMessage());
                        binding.tvOrderID.setText("Order #" + complaint.getOrderID());
                        binding.tvProductID.setText("Product ID: " + complaint.getProductID());
                        binding.tvCustomerID.setText("Customer ID: " + complaint.getCustomerID());
                        binding.btnRespondTicket.setOnClickListener(v -> {
                            String status = complaint.getStatus();
                            if ("Resolved".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status)) {
                                Toast.makeText(CSRTicketDetailsActivity.this,
                                        "This ticket is already " + status.toLowerCase() + ".",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(CSRTicketDetailsActivity.this, CSRRespondTicketActivity.class);
                                intent.putExtra("complaintID", complaint.getComplaintID());
                                startActivity(intent);
                            }
                        });


                        // ✅ Load Customer Full Name
                        loadCustomerName(complaint.getCustomerID());
                    }
                } else {
                    Toast.makeText(CSRTicketDetailsActivity.this, "Complaint not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CSRTicketDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ Fetch user's full name using customerID and display it
     */
    private void loadCustomerName(String customerID) {
        if (customerID == null) {
            binding.tvCustomerName.setText("Unknown Customer");
            return;
        }

        usersRef.child(customerID).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.getValue(String.class);
                    binding.tvCustomerName.setText(fullName != null ? fullName : "N/A");
                } else {
                    binding.tvCustomerName.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.tvCustomerName.setText("N/A");
            }
        });
    }
}