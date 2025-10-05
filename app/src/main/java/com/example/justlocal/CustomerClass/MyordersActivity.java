package com.example.justlocal.CustomerClass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.DeliveryTracking.DeliveryTrackingActivity;
import com.example.justlocal.Models.Order;
import com.example.justlocal.ProductAdapter.OrdersAdapter;
import com.example.justlocal.R;
import com.example.justlocal.SellerClass.OrderDetailsActivity;
import com.example.justlocal.SellerOrdersAdapter.SellerOrdersAdapter;
import com.example.justlocal.databinding.ActivityMyordersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyordersActivity extends AppCompatActivity {

    private ActivityMyordersBinding binding;
    private List<Order> orderList = new ArrayList<>();
    private OrdersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyordersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());



        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
//        adapter = new OrdersAdapter(orderList);
        adapter = new OrdersAdapter(orderList, order -> {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference("users")
                    .child(userID)
                    .child("role")
                    .get()
                    .addOnSuccessListener(dataSnapshot -> {
                        String userRole = dataSnapshot.getValue(String.class);
                        if (userRole == null) userRole = "customer"; // default fallback

                        Intent intent = new Intent(MyordersActivity.this, DeliveryTrackingActivity.class);
                        intent.putExtra("orderID", order.getOrderID());
                        intent.putExtra("userRole", userRole); // pass actual role
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyordersActivity.this, "Failed to load user role", Toast.LENGTH_SHORT).show();
                    });
        });


        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("orders")
                .orderByChild("customerID").equalTo(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Order order = snap.getValue(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }

                        if (orderList.isEmpty()) {
                            Toast.makeText(MyordersActivity.this, "No orders found", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MyordersActivity.this, "Error loading orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}