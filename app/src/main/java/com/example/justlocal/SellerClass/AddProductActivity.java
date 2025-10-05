package com.example.justlocal.SellerClass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justlocal.Utility.TfliteEmbeddingHelper;
import com.example.justlocal.databinding.ActivityAddProductBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ActivityAddProductBinding binding;
    private String base64Image = null;
    private Bitmap selectedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Buttons
        binding.btnSelectImage.setOnClickListener(v -> openGallery());
        binding.btnTakePhoto.setOnClickListener(v -> openCamera());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSaveProduct.setOnClickListener(v -> uploadProduct());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                    Uri uri = data.getData();
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                    selectedBitmap = (Bitmap) data.getExtras().get("data");
                }

                if (selectedBitmap != null) {
                    binding.ivProductImage.setImageBitmap(selectedBitmap);
                    base64Image = bitmapToBase64(selectedBitmap);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private void uploadProduct() {
        String name = binding.etProductName.getText().toString().trim();
        String description = binding.etProductDescription.getText().toString().trim();
        String price = binding.etPrice.getText().toString().trim();
        String quantity = binding.etQuantity.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || price.isEmpty() || quantity.isEmpty() || selectedBitmap == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        generateAndUploadProduct(name, description, price, quantity, selectedBitmap);
    }

    private void generateAndUploadProduct(String name, String desc, String price, String qty, Bitmap bitmap) {
        try {
            // ✅ use the fixed helper (auto-detect embedding size)
            TfliteEmbeddingHelper helper = new TfliteEmbeddingHelper(
                    this,
                    "mobilenet_v2_feature_vector.tflite",
                    224   // input size
            );

            float[] embeddingArray = helper.getEmbedding(bitmap);

            // Convert float[] to List<Double> for Firebase
            List<Double> embList = new ArrayList<>();
            for (float f : embeddingArray) embList.add((double) f);

            String sellerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
            String productId = ref.push().getKey();

            if (productId == null) {
                Toast.makeText(this, "Failed to generate product ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> product = new HashMap<>();
            product.put("productID", productId);
            product.put("sellerID", sellerID);
            product.put("productName", name);
            product.put("productDescription", desc);
            product.put("price", price);
            product.put("quantity", qty);
            product.put("image", base64Image);
            product.put("status", "pending");
            product.put("embedding", embList); // ✅ now guaranteed in Firebase

            ref.child(productId).setValue(product).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Product uploaded with embedding", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            });

            helper.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Embedding generation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
