package com.example.justlocal.AdminClass;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.justlocal.Adapters.PaymentAdapter;
import com.example.justlocal.Models.Order;
import com.example.justlocal.Models.Payment;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityAdminPaymentMonitoringBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminPaymentMonitoringActivity extends AppCompatActivity {

    private ActivityAdminPaymentMonitoringBinding binding;
    private PaymentAdapter adapter;

    private final List<Payment> paymentList = new ArrayList<>();
    private final List<Payment> filteredList = new ArrayList<>();

    private String currentFilter = "All";
    private String currentSort = "Newest";

    private Map<String, Order> orderMap = new HashMap<>();
    private Map<String, String> customerNames = new HashMap<>();
    private Map<String, String> sellerNames = new HashMap<>();

    // For async user loading
    private int totalCustomersToLoad = 0;
    private int totalSellersToLoad = 0;
    private int customersLoaded = 0;
    private int sellersLoaded = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminPaymentMonitoringBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }



        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // RecyclerView + Adapter
        binding.rvPayments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentAdapter(this, filteredList, orderMap, customerNames, sellerNames);
        binding.rvPayments.setAdapter(adapter);

        // Load payments + orders + users
        loadPayments();

        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Search
        binding.etSearchPayment.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterAndSearchPayments(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter buttons
        binding.btnFilterAllPayments.setOnClickListener(v -> setFilter("All"));
        binding.btnFilterGcash.setOnClickListener(v -> setFilter("GCash"));
        binding.btnFilterCod.setOnClickListener(v -> setFilter("Cash on Delivery"));
        binding.btnFilterBank.setOnClickListener(v -> setFilter("Bank"));

        // Top cards clickable to filter
        binding.tvGcashAmount.setOnClickListener(v -> setFilter("GCash"));
        binding.tvCodAmount.setOnClickListener(v -> setFilter("Cash on Delivery"));
        binding.tvBankAmount.setOnClickListener(v -> setFilter("Bank"));
        binding.tvTotalRevenue.setOnClickListener(v -> setFilter("All"));

        // Sort spinner
        binding.spinnerDateRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSort = parent.getItemAtPosition(position).toString();
                filterAndSearchPayments();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // -------------------------
    // Load payments → orders → users
    // -------------------------
    private void loadPayments() {
        FirebaseDatabase.getInstance().getReference("payments")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        paymentList.clear();
                        if (!snapshot.exists()) {
                            binding.layoutEmptyState.setVisibility(View.VISIBLE);
                            Log.d("PAYMENTS_DEBUG", "No payments found in DB.");
                            return;
                        }

                        for (DataSnapshot data : snapshot.getChildren()) {
                            Payment p = data.getValue(Payment.class);
                            if (p != null) paymentList.add(p);
                        }

                        loadOrders();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PAYMENTS_DEBUG", "Error loading payments: " + error.getMessage());
                    }
                });
    }

    private void loadOrders() {
        FirebaseDatabase.getInstance().getReference("orders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderMap.clear();

                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                Order order = data.getValue(Order.class);
                                if (order != null && order.getOrderID() != null) {
                                    orderMap.put(order.getOrderID(), order);
                                }
                            }
                        }

                        loadUserNames();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ORDERS_DEBUG", "Failed to load orders: " + error.getMessage());
                    }
                });
    }

    private void loadUserNames() {
        customerNames.clear();
        sellerNames.clear();
        customersLoaded = 0;
        sellersLoaded = 0;

        List<String> customerIDs = new ArrayList<>();
        List<String> sellerIDs = new ArrayList<>();

        for (Order order : orderMap.values()) {
            if (order.getCustomerID() != null && !customerNames.containsKey(order.getCustomerID()))
                customerIDs.add(order.getCustomerID());
            if (order.getSellerID() != null && !sellerNames.containsKey(order.getSellerID()))
                sellerIDs.add(order.getSellerID());
        }

        totalCustomersToLoad = customerIDs.size();
        totalSellersToLoad = sellerIDs.size();

        for (String customerID : customerIDs) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(customerID)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String fullName = snapshot.exists() ? snapshot.child("fullName").getValue(String.class) : null;
                        customerNames.put(customerID, fullName != null ? fullName : "Unknown Customer");
                        customersLoaded++;
                        checkAllUsersLoaded();
                    })
                    .addOnFailureListener(e -> {
                        customerNames.put(customerID, "Unknown Customer");
                        customersLoaded++;
                        checkAllUsersLoaded();
                    });
        }

        for (String sellerID : sellerIDs) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(sellerID)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String fullName = snapshot.exists() ? snapshot.child("fullName").getValue(String.class) : null;
                        sellerNames.put(sellerID, fullName != null ? fullName : "Unknown Seller");
                        sellersLoaded++;
                        checkAllUsersLoaded();
                    })
                    .addOnFailureListener(e -> {
                        sellerNames.put(sellerID, "Unknown Seller");
                        sellersLoaded++;
                        checkAllUsersLoaded();
                    });
        }

        // If there’s nothing to load, update stats immediately
        if (totalCustomersToLoad == 0 && totalSellersToLoad == 0) {
            adapter.notifyDataSetChanged();
            updatePaymentStats();
            filterAndSearchPayments();
        }
    }

    private void checkAllUsersLoaded() {
        if (customersLoaded >= totalCustomersToLoad && sellersLoaded >= totalSellersToLoad) {
            adapter.notifyDataSetChanged();
            filterAndSearchPayments();
            updatePaymentStats(); // only call here
        }
    }


    // -------------------------
    // Filter & Search
    // -------------------------
    private void filterAndSearchPayments() {
        String searchText = binding.etSearchPayment.getText().toString().trim().toLowerCase();
        filteredList.clear();

        for (Payment payment : paymentList) {
            if (payment.getStatus() == null || !payment.getStatus().equalsIgnoreCase("Paid")) continue;

            boolean matchFilter = currentFilter.equals("All") ||
                    (payment.getMethod() != null && payment.getMethod().equalsIgnoreCase(currentFilter));

            Order order = orderMap.get(payment.getOrderID());
            String customerName = order != null && customerNames.containsKey(order.getCustomerID())
                    ? customerNames.get(order.getCustomerID()) : "";
            String sellerName = order != null && sellerNames.containsKey(order.getSellerID())
                    ? sellerNames.get(order.getSellerID()) : "";

            boolean matchSearch = (payment.getOrderID() != null && payment.getOrderID().toLowerCase().contains(searchText)) ||
                    (payment.getReference() != null && payment.getReference().toLowerCase().contains(searchText)) ||
                    customerName.toLowerCase().contains(searchText) ||
                    sellerName.toLowerCase().contains(searchText);

            if (matchFilter && matchSearch) filteredList.add(payment);
        }

        if (currentSort.equals("Newest")) {
            Collections.sort(filteredList, (p1, p2) -> Long.compare(getTimestamp(p2.getPaidAt()), getTimestamp(p1.getPaidAt())));
        } else {
            Collections.sort(filteredList, Comparator.comparingLong(p -> getTimestamp(p.getPaidAt())));
        }

        adapter.notifyDataSetChanged();

        binding.layoutEmptyState.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvPayments.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);

        binding.tvPaymentCount.setText("Recent Payments (" + filteredList.size() + ")");
    }

    private Long getTimestamp(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    // -------------------------
    // Filter buttons UI
    // -------------------------
    private void setFilter(String filter) {
        currentFilter = filter;

        binding.btnFilterAllPayments.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_200));
        binding.btnFilterGcash.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_200));
        binding.btnFilterCod.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_200));
        binding.btnFilterBank.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_200));

        binding.btnFilterAllPayments.setTextColor(ContextCompat.getColor(this, R.color.gray_600));
        binding.btnFilterGcash.setTextColor(ContextCompat.getColor(this, R.color.gray_600));
        binding.btnFilterCod.setTextColor(ContextCompat.getColor(this, R.color.gray_600));
        binding.btnFilterBank.setTextColor(ContextCompat.getColor(this, R.color.gray_600));

        switch (filter) {
            case "All":
                binding.btnFilterAllPayments.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterAllPayments.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case "GCash":
                binding.btnFilterGcash.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterGcash.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case "Cash on Delivery":
                binding.btnFilterCod.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterCod.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            case "Bank":
                binding.btnFilterBank.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_600));
                binding.btnFilterBank.setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
        }

        filterAndSearchPayments();
    }

    // -------------------------
    // Payment Statistics
    // -------------------------
    private void updatePaymentStats() {
        double totalRevenue = 0, gcashTotal = 0, codTotal = 0, bankTotal = 0;
        int gcashCount = 0, codCount = 0, bankCount = 0;

        for (Payment p : paymentList) {
            if (p.getStatus() == null || !p.getStatus().equalsIgnoreCase("Paid")) continue;

            double amount = 0;
            Order order = orderMap.get(p.getOrderID());
            if (order != null) {
                try { amount = Double.parseDouble(order.getTotalAmount()); }
                catch (Exception e) { amount = 0; }
            }

            totalRevenue += amount;

            if (p.getMethod() != null) {
                switch (p.getMethod().toLowerCase()) { // handle case-insensitive
                    case "gcash": gcashTotal += amount; gcashCount++; break;
                    case "cash on delivery": codTotal += amount; codCount++; break;
                    case "bank": bankTotal += amount; bankCount++; break;
                }
            }
        }

        binding.tvTotalRevenue.setText("₱" + String.format("%,.2f", totalRevenue));
        binding.tvGcashAmount.setText("₱" + String.format("%,.2f", gcashTotal));
        binding.tvGcashCount.setText(gcashCount + " transactions");
        binding.tvCodAmount.setText("₱" + String.format("%,.2f", codTotal));
        binding.tvCodCount.setText(codCount + " transactions");
        binding.tvBankAmount.setText("₱" + String.format("%,.2f", bankTotal));
        binding.tvBankCount.setText(bankCount + " transactions");
    }
}
