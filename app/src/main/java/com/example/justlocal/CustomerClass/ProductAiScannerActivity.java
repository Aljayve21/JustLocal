package com.example.justlocal.CustomerClass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.example.justlocal.Models.Product;
import com.example.justlocal.Utility.TfliteEmbeddingHelper;
import com.example.justlocal.databinding.ActivityProductAiScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductAiScannerActivity extends AppCompatActivity {

    private ActivityProductAiScannerBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private TfliteEmbeddingHelper embeddingHelper;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to scan products", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductAiScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        try {
            embeddingHelper = new TfliteEmbeddingHelper(this, "mobilenet_v2_feature_vector.tflite", 224);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load AI model", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        binding.btnCapture.setOnClickListener(v -> takePhoto());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        binding.layoutLoading.setVisibility(View.VISIBLE);

        File photoFile = new File(getExternalCacheDir(), "scan_capture.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap != null) {
                    performImageSearch(bitmap);
                } else {
                    binding.layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(ProductAiScannerActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                binding.layoutLoading.setVisibility(View.GONE);
                Toast.makeText(ProductAiScannerActivity.this, "Capture failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performImageSearch(Bitmap bitmap) {
        float[] queryEmbedding = embeddingHelper.getEmbedding(bitmap);

        if (queryEmbedding == null) {
            binding.layoutLoading.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to extract features", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("products")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double bestScore = -1;
                        Product bestMatch = null;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Product product = snap.getValue(Product.class);
                            if (product == null || !"approved".equalsIgnoreCase(product.getStatus())) continue;

                            List<Double> embList = product.getEmbedding();
                            if (embList == null || embList.isEmpty()) continue;

                            float[] dbEmbedding = new float[embList.size()];
                            for (int i = 0; i < embList.size(); i++) {
                                dbEmbedding[i] = embList.get(i).floatValue();
                            }

                            double score = calculateCosineSimilarity(queryEmbedding, dbEmbedding);
                            if (score > bestScore) {
                                bestScore = score;
                                bestMatch = product;
                            }
                        }

                        binding.layoutLoading.setVisibility(View.GONE);

                        if (bestMatch != null && bestScore > 0.4) { // Threshold adjusted for MobileNetV2 features
                            Intent intent = new Intent(ProductAiScannerActivity.this, BrowseProductsActivity.class);
                            intent.putExtra("searchQuery", bestMatch.getProductName());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ProductAiScannerActivity.this, "No matching product found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.layoutLoading.setVisibility(View.GONE);
                    }
                });
    }

    private double calculateCosineSimilarity(float[] v1, float[] v2) {
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (embeddingHelper != null) embeddingHelper.close();
    }
}
