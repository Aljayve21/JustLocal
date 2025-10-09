package com.example.justlocal.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.CustomerClass.CustomerAddComplaintMessage;
import com.example.justlocal.CustomerClass.CustomerReportDetailsActivity;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Complaint> complaintList;

    public ReportAdapter(List<Complaint> complaintList) {
        this.complaintList = complaintList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);

        holder.tvReportID.setText("Complaint #" + complaint.getComplaintID());
        holder.tvReportStatus.setText(complaint.getStatus());
        holder.tvReportDescription.setText(complaint.getMessage());

        // Format date
        String createdDate = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                .format(new Date(complaint.getDateCreated()));
        holder.tvReportDate.setText(createdDate);

        // View Details
        holder.btnViewReport.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, CustomerReportDetailsActivity.class);
            intent.putExtra("complaintID", complaint.getComplaintID());
            intent.putExtra("orderID", complaint.getOrderID());
            intent.putExtra("status", complaint.getStatus());
            intent.putExtra("message", complaint.getMessage());
            ctx.startActivity(intent);
        });

        // Add Message
        holder.btnAddMessage.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, CustomerAddComplaintMessage.class);
            intent.putExtra("complaintID", complaint.getComplaintID());
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportID, tvReportDate, tvReportStatus, tvReportDescription;
        Button btnViewReport, btnAddMessage;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportID = itemView.findViewById(R.id.tvReportID);
            tvReportDate = itemView.findViewById(R.id.tvReportDate);
            tvReportStatus = itemView.findViewById(R.id.tvReportStatus);
            tvReportDescription = itemView.findViewById(R.id.tvReportDescription);
            btnViewReport = itemView.findViewById(R.id.btnViewReport);
            btnAddMessage = itemView.findViewById(R.id.btnAddMessage);
        }
    }
}
