package com.example.justlocal.SellerClass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Models.Order;
import com.example.justlocal.ProductAdapter.OrdersAdapter;
import com.example.justlocal.R;
import com.example.justlocal.SellerOrdersAdapter.SellerOrdersAdapter;
import com.example.justlocal.databinding.ActivityOrderMonitoringBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderMonitoringActivity extends AppCompatActivity {

    private ActivityOrderMonitoringBinding binding;
    private SellerOrdersAdapter adapter; // ✅ You're using SellerOrdersAdapter, not OrdersAdapter
    private ArrayList<Order> orderList = new ArrayList<>();

    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderMonitoringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        setupRecyclerView();
        fetchOrdersForCurrentSeller();
        setupBackButton();
    }

    private void setupRecyclerView() {
        adapter = new SellerOrdersAdapter(this, orderList, order -> {
            Intent intent = new Intent(this, OrderDetailsActivity.class);
            intent.putExtra("orderID", order.getOrderID());
            startActivity(intent);
        });

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }



    private void fetchOrdersForCurrentSeller() {
        String currentSellerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ordersRef.orderByChild("sellerID").equalTo(currentSellerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();

                        if (!snapshot.exists()) {
                            Toast.makeText(OrderMonitoringActivity.this, "No orders found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Order order = orderSnap.getValue(Order.class);
                            if (order != null) {
                                orderList.add(order);
                                System.out.println("Loaded Order: " + order.getOrderID()); // Debug
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderMonitoringActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
