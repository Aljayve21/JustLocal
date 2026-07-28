package com.example.justlocal.CustomerClass;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.justlocal.Adapters.NotificationAdapter;
import com.example.justlocal.Models.Notification;
import com.example.justlocal.databinding.ActivityNotificationHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationHistoryActivity extends AppCompatActivity {

    private ActivityNotificationHistoryBinding binding;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);

        loadNotifications();

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseDatabase.getInstance().getReference("notifications")
                .child(userId)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Notification notification = snap.getValue(Notification.class);
                            if (notification != null) {
                                notificationList.add(notification);
                                // Mark as read when viewed
                                if (!notification.isRead()) {
                                    snap.getRef().child("read").setValue(true);
                                }
                            }
                        }
                        Collections.reverse(notificationList);
                        adapter.notifyDataSetChanged();

                        if (notificationList.isEmpty()) {
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvEmpty.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(NotificationHistoryActivity.this, 
                            "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
