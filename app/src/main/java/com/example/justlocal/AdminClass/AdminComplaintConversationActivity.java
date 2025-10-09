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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
    private DatabaseReference complaintRef, messagesRef;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminComplaintConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        complaintID = getIntent().getStringExtra("complaintID");
        if (complaintID == null) {
            Toast.makeText(this, "Complaint ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        binding.btnBack.setOnClickListener(v -> finish());

        // load complaint header info
        loadComplaintDetails();

        // listen for messages under "messages" node
        loadMessages();
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        String adminUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String role = "admin"; // view-only
        boolean isEditable = role.equals("csr");
        messageAdapter = new MessageAdapter(this, messageList, adminUserId, isEditable);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void loadComplaintDetails() {
        complaintRef = FirebaseDatabase.getInstance()
                .getReference("complaints")
                .child(complaintID);

        complaintRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Complaint complaint = snapshot.getValue(Complaint.class);
                if (complaint != null) {
                    binding.tvHeaderTitle.setText("Complaint #" + complaint.getComplaintID());
                    binding.tvHeaderStatus.setText(complaint.getStatus());
                    binding.tvCustomerID.setText("Customer: " + complaint.getCustomerID());
                    binding.tvOrderID.setText("Order: " + complaint.getOrderID());
                    binding.tvProductID.setText("Product: " + complaint.getProductID());
                    binding.tvCreatedAt.setText("Created: " + complaint.getDateCreated());
                } else {
                    Toast.makeText(AdminComplaintConversationActivity.this, "Complaint not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminComplaintConversationActivity.this,
                        "Failed to load complaint: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        messagesRef.orderByChild("complaintID").equalTo(complaintID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            binding.rvMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }

                    @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminComplaintConversationActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}