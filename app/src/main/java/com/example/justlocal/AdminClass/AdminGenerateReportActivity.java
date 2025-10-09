package com.example.justlocal.AdminClass;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.justlocal.Models.Complaint;
import com.example.justlocal.Models.Order;
import com.example.justlocal.R;
import com.example.justlocal.databinding.ActivityAdminGenerateReportBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminGenerateReportActivity extends AppCompatActivity {

    private ActivityAdminGenerateReportBinding binding;
    private long startDateMillis, endDateMillis;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminGenerateReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Default date range: this month
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        startDateMillis = cal.getTimeInMillis();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDateMillis = cal.getTimeInMillis();

        Log.d("REPORT", "Default Start: " + startDateMillis + ", End: " + endDateMillis);
        updateDateRangeText();

        // Spinner setup
        Spinner spinner = binding.spinnerReportType;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.admin_report_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Date picker
        binding.layoutDateRange.setOnClickListener(v -> showDateRangePicker());

        // Fetch metrics
        loadMetrics();

        // Export button
        binding.btnExportPDF.setOnClickListener(v -> checkPermissionAndExportPDF());
    }

    private void updateDateRangeText() {
        String start = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(startDateMillis));
        String end = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(endDateMillis));
        binding.tvDateRange.setText(start + " - " + end);
        Log.d("REPORT", "Selected range: " + startDateMillis + " - " + endDateMillis);
    }

    private void showDateRangePicker() {
        final Calendar startCal = Calendar.getInstance();
        final Calendar endCal = Calendar.getInstance();

        DatePickerDialog startPicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    startCal.set(year, month, dayOfMonth, 0, 0, 0);
                    startDateMillis = startCal.getTimeInMillis();

                    // Show end date picker after start date
                    DatePickerDialog endPicker = new DatePickerDialog(this,
                            (view1, year1, month1, dayOfMonth1) -> {
                                endCal.set(year1, month1, dayOfMonth1, 23, 59, 59);
                                endDateMillis = endCal.getTimeInMillis();

                                updateDateRangeText();
                                loadMetrics(); // refresh after date selection
                            }, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH));
                    endPicker.show();
                }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH));
        startPicker.show();
    }

    private void loadMetrics() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // --- ORDERS ---
        db.getReference("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalOrders = 0;
                double totalRevenue = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Order order = ds.getValue(Order.class);
                    if (order == null) continue;

                    long ts = 0;

                    // Convert orderDate String to timestamp
                    try {
                        Date date = sdf.parse(order.getOrderDate()); // orderDate is a String
                        if (date != null) ts = date.getTime();
                    } catch (ParseException e) {
                        Log.e("REPORT", "Invalid orderDate: " + order.getOrderDate());
                        continue;
                    }

                    Log.d("REPORT", "Raw orderDate: " + order.getOrderDate() +
                            ", OrderID: " + order.getOrderID() +
                            ", Total: " + order.getTotalAmount() +
                            ", Timestamp: " + ts);

                    if (ts >= startDateMillis && ts <= endDateMillis) {
                        totalOrders++;
                        try {
                            totalRevenue += Double.parseDouble(order.getTotalAmount());
                        } catch (Exception e) {
                            Log.e("REPORT", "Invalid totalAmount for order " + order.getOrderID());
                        }
                    }
                }

                binding.tvTotalOrders.setText(String.valueOf(totalOrders));
                binding.tvRevenue.setText(String.format("₱%.2f", totalRevenue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("REPORT", "Orders fetch error: " + error.getMessage());
            }
        });

        // --- COMPLAINTS ---
        db.getReference("complaints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int complaints = 0;
                int resolved = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Complaint complaint = ds.getValue(Complaint.class);
                    if (complaint == null) continue;

                    long ts = complaint.getDateCreated(); // already in millis

                    // Convert seconds -> millis if needed
                    if (ts < 100000000000L) ts = ts * 1000;

                    Log.d("REPORT", "ComplaintID: " + complaint.getComplaintID() +
                            ", Status: " + complaint.getStatus() +
                            ", Timestamp: " + ts);

                    if (ts >= startDateMillis && ts <= endDateMillis) {
                        complaints++;
                        if ("Resolved".equalsIgnoreCase(complaint.getStatus())) resolved++;
                    }
                }

                String rate = complaints > 0 ? String.format("%d%%", (resolved * 100 / complaints)) : "0%";
                binding.tvComplaints.setText(String.valueOf(complaints));
                binding.tvResolvedRate.setText(rate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("REPORT", "Complaints fetch error: " + error.getMessage());
            }
        });
    }


    // --- PERMISSION & PDF ---
    private void checkPermissionAndExportPDF() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            exportPDF();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportPDF();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportPDF() {
        try {
            File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "report.pdf");
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
            document.add(new Paragraph("Report Summary"));
            document.add(new Paragraph("Total Orders: " + binding.tvTotalOrders.getText()));
            document.add(new Paragraph("Revenue: " + binding.tvRevenue.getText()));
            document.add(new Paragraph("Complaints: " + binding.tvComplaints.getText()));
            document.add(new Paragraph("Resolved Rate: " + binding.tvResolvedRate.getText()));
            document.close();
            Toast.makeText(this, "PDF exported: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("REPORT", "PDF exported at: " + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "PDF export failed", Toast.LENGTH_SHORT).show();
        }
    }
}
