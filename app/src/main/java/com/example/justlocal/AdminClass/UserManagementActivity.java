package com.example.justlocal.AdminClass;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Models.User;
import com.example.justlocal.UserAdapter.UserAdapter;
import com.example.justlocal.databinding.ActivityUserManagementBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private ActivityUserManagementBinding binding;
    private UserAdapter adapter;
    private List<User> users = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private DatabaseReference usersRef;
    private String currentRoleFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        loadUsers();
    }

    private void setupViews() {
        adapter = new UserAdapter(filteredUsers, this::onUserClick, this::onUserEditClick, this::onUserDeleteClick);
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUsers.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFilter.setOnClickListener(v -> showFilterDialog());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                filterUsers(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed
            }
        });

        setupFilterChips();
    }

    private void setupFilterChips() {
        View.OnClickListener chipListener = v -> {
            resetChipSelection();
            v.setSelected(true);
            String filter = ((TextView) v).getText().toString();
            currentRoleFilter = filter.equals("All Users") ? null : filter;
            filterUsers(binding.etSearch.getText().toString());
        };

        binding.chipAll.setOnClickListener(chipListener);
        binding.chipSellers.setOnClickListener(chipListener);
        binding.chipCustomers.setOnClickListener(chipListener);
        binding.chipCSR.setOnClickListener(chipListener);
        binding.chipAll.setSelected(true);
    }

    private void resetChipSelection() {
        binding.chipAll.setSelected(false);
        binding.chipSellers.setSelected(false);
        binding.chipCustomers.setSelected(false);
        binding.chipCSR.setSelected(false);
    }

    private void loadUsers() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                filterUsers(binding.etSearch.getText().toString());
                updateStats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserManagementActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        for (User user : users) {
            boolean matchesQuery = query.isEmpty() ||
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase()));

            boolean matchesRole = currentRoleFilter == null || (user.getRole() != null && user.getRole().equalsIgnoreCase(currentRoleFilter));

            if (matchesQuery && matchesRole) {
                filteredUsers.add(user);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateStats() {
        int total = users.size();
        int active = 0;
        int inactive = 0;

        for (User user : users) {
            if ("active".equalsIgnoreCase(user.getStatus())) {
                active++;
            } else {
                inactive++;
            }
        }

        binding.tvTotalUsers.setText(String.valueOf(total));
        binding.tvActiveUsers.setText(String.valueOf(active));
        binding.tvInactiveUsers.setText(String.valueOf(inactive));
    }

    private void onUserClick(User user) {
        Toast.makeText(this, "Clicked: " + user.getFullName(), Toast.LENGTH_SHORT).show();
        // Implement further actions, such as opening a detailed view
    }

    private void showFilterDialog() {
        Toast.makeText(this, "Filter dialog clicked", Toast.LENGTH_SHORT).show();
        // Implement filter dialog functionality
    }

    private void onUserEditClick(User user) {
        // Open edit activity, pass User details via Intent
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("userId", user.getId()); // Pass user ID only // Correct label// Make sure User implements Serializable or Parcelable
        startActivity(intent);
    }

    private void onUserDeleteClick(User user) {
        // Confirm deletion or delete directly
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getFullName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    usersRef.child(user.getEmail().replace(".", ",")).removeValue() // Adjust key if needed
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

}
