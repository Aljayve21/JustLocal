package com.example.justlocal.SellerClass;

import android.app.ComponentCaller;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityAddProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ActivityAddProductBinding binding;

    private String base64Image = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Button listeners
        binding.btnSelectImage.setOnClickListener(v -> openGallery());
        binding.btnTakePhoto.setOnClickListener(v -> openCamera());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSaveProduct.setOnClickListener(v -> uploadProduct());

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void openGallery() {
           // Implement gallery selection logic
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = null;

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }

            if (bitmap != null) {
                binding.ivProductImage.setImageBitmap(bitmap);
                base64Image = bitmapToBase64(bitmap);
            }
        }
    }


    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private void uploadProduct() {
        String name = binding.etProductName.getText().toString().trim();
        String description = binding.etProductDescription.getText().toString().trim();
        String price = binding.etPrice.getText().toString().trim();
        String quantity = binding.etQuantity.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || quantity.isEmpty() || base64Image == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assume you have sellerID from logged-in user session or Firebase Auth UID
        String sellerID = getCurrentSellerID();  // <-- Implement this method based on your auth/session

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
        String productId = ref.push().getKey();

        Map<String, Object> product = new HashMap<>();
        product.put("productID", productId);
        product.put("sellerID", sellerID);     // Add sellerID here
        product.put("productName", name);
        product.put("productDescription", description);
        product.put("price", price);
        product.put("quantity", quantity);
        product.put("image", base64Image);
        product.put("status", "pending");
        product.put("approvedBy", "");  // initially empty

        ref.child(productId).setValue(product).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddProductActivity.this, "Product submitted for approval", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddProductActivity.this, "Upload failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //get the current UserID
    private String getCurrentSellerID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


}