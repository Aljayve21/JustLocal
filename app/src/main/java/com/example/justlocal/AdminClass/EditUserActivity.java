package com.example.justlocal.AdminClass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.justlocal.Models.User;
import com.example.justlocal.databinding.ActivityEditUserBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditUserActivity extends AppCompatActivity {

    private ActivityEditUserBinding binding;
    private DatabaseReference usersRef;
    private String userIdKey;
    private User currentUser;
    private static final int PICK_IMAGE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userIdKey = getIntent().getStringExtra("userId");

        if (userIdKey == null || userIdKey.isEmpty()) {
            Toast.makeText(this, "User ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users").child(userIdKey);

        setupDropdowns();
        setupListeners();
        loadUserDetails();
    }

    private void setupDropdowns() {
        String[] roles = {"Admin", "Seller", "Customer", "CSR"};
        String[] statuses = {"Active", "Inactive", "Banned"};

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        binding.spinnerRole.setAdapter(roleAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        binding.spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        binding.btnSave.setOnClickListener(v -> {
            if (currentUser == null) return;

            String updatedName = binding.etFullName.getText().toString().trim();
            if (updatedName.isEmpty()) {
                binding.etFullName.setError("Name cannot be empty");
                return;
            }

            currentUser.setFullName(updatedName);
            currentUser.setPhone(binding.etPhone.getText().toString().trim());
            currentUser.setRole(binding.spinnerRole.getText().toString().trim());
            currentUser.setStatus(binding.spinnerStatus.getText().toString().trim());

            usersRef.setValue(currentUser)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loadUserDetails() {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                currentUser = snapshot.getValue(User.class);

                if (currentUser != null) {
                    binding.etFullName.setText(currentUser.getFullName());
                    binding.etEmail.setText(currentUser.getEmail());
                    binding.etPhone.setText(currentUser.getPhone());

                    binding.spinnerRole.setText(currentUser.getRole(), false);
                    binding.spinnerStatus.setText(currentUser.getStatus(), false);

                    if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                        byte[] decodedBytes = Base64.decode(currentUser.getAvatarUrl(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        binding.ivUserAvatar.setImageBitmap(bitmap);
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                String base64Image = convertImageToBase64(imageUri);
                currentUser.setAvatarUrl(base64Image);
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                binding.ivUserAvatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertImageToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
