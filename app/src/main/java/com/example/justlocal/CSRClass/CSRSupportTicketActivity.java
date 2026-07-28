package com.example.justlocal.CSRClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.TicketAdapter;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.Models.Notification;
import com.example.justlocal.databinding.ActivityCsrsupportTicketBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSRSupportTicketActivity extends AppCompatActivity {

    private ActivityCsrsupportTicketBinding b;
    private TicketAdapter adapter;
    private List<Complaint> allTickets = new ArrayList<>();
    private List<Complaint> filteredTickets = new ArrayList<>();
    private Map<String, String> userIdToFullName = new HashMap<>();
    private DatabaseReference complaintsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCsrsupportTicketBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        setupRecyclerView();
        setupListeners();

        // Check for filter from Intent
        String filter = getIntent().getStringExtra("filterStatus");
        if (filter != null) {
            preloadUsers(() -> {
                loadTicketsWithFilter(filter);
            });
        } else {
            // Preload users first, then tickets
            preloadUsers(this::loadTickets);
        }
    }

    private void loadTicketsWithFilter(String filter) {
        allTickets.clear();
        complaintsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Complaint c = child.getValue(Complaint.class);
                    if (c != null) {
                        allTickets.add(c);
                    }
                }
                filterTicketsByStatus(filter);
                updateStats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toggleEmptyState();
            }
        });
    }

    /** 🔹 Setup RecyclerView */
    private void setupRecyclerView() {
        b.rvTickets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TicketAdapter(this, filteredTickets, userIdToFullName, new TicketAdapter.OnTicketClickListener() {
            @Override
            public void onViewDetails(Complaint complaint) {
                DatabaseReference complaintRef = FirebaseDatabase.getInstance()
                        .getReference("complaints")
                        .child(complaint.getComplaintID());

                complaintRef.child("status").get().addOnSuccessListener(snapshot -> {
                    String currentStatus = snapshot.getValue(String.class);
                    if ("Open".equalsIgnoreCase(currentStatus)) {
                        complaintRef.child("status").setValue("In Progress");
                        sendNotificationToCustomer(complaint.getCustomerID(), "Complaint In Progress",
                                "Your complaint #" + complaint.getComplaintID() + " is now being reviewed by a CSR.");
                    }

                    // 👇 Open the ticket details activity
                    Intent intent = new Intent(CSRSupportTicketActivity.this, CSRTicketDetailsActivity.class);
                    intent.putExtra("complaintID", complaint.getComplaintID());
                    startActivity(intent);
                });
            }

            @Override
            public void onTakeTicket(Complaint complaint) {
                // TODO: Assign this ticket to CSR user if needed
            }
        });
        b.rvTickets.setAdapter(adapter);
    }

    /** 🔹 Setup click and search listeners */
    private void setupListeners() {
        b.btnBack.setOnClickListener(v -> finish());
        b.btnRefresh.setOnClickListener(v -> loadTickets());

        // Search functionality
        b.etSearchTicket.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTicketsBySearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Buttons
        b.btnFilterAll.setOnClickListener(v -> filterTicketsByStatus("All"));
        b.btnFilterOpen.setOnClickListener(v -> filterTicketsByStatus("Open"));
        b.btnFilterInProgress.setOnClickListener(v -> filterTicketsByStatus("In Progress"));
        b.btnFilterResolved.setOnClickListener(v -> filterTicketsByStatus("Resolved"));
        b.btnFilterClosed.setOnClickListener(v -> filterTicketsByStatus("Closed"));
    }

    /** 🔹 Preload users into map */
    private void preloadUsers(Runnable onComplete) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIdToFullName.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String id = userSnap.getKey();
                    String fullName = userSnap.child("fullName").getValue(String.class);
                    if (id != null && fullName != null) {
                        userIdToFullName.put(id, fullName);
                    }
                }
                onComplete.run(); // Continue loading tickets
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /** 🔹 Load tickets from Firebase */
    private void loadTickets() {
        allTickets.clear();
        complaintsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Complaint c = child.getValue(Complaint.class);
                    if (c != null) {
                        allTickets.add(c);
                    }
                }
                filteredTickets.clear();
                filteredTickets.addAll(allTickets);
                adapter.notifyDataSetChanged();
                updateStats();
                toggleEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toggleEmptyState();
            }
        });
    }

    /** 🔹 Filter tickets by status */
    private void filterTicketsByStatus(String status) {
        filteredTickets.clear();
        if (status.equalsIgnoreCase("All")) {
            filteredTickets.addAll(allTickets);
        } else {
            for (Complaint c : allTickets) {
                if (c.getStatus() != null && c.getStatus().equalsIgnoreCase(status)) {
                    filteredTickets.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    /** 🔹 Filter tickets by search query */
    private void filterTicketsBySearch(String query) {
        filteredTickets.clear();
        String q = query.toLowerCase();

        for (Complaint c : allTickets) {
            boolean matches = false;

            // Check complaintID
            if (c.getComplaintID() != null && c.getComplaintID().toLowerCase().contains(q)) matches = true;
                // Check message
            else if (c.getMessage() != null && c.getMessage().toLowerCase().contains(q)) matches = true;
                // Check customer fullName
            else {
                String fullName = userIdToFullName.getOrDefault(c.getCustomerID(), "");
                if (fullName.toLowerCase().contains(q)) matches = true;
            }

            if (matches) filteredTickets.add(c);
        }

        adapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    /** 🔹 Update ticket statistics cards */
    private void updateStats() {
        int total = allTickets.size();
        int open = 0, inProgress = 0, resolved = 0, closed = 0;

        for (Complaint c : allTickets) {
            if (c.getStatus() == null) continue;
            switch (c.getStatus()) {
                case "Open": open++; break;
                case "In Progress": inProgress++; break;
                case "Resolved": resolved++; break;
                case "Closed": closed++; break;
            }
        }

        b.tvTicketCount.setText("All Tickets (" + total + ")");
        b.tvTotalTickets.setText(String.valueOf(total));
        b.tvOpenTickets.setText(String.valueOf(open));
        b.tvInProgressTickets.setText(String.valueOf(inProgress));
        b.tvResolvedTickets.setText(String.valueOf(resolved));
//        b.tvClosedTickets.setText(String.valueOf(closed));
    }

    /** 🔹 Toggle empty state visibility */
    private void toggleEmptyState() {
        if (filteredTickets.isEmpty()) {
            b.layoutEmptyState.setVisibility(View.VISIBLE);
            b.rvTickets.setVisibility(View.GONE);
        } else {
            b.layoutEmptyState.setVisibility(View.GONE);
            b.rvTickets.setVisibility(View.VISIBLE);
        }
    }

    private void sendNotificationToCustomer(String customerID, String title, String message) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications").child(customerID);
        String notifId = notifRef.push().getKey();
        if (notifId == null) return;

        Notification notification = new Notification(
                notifId,
                title,
                message,
                System.currentTimeMillis(),
                "complaint"
        );

        notifRef.child(notifId).setValue(notification);
    }
}
