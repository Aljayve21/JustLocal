package com.example.justlocal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justlocal.AdminClass.AdminDashboardActivity;
import com.example.justlocal.SellerClass.SellerDashboardActivity;
import com.example.justlocal.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.btnQuickAccess.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String role = snapshot.child("role").getValue(String.class);

                                    runOnUiThread(() -> {
                                        if (role != null) {
                                            switch (role.toLowerCase()) {
                                                case "admin":
                                                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                                    break;
                                                case "seller":
                                                    startActivity(new Intent(LoginActivity.this, SellerDashboardActivity.class));
                                                    break;
//                                                case "Customer":
//                                                    startActivity(new Intent(LoginActivity.this, CustomerDashboardActivity.class));
//                                                    break;
//                                                case "CSR":
//                                                    startActivity(new Intent(LoginActivity.this, CSRDashboardActivity.class));
//                                                    break;
                                                default:
                                                    Toast.makeText(LoginActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                                    break;
                                            }

                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Role not found for user", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {
                                    Toast.makeText(LoginActivity.this, "User record not found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(LoginActivity.this, "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
