package com.example.justlocal.CustomerClass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.Models.Address;
import com.example.justlocal.Models.User;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityUserAddressBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserAddressActivity extends AppCompatActivity {

    private ActivityUserAddressBinding binding;
    private DatabaseReference addressRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUserAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        addressRef = FirebaseDatabase.getInstance().getReference("addresses");

        Log.d("FirebaseCheck", "CurrentUserID: " + currentUserId);



        setupListeners();

        // Check if this is an edit request
        if (getIntent().getBooleanExtra("isEdit", false)) {
            String addressID = getIntent().getStringExtra("addressID");
            String type = getIntent().getStringExtra("type");
            String street = getIntent().getStringExtra("street");
            String city = getIntent().getStringExtra("city");
            String province = getIntent().getStringExtra("province");
            String postalCode = getIntent().getStringExtra("postalCode");
            String country = getIntent().getStringExtra("country");
            String contactNo = getIntent().getStringExtra("contactNo");
            boolean isDefault = getIntent().getBooleanExtra("isDefault", false);

            // Populate UI
            binding.etStreet.setText(street);
            binding.etCity.setText(city);
            binding.etProvince.setText(province);
            binding.etPostalCode.setText(postalCode);
            binding.etCountry.setText(country);
            binding.etContactNo.setText(contactNo);
            binding.cbDefaultAddress.setChecked(isDefault);

            if (type != null) {
                if (type.equalsIgnoreCase("Home")) {
                    binding.rbHome.setChecked(true);
                } else if (type.equalsIgnoreCase("Work")) {
                    binding.rbOffice.setChecked(true);
                }
            }

            Log.d("EditMode", "Loaded address data for edit: " + addressID);
        }

    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveAddress.setOnClickListener(v -> {

            Log.d("UserAddressActivity", "Save button clicked");
            if (validateInputs()) {

                Log.d("UserAddressActivity", "Inputs are valid");
                saveAddressToFirebase();




            }
        });
    }


    private boolean validateInputs() {
        if (binding.etStreet.getText().toString().trim().isEmpty()) {
            binding.tilStreet.setError("Street is required");
            return false;
        }

        if (binding.etCity.getText().toString().trim().isEmpty()) {
            binding.tilCity.setError("City is required");
            return false;
        }

        if (binding.etProvince.getText().toString().trim().isEmpty()) {
            binding.tilProvince.setError("Province is required");
            return false;
        }

        if (binding.etPostalCode.getText().toString().trim().isEmpty()) {
            binding.tilPostalCode.setError("Postal code is required");
            return false;
        }

        if (binding.etCountry.getText().toString().trim().isEmpty()) {
            binding.tilCountry.setError("Country is required");
            return false;
        }

        if (binding.etContactNo.getText().toString().trim().isEmpty()) {
            binding.tilContactNo.setError("Contact number is required");
            return false;
        }

        return true;
    }

    private void saveAddressToFirebase() {
        String addressId = addressRef.push().getKey();
        Log.d("UserAddressActivity", "Generated address ID: " + addressId);

        String type = ((RadioButton) findViewById(binding.rgAddressType.getCheckedRadioButtonId()))
                .getText().toString();

        // Create map for the address
        Map<String, Object> map = new HashMap<>();
        map.put("addressID", addressId);
        map.put("userID", currentUserId);
        map.put("type", type);
        map.put("street", binding.etStreet.getText().toString().trim());
        map.put("city", binding.etCity.getText().toString().trim());
        map.put("province", binding.etProvince.getText().toString().trim());
        map.put("postalCode", binding.etPostalCode.getText().toString().trim());
        map.put("country", binding.etCountry.getText().toString().trim());
        map.put("contactNo", binding.etContactNo.getText().toString().trim());
        map.put("defaultAddress", binding.cbDefaultAddress.isChecked()); // ✅ Add this

        // Save to Firebase
        addressRef.child(addressId).setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Address saved successfully", Toast.LENGTH_SHORT).show();
                    Log.d("UserAddressActivity", "Address written to Firebase");

                    // Redirect to OrderPaymentActivity
                    Intent intent = new Intent(UserAddressActivity.this, OrderPaymentActivity.class);
                    intent.putExtra("addressID", addressId);
                    intent.putExtra("addressUpdated", true);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseWrite", "Failed to save: " + e.getMessage());
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }






}