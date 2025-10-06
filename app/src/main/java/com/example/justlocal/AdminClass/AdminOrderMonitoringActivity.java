package com.example.justlocal.AdminClass;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.AdminOrdersAdapter;
import com.example.justlocal.Models.Order;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityAdminOrderMonitoringBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderMonitoringActivity extends AppCompatActivity {

    private ActivityAdminOrderMonitoringBinding binding;
    private DatabaseReference ordersRef;
    private AdminOrdersAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();
    private final List<Order> filteredList = new ArrayList<>();

    private String currentFilter = "All";
    private String currentSort = "Newest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminOrderMonitoringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase Reference
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Setup RecyclerView
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrdersAdapter(this, filteredList);
        binding.rvOrders.setAdapter(adapter);

        // Load Orders
        loadAllOrders();


        //full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // 🔙 Back Button
        binding.btnBack.setOnClickListener(v -> finish());

        // 🔄 Refresh
        binding.btnRefresh.setOnClickListener(v -> loadAllOrders());

        // 📝 Search Bar
        binding.etSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSearchOrders();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 📌 Filter Buttons
        binding.btnFilterAll.setOnClickListener(v -> setFilter("All"));
        binding.btnFilterPending.setOnClickListener(v -> setFilter("Pending"));
        binding.btnFilterProcessing.setOnClickListener(v -> setFilter("Processing"));
        binding.btnFilterDelivery.setOnClickListener(v -> setFilter("Out for Delivery"));
        binding.btnFilterCompleted.setOnClickListener(v -> setFilter("Completed"));

        // ⏫ Sort Spinner
        binding.spinnerSortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSort = parent.getItemAtPosition(position).toString();
                filterAndSearchOrders();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAllOrders() {
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.rvOrders.setVisibility(View.GONE);

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Order order = ds.getValue(Order.class);
                    if (order != null) {
                        order.setOrderID(ds.getKey());

                        // ⏰ Convert orderDate to timestamp for sorting
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            Date date = sdf.parse(order.getOrderDate());
                            if (date != null) {
                                order.setOrderTimestamp(date.getTime());
                            }
                        } catch (Exception e) {
                            order.setOrderTimestamp(0);
                        }

                        orderList.add(order);
                    }
                }

                updateDashboardCounts();
                filterAndSearchOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrderMonitoringActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void filterAndSearchOrders() {
        String searchText = binding.etSearchOrder.getText().toString().trim().toLowerCase();
        filteredList.clear();

        for (Order order : orderList) {
            boolean matchFilter = false;

            if (currentFilter.equals("All")) {
                matchFilter = true;
            } else if (order.getStatus() != null) {
                String status = order.getStatus();

                if (currentFilter.equalsIgnoreCase("Completed") && status.equalsIgnoreCase("Delivered")) {
                    matchFilter = true;
                } else if (status.equalsIgnoreCase(currentFilter)) {
                    matchFilter = true;
                }
            }

            boolean matchSearch = (order.getOrderID() != null &&
                    order.getOrderID().toLowerCase().contains(searchText));

            if (matchFilter && matchSearch) {
                filteredList.add(order);
            }
        }

        // Sort
        if (currentSort.equals("Newest")) {
            Collections.sort(filteredList, (o1, o2) -> Long.compare(o2.getOrderTimestamp(), o1.getOrderTimestamp()));
        } else if (currentSort.equals("Oldest")) {
            Collections.sort(filteredList, Comparator.comparingLong(Order::getOrderTimestamp));
        }

        adapter.notifyDataSetChanged();

        // Empty state
        if (filteredList.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvOrders.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvOrders.setVisibility(View.VISIBLE);
        }

        binding.tvOrderCount.setText(currentFilter + " Orders (" + filteredList.size() + ")");
    }


    // 📌 Filter buttons style update
    private void setFilter(String filter) {
        currentFilter = filter;
        resetFilterButtons();

        switch (filter) {
            case "All":
                binding.btnFilterAll.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterAll.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case "Pending":
                binding.btnFilterPending.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterPending.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case "Processing":
                binding.btnFilterProcessing.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterProcessing.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case "Out for Delivery":
                binding.btnFilterDelivery.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterDelivery.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case "Completed":
                binding.btnFilterCompleted.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterCompleted.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
        }

        filterAndSearchOrders();
    }


    private void resetFilterButtons() {
        binding.btnFilterAll.setBackgroundTintList(getColorStateList(R.color.gray_200));
        binding.btnFilterPending.setBackgroundTintList(getColorStateList(R.color.gray_200));
        binding.btnFilterProcessing.setBackgroundTintList(getColorStateList(R.color.gray_200));
        binding.btnFilterDelivery.setBackgroundTintList(getColorStateList(R.color.gray_200));
        binding.btnFilterCompleted.setBackgroundTintList(getColorStateList(R.color.gray_200));

        binding.btnFilterAll.setTextColor(getColor(R.color.gray_600));
        binding.btnFilterPending.setTextColor(getColor(R.color.gray_600));
        binding.btnFilterProcessing.setTextColor(getColor(R.color.gray_600));
        binding.btnFilterDelivery.setTextColor(getColor(R.color.gray_600));
        binding.btnFilterCompleted.setTextColor(getColor(R.color.gray_600));
    }

    // 🧮 Dashboard counts (total, pending, etc.)
    private void updateDashboardCounts() {
        int total = orderList.size();
        int pending = 0, processing = 0, completed = 0;

        for (Order order : orderList) {
            if (order.getStatus() == null) continue;
            switch (order.getStatus().toLowerCase()) {
                case "pending":
                    pending++;
                    break;
                case "processing":
                case "out for delivery":
                case "out_for_delivery":
                    processing++;
                    break;
                case "completed":
                case "delivered":
                    completed++;
                    break;
            }
        }

        binding.tvTotalOrders.setText(String.valueOf(total));
        binding.tvPendingOrders.setText(String.valueOf(pending));
        binding.tvProcessingOrders.setText(String.valueOf(processing));
        binding.tvCompletedOrders.setText(String.valueOf(completed));
    }
}
