package com.example.justlocal.CSRClass;

import android.app.AlertDialog;
import android.graphics.Color;
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
import com.example.justlocal.Adapters.MessageAdapter;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.Models.Message;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityCsrRespondTicketBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CSRRespondTicketActivity extends AppCompatActivity {

    private ActivityCsrRespondTicketBinding binding;
    private DatabaseReference complaintsRef, messagesRef;
    private String complaintID, csrUserID;

    private boolean isCSRRole;

    private MessageAdapter messageAdapter;
    private ArrayList<Message> messageList = new ArrayList<>();

    private static final String TAG = "CSRRespondTicket";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCsrRespondTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        csrUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        complaintID = getIntent().getStringExtra("complaintID");

        if (complaintID == null) {
            Toast.makeText(this, "Missing complaint ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        binding.btnMarkResolved.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Mark as Resolved")
                    .setMessage("Are you sure you want to mark this ticket as resolved? No further replies will be allowed.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DatabaseReference complaintsRef = FirebaseDatabase.getInstance()
                                .getReference("complaints")
                                .child(complaintID); // galing sa Intent

                        complaintsRef.child("status").setValue("resolved")
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Ticket marked as resolved", Toast.LENGTH_SHORT).show();
                                    binding.tvStatusBadge.setText("Resolved");
                                    binding.tvStatusBadge.setTextColor(Color.parseColor("#059669"));
                                    binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#D1FAE5"));

                                    // Disable input at send button
                                    binding.etResponseInput.setEnabled(false);
                                    binding.btnSendResponse.setEnabled(false);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.btnEscalate.setOnClickListener(v -> {
            DatabaseReference complaintsRef = FirebaseDatabase.getInstance()
                    .getReference("complaints")
                    .child(complaintID);

            complaintsRef.child("status").setValue("escalated")
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Ticket escalated", Toast.LENGTH_SHORT).show();
                        binding.tvStatusBadge.setText("Escalated");
                        binding.tvStatusBadge.setTextColor(Color.parseColor("#DC2626"));
                        binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FEE2E2"));
                    });
        });

        binding.chipTemplate1.setOnClickListener(v ->
                binding.etResponseInput.setText("Checking now")
        );

        binding.chipTemplate2.setOnClickListener(v ->
                binding.etResponseInput.setText("Will update soon")
        );

        binding.chipTemplate3.setOnClickListener(v ->
                binding.etResponseInput.setText("Issue resolved")
        );


        setupRecyclerView();
        loadComplaintDetails();
        listenForMessages();
        setupSendMessage();
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, messageList, csrUserID, isCSRRole);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvCsrMessages.setLayoutManager(layoutManager);
        binding.rvCsrMessages.setAdapter(messageAdapter);
    }

    private void loadComplaintDetails() {
        complaintsRef.child(complaintID).get().addOnSuccessListener(snapshot -> {
            Complaint complaint = snapshot.getValue(Complaint.class);
            if (complaint != null) {
                binding.tvTicketIDHeader.setText("Complaint #" + complaint.getComplaintID());
                binding.tvCustomerNameHeader.setText(complaint.getMessage());
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
                            binding.rvCsrMessages.scrollToPosition(messageList.size() - 1);
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
            binding.layoutEmptyMessages.setVisibility(android.view.View.VISIBLE);
            binding.rvCsrMessages.setVisibility(android.view.View.GONE);
        } else {
            binding.layoutEmptyMessages.setVisibility(android.view.View.GONE);
            binding.rvCsrMessages.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void setupSendMessage() {
        binding.etResponseInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvCharacterCount.setText(s.length() + "/500");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnSendResponse.setOnClickListener(v -> {
            String content = binding.etResponseInput.getText().toString().trim();
            if (content.isEmpty()) return;

            String messageID = messagesRef.push().getKey();
            if (messageID == null) return;

            Message message = new Message(
                    messageID,
                    complaintID,
                    csrUserID,
                    content,
                    System.currentTimeMillis()
            );

            messagesRef.child(messageID).setValue(message)
                    .addOnSuccessListener(unused -> {
                        binding.etResponseInput.setText("");
                        toggleEmptyState();

                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                        usersRef.child(csrUserID).child("fullName").get()
                                .addOnSuccessListener(snapshot -> {
                                    String csrName = snapshot.getValue(String.class);
                                    complaintsRef.child(complaintID).child("repliedBy").setValue(csrName);
                                });

                        complaintsRef.child(complaintID).child("repliedBy").setValue(csrUserID)
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to update repliedBy", e));
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Send message error", e);
                    });



        });
    }
}