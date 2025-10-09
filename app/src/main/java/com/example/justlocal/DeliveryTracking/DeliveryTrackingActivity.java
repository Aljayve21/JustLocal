package com.example.justlocal.DeliveryTracking;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityDeliveryTrackingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DeliveryTrackingActivity extends AppCompatActivity {

    private ActivityDeliveryTrackingBinding binding;
    private DatabaseReference ordersRef;
    private String orderID, customerID, totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // Firebase Reference
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Get order details from intent
        orderID = getIntent().getStringExtra("orderID");
        loadOrderDetails();

        customerID = getIntent().getStringExtra("customerID");
        totalAmount = getIntent().getStringExtra("totalAmount");

        // Set initial info
        binding.tvOrderID.setText("Order ID: " + orderID);
        binding.tvCustomerID.setText("Customer ID: " + customerID);
        binding.tvTotalAmount.setText("Total Amount: ₱" + totalAmount);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.delivery_carriers,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCarrier.setAdapter(adapter);

        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        checkUserRoleAndHideSetup();

        // Start Delivery Process
        binding.btnStartDelivery.setOnClickListener(v -> startDelivery());

        // Update status buttons
        binding.btnOutForDelivery.setOnClickListener(v -> updateStatus("Out for Delivery"));
        binding.btnDelivered.setOnClickListener(v -> updateStatus("Delivered"));
    }

    private void checkUserRoleAndHideSetup() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        userRef.child("role").get().addOnSuccessListener(snapshot -> {
            String role = snapshot.getValue(String.class);

            if (role != null && role.equals("Customer")) {
                // Hide the entire Delivery Setup card
                binding.layoutDeliverySetup.setVisibility(View.GONE);

                // Hide update buttons if any
                binding.btnOutForDelivery.setVisibility(View.GONE);
                binding.btnDelivered.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to determine user role", Toast.LENGTH_SHORT).show();
        });
    }


    private void startDelivery() {
        String carrier = binding.spinnerCarrier.getSelectedItem().toString();
        String trackingNo = binding.etTrackingNumber.getText().toString().trim();

        if (trackingNo.isEmpty()) {
            Toast.makeText(this, "Enter tracking number", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("carrier", carrier);
        updates.put("trackingNumber", trackingNo);
        updates.put("status", "processing");
        updates.put("processingTime", getCurrentTime());

        ordersRef.child(orderID).updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Delivery started", Toast.LENGTH_SHORT).show();
            binding.layoutDeliveryStatus.setVisibility(View.VISIBLE);
            binding.layoutDeliveryTimeline.setVisibility(View.VISIBLE);

            binding.tvCarrierInfo.setText("Carrier: " + carrier);
            binding.tvTrackingInfo.setText("Tracking No: " + trackingNo);
            binding.tvCurrentStatus.setText("Processing");
            binding.tvProcessingTime.setText(getCurrentTime());
            binding.timelineProcessing.setVisibility(View.VISIBLE);
        });
    }

    private void loadOrderDetails() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(orderID);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Basic Info
                    String customerID = snapshot.child("customerID").getValue(String.class);
                    String totalAmount = snapshot.child("totalAmount").getValue(String.class);

                    binding.tvCustomerID.setText("Customer ID: " + (customerID != null ? customerID : "N/A"));

                    try {
                        double total = Double.parseDouble(totalAmount != null ? totalAmount : "0");
                        binding.tvTotalAmount.setText("Total Amount: ₱" + String.format("%.2f", total));
                    } catch (NumberFormatException e) {
                        binding.tvTotalAmount.setText("Total Amount: ₱0.00");
                    }

                    String customerName = snapshot.child("customerName").getValue(String.class);
                    String address = snapshot.child("deliveryAddress").getValue(String.class);
                    String contact = snapshot.child("contactNumber").getValue(String.class);

                    if (customerName != null) binding.tvCustomerName.setText(customerName);
                    if (address != null) binding.tvDeliveryAddress.setText(address);
                    if (contact != null) binding.tvCustomerContact.setText("Contact: " + contact);

                    // ✅ Delivery Tracking Info
                    String status = snapshot.child("status").getValue(String.class);
                    String carrier = snapshot.child("carrier").getValue(String.class);
                    String trackingNo = snapshot.child("trackingNumber").getValue(String.class);

                    String processingTime = snapshot.child("processingTime").getValue(String.class);
                    String outForDeliveryTime = snapshot.child("outForDeliveryTime").getValue(String.class);
                    String deliveredTime = snapshot.child("deliveredTime").getValue(String.class);

                    if (carrier != null) binding.tvCarrierInfo.setText("Carrier: " + carrier);
                    if (trackingNo != null) binding.tvTrackingInfo.setText("Tracking No: " + trackingNo);

                    if (status != null) {
                        binding.tvCurrentStatus.setText(status);
                        binding.layoutDeliveryStatus.setVisibility(View.VISIBLE);
                        binding.layoutDeliveryTimeline.setVisibility(View.VISIBLE);

                        switch (status) {
                            case "processing":
                                if (processingTime != null) {
                                    binding.tvProcessingTime.setText(processingTime);
                                    binding.timelineProcessing.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "Out for Delivery":
                                if (processingTime != null) {
                                    binding.tvProcessingTime.setText(processingTime);
                                    binding.timelineProcessing.setVisibility(View.VISIBLE);
                                }
                                if (outForDeliveryTime != null) {
                                    binding.tvOutForDeliveryTime.setText(outForDeliveryTime);
                                    binding.timelineOutForDelivery.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "Delivered":
                                if (processingTime != null) {
                                    binding.tvProcessingTime.setText(processingTime);
                                    binding.timelineProcessing.setVisibility(View.VISIBLE);
                                }
                                if (outForDeliveryTime != null) {
                                    binding.tvOutForDeliveryTime.setText(outForDeliveryTime);
                                    binding.timelineOutForDelivery.setVisibility(View.VISIBLE);
                                }
                                if (deliveredTime != null) {
                                    binding.tvDeliveredTime.setText(deliveredTime);
                                    binding.timelineDelivered.setVisibility(View.VISIBLE);
                                }
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void updateStatus(String newStatus) {
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        if (newStatus.equals("Out for Delivery")) {
            updates.put("outForDeliveryTime", getCurrentTime());
        } else if (newStatus.equals("Delivered")) {
            updates.put("deliveredTime", getCurrentTime());
        }

        ordersRef.child(orderID).updateChildren(updates).addOnSuccessListener(aVoid -> {
            binding.tvCurrentStatus.setText(newStatus);

            if (newStatus.equals("Out for Delivery")) {
                binding.tvOutForDeliveryTime.setText(getCurrentTime());
                binding.timelineOutForDelivery.setVisibility(View.VISIBLE);
            } else if (newStatus.equals("Delivered")) {
                binding.tvDeliveredTime.setText(getCurrentTime());
                binding.timelineDelivered.setVisibility(View.VISIBLE);
            }
        });
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault()).format(new Date());
    }
}
