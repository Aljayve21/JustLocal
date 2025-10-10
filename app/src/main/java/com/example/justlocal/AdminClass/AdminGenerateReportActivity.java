package com.example.justlocal.AdminClass;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminGenerateReportActivity extends AppCompatActivity {

    private ActivityAdminGenerateReportBinding binding;
    private long startDateMillis, endDateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminGenerateReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        // Default date range: This year (para hindi 0 kung hindi current month ang data)
        Calendar start = Calendar.getInstance();
        start.set(Calendar.MONTH, Calendar.JANUARY);
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        startDateMillis = start.getTimeInMillis();

        Calendar end = Calendar.getInstance();
        end.set(Calendar.MONTH, Calendar.DECEMBER);
        end.set(Calendar.DAY_OF_MONTH, 31);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        endDateMillis = end.getTimeInMillis();

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

        // Buttons initial state
        setExportButtonsEnabled(false);

        // Fetch metrics
        loadMetrics(startDateMillis, endDateMillis);

        // Export PDF
        binding.btnExportPDF.setOnClickListener(v -> checkPermissionAndExportPDF());

        // Preview dialog
        binding.btnPreview.setOnClickListener(v -> {
            String preview = "Report Summary\n"
                    + "Date Range: " + binding.tvDateRange.getText() + "\n"
                    + "Total Orders: " + binding.tvTotalOrders.getText() + "\n"
                    + "Revenue: " + binding.tvRevenue.getText() + "\n"
                    + "Complaints: " + binding.tvComplaints.getText() + "\n"
                    + "Resolved Rate: " + binding.tvResolvedRate.getText();

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Preview")
                    .setMessage(preview)
                    .setPositiveButton("Close", null)
                    .show();
        });

        // Export CSV
        binding.btnExportCSV.setOnClickListener(v -> exportCSV());
    }

    private void setExportButtonsEnabled(boolean enabled) {
        binding.btnExportPDF.setEnabled(enabled);
        binding.btnExportCSV.setEnabled(enabled);
        binding.btnPreview.setEnabled(enabled);
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

                    DatePickerDialog endPicker = new DatePickerDialog(this,
                            (view1, year1, month1, dayOfMonth1) -> {
                                endCal.set(year1, month1, dayOfMonth1, 23, 59, 59);
                                endDateMillis = endCal.getTimeInMillis();

                                updateDateRangeText();
                                loadMetrics(startDateMillis, endDateMillis);
                            }, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH));
                    endPicker.show();
                }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH));
        startPicker.show();
    }

    private void loadMetrics(long startDateMillis, long endDateMillis) {
        Log.d("REPORT", "Selected range: " + startDateMillis + " - " + endDateMillis);
        setExportButtonsEnabled(false);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // State holders to re-enable buttons after both loads finish
        final boolean[] done = {false, false};

        // 1) Orders
        rootRef.child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalRevenue = 0;
                int totalOrders = 0;

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order == null) continue;

                    long ts = order.getOrderTimestamp(); // from model parsing orderDate
                    if (ts < 100000000000L && ts > 0) ts *= 1000; // normalize if seconds

                    if (ts >= startDateMillis && ts <= endDateMillis) {
                        totalOrders++;
                        try {
                            totalRevenue += Double.parseDouble(order.getTotalAmount());
                        } catch (Exception e) {
                            Log.e("REPORT", "Invalid totalAmount for " + order.getOrderID() + ": " + order.getTotalAmount());
                        }
                    }

                    Log.d("REPORT", "Raw orderDate: " + order.getOrderDate()
                            + ", OrderID: " + order.getOrderID()
                            + ", Total: " + order.getTotalAmount()
                            + ", Timestamp: " + ts);
                }

                // Update UI
                binding.tvTotalOrders.setText(String.valueOf(totalOrders));
                binding.tvRevenue.setText(String.format(Locale.getDefault(), "%,.2f", totalRevenue));
                Log.d("REPORT", "Total Orders: " + totalOrders + ", Total Revenue: " + totalRevenue);

                done[0] = true;
                if (done[0] && done[1]) setExportButtonsEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("REPORT", "Orders load failed: " + error.getMessage());
                done[0] = true;
                if (done[0] && done[1]) setExportButtonsEnabled(true);
            }
        });

        // 2) Complaints
        rootRef.child("complaints").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalComplaints = 0;
                int resolvedComplaints = 0;

                for (DataSnapshot complaintSnap : snapshot.getChildren()) {
                    Complaint complaint = complaintSnap.getValue(Complaint.class);
                    if (complaint == null) continue;

                    long ts = 0L;
                    if (complaint.getDateCreated() > 0) {
                        ts = complaint.getDateCreated();
                    } else {
                        Long createdAt = complaintSnap.child("createdAt").getValue(Long.class);
                        if (createdAt != null) ts = createdAt;
                    }
                    if (ts < 100000000000L && ts > 0) ts *= 1000;

                    if (ts >= startDateMillis && ts <= endDateMillis) {
                        totalComplaints++;
                        if ("resolved".equalsIgnoreCase(complaint.getStatus())) {
                            resolvedComplaints++;
                        }
                    }

                    Log.d("REPORT", "ComplaintID: " + (complaint.getComplaintID())
                            + ", Status: " + complaint.getStatus()
                            + ", Timestamp: " + ts);
                }

                String resolvedRate = totalComplaints > 0
                        ? String.format(Locale.getDefault(), "%d%%", (resolvedComplaints * 100 / totalComplaints))
                        : "0%";

                binding.tvComplaints.setText(String.valueOf(totalComplaints));
                binding.tvResolvedRate.setText(resolvedRate);

                Log.d("REPORT", "Total Complaints: " + totalComplaints + ", Resolved: " + resolvedComplaints);

                done[1] = true;
                if (done[0] && done[1]) setExportButtonsEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("REPORT", "Complaints load failed: " + error.getMessage());
                done[1] = true;
                if (done[0] && done[1]) setExportButtonsEnabled(true);
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
            document.add(new Paragraph("Date Range: " + binding.tvDateRange.getText()));
            document.add(new Paragraph("Total Orders: " + binding.tvTotalOrders.getText()));
            document.add(new Paragraph("Revenue: " + binding.tvRevenue.getText()));
            document.add(new Paragraph("Complaints: " + binding.tvComplaints.getText()));
            document.add(new Paragraph("Resolved Rate: " + binding.tvResolvedRate.getText()));
            document.close();
            Toast.makeText(this, "PDF exported: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("REPORT", "PDF exported at: " + pdfFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("REPORT", "PDF export failed", e);
            Toast.makeText(this, "PDF export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCSV() {
        try {
            String csvHeader = "Date Range,Total Orders,Revenue,Complaints,Resolved Rate\n";
            String csvRow = String.format(
                    Locale.getDefault(),
                    "%s,%s,%s,%s,%s\n",
                    binding.tvDateRange.getText(),
                    binding.tvTotalOrders.getText(),
                    binding.tvRevenue.getText(),
                    binding.tvComplaints.getText(),
                    binding.tvResolvedRate.getText()
            );
            String csv = csvHeader + csvRow;

            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) {
                Toast.makeText(this, "Storage unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
            File csvFile = new File(dir, "report.csv");
            try (FileOutputStream fos = new FileOutputStream(csvFile)) {
                fos.write(csv.getBytes());
            }
            Toast.makeText(this, "CSV exported: " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("REPORT", "CSV exported at: " + csvFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("REPORT", "CSV export failed", e);
            Toast.makeText(this, "CSV export failed", Toast.LENGTH_SHORT).show();
        }
    }
}
