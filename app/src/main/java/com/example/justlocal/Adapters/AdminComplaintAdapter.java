package com.example.justlocal.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.AdminClass.AdminComplaintConversationActivity;
import com.example.justlocal.Models.Complaint;
import com.example.justlocal.R;

import java.util.List;

public class AdminComplaintAdapter extends RecyclerView.Adapter<AdminComplaintAdapter.ViewHolder> {

    private List<Complaint> complaintList;
    private Context context;

    public AdminComplaintAdapter(List<Complaint> complaintList, Context context) {
        this.complaintList = complaintList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);

        holder.tvComplaintID.setText(complaint.getComplaintID());
        holder.tvStatus.setText(complaint.getStatus());
        holder.tvMessage.setText(complaint.getMessage());
        holder.tvCustomerID.setText("CUST: " + complaint.getCustomerID());
        holder.tvOrderID.setText(" • ORD: " + complaint.getOrderID());
        holder.tvProductID.setText(" • PRD: " + complaint.getProductID());
        holder.tvRepliedBy.setText(" • CSR: " + (complaint.getRepliedBy() != null ? complaint.getRepliedBy() : "none"));

        // Click listener for item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminComplaintConversationActivity.class);
            intent.putExtra("complaintID", complaint.getComplaintID());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvComplaintID, tvStatus, tvMessage, tvCustomerID, tvOrderID, tvProductID, tvRepliedBy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvComplaintID = itemView.findViewById(R.id.tvComplaintID);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvCustomerID = itemView.findViewById(R.id.tvCustomerID);
            tvOrderID = itemView.findViewById(R.id.tvOrderID);
            tvProductID = itemView.findViewById(R.id.tvProductID);
            tvRepliedBy = itemView.findViewById(R.id.tvRepliedBy);
        }
    }
}

