package com.example.justlocal.CustomerClass;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.ReportAdapter;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.databinding.ActivityCustomerReportHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerReportHistoryActivity extends AppCompatActivity {

    private ActivityCustomerReportHistoryBinding binding;
    private ReportAdapter reportAdapter;
    private final List<Complaint> allComplaints = new ArrayList<>();
    private final List<Complaint> filteredComplaints = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerReportHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setupUI();
        loadComplaintsFromFirebase();
    }

    private void setupUI() {
        // 🔙 Back Button
        binding.btnBack.setOnClickListener(v -> finish());

        // ➕ Create Report Button (Optional)
        binding.btnCreateReport.setOnClickListener(v -> {
            // TODO: Open Create Report Activity
        });

        // ✍️ Search Function
        binding.etSearchReport.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterComplaintsBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 🌀 RecyclerView Setup
        binding.rvReports.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(filteredComplaints);
        binding.rvReports.setAdapter(reportAdapter);

        // 🟦 Filter Buttons
        binding.btnFilterAllReports.setOnClickListener(v -> filterByStatus("All"));
        binding.btnFilterPending.setOnClickListener(v -> filterByStatus("Pending"));
        binding.btnFilterInReview.setOnClickListener(v -> filterByStatus("In Review"));
        binding.btnFilterResolved.setOnClickListener(v -> filterByStatus("Resolved"));
    }

    /**
     * 📡 Load Complaints from Firebase Realtime DB (based on current customer)
     */
    private void loadComplaintsFromFirebase() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference complaintsRef = FirebaseDatabase.getInstance()
                .getReference("complaints");

        complaintsRef.orderByChild("customerID").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allComplaints.clear();

                        for (DataSnapshot complaintSnap : snapshot.getChildren()) {
                            Complaint complaint = complaintSnap.getValue(Complaint.class);
                            if (complaint != null) {
                                allComplaints.add(complaint);
                            }
                        }

                        updateSummaryCards();
                        filteredComplaints.clear();
                        filteredComplaints.addAll(allComplaints);
                        reportAdapter.notifyDataSetChanged();
                        toggleEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Optional: Log error
                    }
                });
    }

    private void updateSummaryCards() {
        int total = allComplaints.size();
        int pending = 0;
        int resolved = 0;
        int inReview = 0;

        for (Complaint c : allComplaints) {
            if (c.getStatus() == null) continue;

            switch (c.getStatus()) {
                case "Pending":
                    pending++;
                    break;
                case "Resolved":
                    resolved++;
                    break;
                case "In Review":
                    inReview++;
                    break;
            }
        }

        binding.tvTotalReports.setText(String.valueOf(total));
        binding.tvPendingReports.setText(String.valueOf(pending));
        binding.tvResolvedReports.setText(resolved + " out of " + total);
        int percent = total == 0 ? 0 : (resolved * 100 / total);
        binding.tvResolvedPercentage.setText(percent + "%");
        binding.tvReportCount.setText("All Reports (" + total + ")");
    }

    private void filterComplaintsBySearch(String query) {
        filteredComplaints.clear();
        for (Complaint c : allComplaints) {
            if (c.getMessage() != null &&
                    c.getMessage().toLowerCase().contains(query.toLowerCase())) {
                filteredComplaints.add(c);
            }
        }
        reportAdapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    private void filterByStatus(String status) {
        filteredComplaints.clear();

        if (status.equalsIgnoreCase("All")) {
            filteredComplaints.addAll(allComplaints);
        } else {
            for (Complaint c : allComplaints) {
                if (c.getStatus() != null && c.getStatus().equalsIgnoreCase(status)) {
                    filteredComplaints.add(c);
                }
            }
        }

        reportAdapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    private void toggleEmptyState() {
        if (filteredComplaints.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvReports.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvReports.setVisibility(View.VISIBLE);
        }
    }
}