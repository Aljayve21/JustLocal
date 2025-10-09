package com.example.justlocal.AdminClass;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.AdminComplaintAdapter;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityAdminComplaintsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminComplaintsActivity extends AppCompatActivity {

    private ActivityAdminComplaintsBinding binding;
    private List<Complaint> complaintList;
    private AdminComplaintAdapter adapter; // custom adapter na gagawin mo
    private DatabaseReference complaintsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminComplaintsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        complaintList = new ArrayList<>();
        adapter = new AdminComplaintAdapter(complaintList, this);

        binding.rvAdminComplaints.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminComplaints.setAdapter(adapter);

        complaintsRef = FirebaseDatabase.getInstance().getReference("complaints");

        setupFilters();
        loadComplaints(null); // load all complaints initially

        binding.btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Load complaints from Firebase and filter by status if provided.
     * @param status null = load all
     */
    private void loadComplaints(String status) {
        complaintsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                complaintList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Complaint complaint = child.getValue(Complaint.class);
                    if (complaint != null) {
                        if (status == null || complaint.getStatus().equalsIgnoreCase(status)) {
                            complaintList.add(complaint);
                        }
                    }
                }

                binding.tvTotalCount.setText(String.valueOf(complaintList.size()));
                adapter.notifyDataSetChanged();

                // Show empty layout if no complaints
                if (complaintList.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.rvAdminComplaints.setVisibility(View.GONE);
                } else {
                    binding.layoutEmpty.setVisibility(View.GONE);
                    binding.rvAdminComplaints.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminComplaintsActivity.this, "Failed to load complaints: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Setup filter buttons to load complaints by status
     */
    private void setupFilters() {
        Button btnAll = binding.btnFilterAll;
        Button btnOpen = binding.btnFilterOpen;
        Button btnInReview = binding.btnFilterInReview;
        Button btnResolved = binding.btnFilterResolved;

        btnAll.setOnClickListener(v -> loadComplaints(null));
        btnOpen.setOnClickListener(v -> loadComplaints("Open"));
        btnInReview.setOnClickListener(v -> loadComplaints("In Review"));
        btnResolved.setOnClickListener(v -> loadComplaints("Resolved"));
    }
}