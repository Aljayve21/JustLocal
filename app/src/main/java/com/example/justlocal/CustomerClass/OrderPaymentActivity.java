package com.example.justlocal.CustomerClass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.Models.Address;
import com.example.justlocal.Models.Order;
import com.example.justlocal.Models.OrderItems;
import com.example.justlocal.Models.Payment;
import com.example.justlocal.Models.Product;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityOrderPaymentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class OrderPaymentActivity extends AppCompatActivity {

    private ActivityOrderPaymentBinding binding;
    private String productID;
    private int quantity;
    private Product product;
    private Address address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrderPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productID = getIntent().getStringExtra("productID");
        quantity = getIntent().getIntExtra("quantity", 1);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnConfirmPayment.setOnClickListener(v -> confirmPayment());

        binding.btnChangeAddress.setOnClickListener(v -> {
            if (address != null) {
                Intent intent = new Intent(OrderPaymentActivity.this, UserAddressActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("addressID", address.getAddressID());
                intent.putExtra("type", address.getType());
                intent.putExtra("street", address.getStreet());
                intent.putExtra("city", address.getCity());
                intent.putExtra("province", address.getProvince());
                intent.putExtra("postalCode", address.getPostalCode());
                intent.putExtra("country", address.getCountry());
                intent.putExtra("contactNo", address.getContactNo());
                intent.putExtra("isDefault", address.isDefaultAddress());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No address loaded to edit", Toast.LENGTH_SHORT).show();
            }
        });


        loadProductDetails();
        loadDefaultAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductDetails();
        if (getIntent().getBooleanExtra("addressUpdated", false)) {
            Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show();
            getIntent().removeExtra("addressUpdated"); // clear flag if needed
        }
    }

    private void loadProductDetails() {
        FirebaseDatabase.getInstance().getReference("products")
                .child(productID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        product = snapshot.getValue(Product.class);
                        if (product != null) {
                            double price = Double.parseDouble(product.getPrice());
                            double total = price * quantity;

                            binding.tvProductName.setText(product.getProductName());
                            binding.tvProductPrice.setText("₱" + product.getPrice());
                            binding.tvProductQuantity.setText("Quantity: " + quantity);
                            binding.tvItemTotal.setText("₱" + String.format(Locale.getDefault(), "%.2f", total));
                            binding.tvTotalAmount.setText("₱" + String.format(Locale.getDefault(), "%.2f", total));

                            if (product.getImage() != null) {
                                byte[] decodedBytes = Base64.decode(product.getImage(), Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                binding.ivProductImage.setImageBitmap(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderPaymentActivity.this, "Error loading product", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadDefaultAddress() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("addresses");

        addressRef.orderByChild("userID").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean foundDefault = false;

                        for (DataSnapshot addressSnap : snapshot.getChildren()) {
                            Boolean isDefault = addressSnap.child("defaultAddress").getValue(Boolean.class);

                            if (isDefault != null && isDefault) {
                                // Build the Address object
                                address = new Address(
                                        addressSnap.child("addressID").getValue(String.class),
                                        addressSnap.child("userID").getValue(String.class),
                                        addressSnap.child("type").getValue(String.class),
                                        addressSnap.child("street").getValue(String.class),
                                        addressSnap.child("city").getValue(String.class),
                                        addressSnap.child("province").getValue(String.class),
                                        addressSnap.child("postalCode").getValue(String.class),
                                        addressSnap.child("country").getValue(String.class),
                                        addressSnap.child("contactNo").getValue(String.class),
                                        Boolean.TRUE.equals(addressSnap.child("defaultAddress").getValue(Boolean.class))
                                );

                                // Display in UI
                                String formattedAddress = address.getStreet() + ", "
                                        + address.getCity() + ", "
                                        + address.getProvince() + ", "
                                        + address.getPostalCode() + ", "
                                        + address.getCountry() + "\n"
                                        + "Contact: " + address.getContactNo();

                                binding.tvDeliveryAddress.setText(formattedAddress);
                                foundDefault = true;
                                break;
                            }
                        }

                        if (!foundDefault) {
                            binding.tvDeliveryAddress.setText("No default address found.");
                            address = null;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderPaymentActivity.this, "Failed to load address: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void confirmPayment() {
        if (product == null || address == null) {
            Toast.makeText(this, "Missing product or address info", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = getSelectedPaymentMethod();
        if (paymentMethod == null) {
            Toast.makeText(this, "Select payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderID = UUID.randomUUID().toString();
        String itemID = UUID.randomUUID().toString();
        String paymentID = UUID.randomUUID().toString();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        double price = Double.parseDouble(product.getPrice());
        double total = price * quantity;

        Order order = new Order(
                orderID,
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                product.getSellerID(),
                address.getAddressID(),
                paymentMethod,
                "Standard",
                "Pending",
                now,
                "Pending",
                String.valueOf(total) // use total price instead of product.getPrice()
        );

        OrderItems orderItem = new OrderItems(
                itemID,
                orderID,
                product.getProductID(),
                product.getPrice(),
                quantity
        );

        Payment payment = new Payment(
                paymentID,
                orderID,
                paymentMethod,
                "Paid via " + paymentMethod,
                "Paid",
                now,
                "REF-" + System.currentTimeMillis()
        );

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("orders").child(orderID).setValue(order);
        db.child("order_items").child(itemID).setValue(orderItem);
        db.child("payments").child(paymentID).setValue(payment);

        // Reduce product quantity
//        int newQty = Integer.parseInt(product.getQuantity()) - quantity;
//        db.child("products").child(product.getProductID()).child("quantity").setValue(String.valueOf(newQty));

        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
        finish();
    }

    private String getSelectedPaymentMethod() {
        int selectedId = binding.rgPaymentMethods.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selected = findViewById(selectedId);
            return selected.getText().toString();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}