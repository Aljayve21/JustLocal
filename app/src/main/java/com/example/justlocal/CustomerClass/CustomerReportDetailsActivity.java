package com.example.justlocal.CustomerClass;

import android.os.Build;
import android.os.Bundle;
import com.example.justlocal.Models.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Adapters.MessagePreviewAdapter;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerReportDetailsActivity extends AppCompatActivity {

    private String complaintID;
    private TextView tvComplaintIDHeader, tvHeaderStatus, tvComplaintMessage, tvOrderID, tvProductID, tvStatusUpdateInfo, tvCreatedDate;
    private View ivStatusStep1, ivStatusStep2, ivStatusStep3, lineStep1to2, lineStep2to3;
    private View layoutSupportResponseCard;
    private TextView tvRepliedBy, tvSupportResponse, tvResponseTime, tvMessageCount;
    private RecyclerView rvRecentMessages;

    private MessagePreviewAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_report_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        complaintID = getIntent().getStringExtra("complaintID");
        if (complaintID == null) {
            Toast.makeText(this, "Missing complaint ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initUI();
        loadComplaintDetails();
        loadRecentMessages();
    }

    private void initUI() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvComplaintIDHeader = findViewById(R.id.tvComplaintIDHeader);
        tvHeaderStatus = findViewById(R.id.tvHeaderStatus);
        tvComplaintMessage = findViewById(R.id.tvComplaintMessage);
        tvOrderID = findViewById(R.id.tvOrderID);
        tvProductID = findViewById(R.id.tvProductID);
        tvStatusUpdateInfo = findViewById(R.id.tvStatusUpdateInfo);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);

        ivStatusStep1 = findViewById(R.id.ivStatusStep1);
        ivStatusStep2 = findViewById(R.id.ivStatusStep2);
        ivStatusStep3 = findViewById(R.id.ivStatusStep3);
        lineStep1to2 = findViewById(R.id.lineStep1to2);
        lineStep2to3 = findViewById(R.id.lineStep2to3);

        layoutSupportResponseCard = findViewById(R.id.layoutSupportResponseCard);
        tvRepliedBy = findViewById(R.id.tvRepliedBy);
        tvSupportResponse = findViewById(R.id.tvSupportResponse);
        tvResponseTime = findViewById(R.id.tvResponseTime);
        tvMessageCount = findViewById(R.id.tvMessageCount);

        rvRecentMessages = findViewById(R.id.rvRecentMessages);
        rvRecentMessages.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessagePreviewAdapter(messageList);
        rvRecentMessages.setAdapter(messageAdapter);

        findViewById(R.id.btnViewOrder).setOnClickListener(v -> {
            // Optional: open order details activity
            // startActivity(new Intent(this, CustomerOrderDetailsActivity.class)
            //         .putExtra("orderID", tvOrderID.getText().toString()));
        });

        findViewById(R.id.btnAddMessage).setOnClickListener(v -> {
            // Optional: open add message activity
            // startActivity(new Intent(this, AddComplaintMessageActivity.class)
            //         .putExtra("complaintID", complaintID));
        });
    }

    private void loadComplaintDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("complaints")
                .child(complaintID);

        ref.get().addOnSuccessListener(snapshot -> {
            Complaint complaint = snapshot.getValue(Complaint.class);
            if (complaint == null) {
                Toast.makeText(this, "Complaint not found", Toast.LENGTH_SHORT).show();
                return;
            }

            tvComplaintIDHeader.setText("Complaint #" + complaint.getComplaintID());
            tvComplaintMessage.setText(complaint.getMessage());
            tvOrderID.setText("Order #" + complaint.getOrderID());
            tvProductID.setText("Product ID: " + complaint.getProductID());

            tvHeaderStatus.setText(complaint.getStatus());
            updateStatusTimeline(complaint.getStatus());

            tvStatusUpdateInfo.setText("Last updated: " + formatDate(complaint.getLastUpdated()));
            tvCreatedDate.setText("Created: " + formatDate(complaint.getDateCreated()));

            if (complaint.getSupportReply() != null) {
                layoutSupportResponseCard.setVisibility(View.VISIBLE);
                tvRepliedBy.setText("Replied by: " + complaint.getRepliedBy());
                tvSupportResponse.setText(complaint.getSupportReply());
                tvResponseTime.setText("Replied: " + formatDate(complaint.getReplyDate()));
            } else {
                layoutSupportResponseCard.setVisibility(View.GONE);
            }
        });
    }

    private void loadRecentMessages() {
        DatabaseReference msgRef = FirebaseDatabase.getInstance()
                .getReference("complaint_messages")
                .child(complaintID);

        msgRef.limitToLast(5).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    Message msg = msgSnap.getValue(Message.class);
                    if (msg != null) messageList.add(msg);
                }

                tvMessageCount.setText(messageList.size() + " messages");
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error if needed
            }
        });
    }

    private void updateStatusTimeline(String status) {
        // ✅ Submitted always green
        ((ImageView) ivStatusStep1).setImageResource(R.drawable.ic_check_circle);
        ((ImageView) ivStatusStep1).setColorFilter(getColor(R.color.green_success));

        if ("In Review".equalsIgnoreCase(status)) {
            lineStep1to2.setBackgroundColor(getColor(R.color.green_success));
            ((ImageView) ivStatusStep2).setImageResource(R.drawable.ic_pending);
            ((ImageView) ivStatusStep2).setColorFilter(getColor(R.color.amber_500));

        } else if ("Resolved".equalsIgnoreCase(status)) {
            lineStep1to2.setBackgroundColor(getColor(R.color.green_success));
            lineStep2to3.setBackgroundColor(getColor(R.color.green_success));

            ((ImageView) ivStatusStep2).setImageResource(R.drawable.ic_check_circle);
            ((ImageView) ivStatusStep2).setColorFilter(getColor(R.color.green_success));

            ((ImageView) ivStatusStep3).setImageResource(R.drawable.ic_check_circle);
            ((ImageView) ivStatusStep3).setColorFilter(getColor(R.color.green_success));
        }
    }

    private String formatDate(long timestamp) {
        if (timestamp == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}