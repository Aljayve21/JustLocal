package com.example.justlocal.CustomerClass;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.ComplaintMessageAdapter;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.Models.Message;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityCustomerAddComplaintMessageBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CustomerAddComplaintMessage extends AppCompatActivity {

    private ActivityCustomerAddComplaintMessageBinding binding;
    private DatabaseReference complaintsRef, messagesRef;
    private String complaintID, currentUserID;

    private ComplaintMessageAdapter messageAdapter;
    private ArrayList<Message> messageList = new ArrayList<>();

    private static final String TAG = "ComplaintMessageAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerAddComplaintMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        complaintID = getIntent().getStringExtra("complaintID");

        if (complaintID == null) {
            Toast.makeText(this, "Missing complaint ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        setupRecyclerView();
        loadComplaintDetails();
        listenForMessages();
        setupSendMessage();
    }

    private void setupRecyclerView() {
        messageAdapter = new ComplaintMessageAdapter(messageList, currentUserID);
        binding.rvComplaintMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvComplaintMessages.setAdapter(messageAdapter);
    }

    private void loadComplaintDetails() {
        complaintsRef.child(complaintID).get().addOnSuccessListener(snapshot -> {
            Complaint complaint = snapshot.getValue(Complaint.class);
            if (complaint != null) {
                binding.tvComplaintIDHeader.setText("Complaint #" + complaint.getComplaintID());
                binding.tvOriginalComplaint.setText(complaint.getMessage());
                binding.tvOrderIDExpanded.setText(complaint.getOrderID());
                binding.tvCreatedDateExpanded.setText(formatDate(complaint.getDateCreated()));

                String status = complaint.getStatus();
                binding.tvStatusBadge.setText(status);

                if ("Resolved".equalsIgnoreCase(status)) {
                    binding.tvStatusBadge.setTextColor(0xFF059669);
                    binding.tvStatusBadge.setBackgroundColor(0xFFD1FAE5);
                } else if ("Closed".equalsIgnoreCase(status)) {
                    binding.tvStatusBadge.setTextColor(0xFF6B7280);
                    binding.tvStatusBadge.setBackgroundColor(0xFFE5E7EB);
                } else {
                    binding.tvStatusBadge.setTextColor(0xFFB45309);
                    binding.tvStatusBadge.setBackgroundColor(0xFFFDE68A);
                }

                // ✅ Disable reply UI if complaint is Resolved or Closed
                if ("Resolved".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status)) {
                    binding.etMessageInput.setEnabled(false);
                    binding.etMessageInput.setHint("This ticket is " + status.toLowerCase() + ".");
                    binding.btnSendMessage.setEnabled(false);
                    binding.btnSendMessage.setAlpha(0.5f);  // visual feedback
                } else {
                    binding.etMessageInput.setEnabled(true);
                    binding.btnSendMessage.setEnabled(true);
                    binding.btnSendMessage.setAlpha(1.0f);
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading complaint", e));
    }


    private void listenForMessages() {
        messagesRef.orderByChild("complaintID").equalTo(complaintID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            binding.rvComplaintMessages.scrollToPosition(messageList.size() - 1);
                            toggleEmptyState();
                        }
                    }

                    @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to listen for messages", error.toException());
                    }
                });
    }

    private void toggleEmptyState() {
        if (messageList.isEmpty()) {
            binding.layoutEmptyMessages.setVisibility(View.VISIBLE);
            binding.rvComplaintMessages.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyMessages.setVisibility(View.GONE);
            binding.rvComplaintMessages.setVisibility(View.VISIBLE);
        }
    }

    private void setupSendMessage() {
        // character count
        binding.etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvCharacterCount.setText(s.length() + "/500");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // send button
        binding.btnSendMessage.setOnClickListener(v -> {
            String content = binding.etMessageInput.getText().toString().trim();
            if (content.isEmpty()) return;

            String messageID = messagesRef.push().getKey();
            if (messageID == null) return;

            Message message = new Message(
                    messageID,
                    complaintID,
                    currentUserID,
                    content,
                    System.currentTimeMillis()
            );

            messagesRef.child(messageID).setValue(message)
                    .addOnSuccessListener(unused -> {
                        binding.etMessageInput.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Send message error", e);
                    });
        });
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}