package com.example.justlocal.AdminClass;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.MessageAdapter;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.Models.Message;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityAdminComplaintConversationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminComplaintConversationActivity extends AppCompatActivity {

    private ActivityAdminComplaintConversationBinding binding;
    private String complaintID;
    private DatabaseReference complaintRef;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminComplaintConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get complaintID from intent
        complaintID = getIntent().getStringExtra("complaintID");
        if (complaintID == null) {
            Toast.makeText(this, "Complaint ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, this);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(messageAdapter);

        binding.btnBack.setOnClickListener(v -> finish());

        // Load complaint and messages
        loadComplaintDetails();
    }

    private void loadComplaintDetails() {
        complaintRef = FirebaseDatabase.getInstance().getReference("complaints").child(complaintID);
        complaintRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Complaint complaint = snapshot.getValue(Complaint.class);
                if (complaint != null) {
                    // Populate header and meta info
                    binding.tvHeaderTitle.setText("Complaint #" + complaint.getComplaintID());
                    binding.tvHeaderStatus.setText(complaint.getStatus());

                    binding.tvCustomerID.setText("Customer: " + complaint.getCustomerID());
                    binding.tvOrderID.setText("Order: " + complaint.getOrderID());
                    binding.tvProductID.setText("Product: " + complaint.getProductID());

                    binding.tvCreatedAt.setText("Created: " + complaint.getDateCreated());

                    // Load messages if exist
                    if (complaint.getMessage() != null) {
                        messageList.clear();
                        messageList.addAll(complaint.getMessage());
                        messageAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(AdminComplaintConversationActivity.this, "Complaint not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminComplaintConversationActivity.this, "Failed to load complaint: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}