package com.example.justlocal.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Complaint;
import com.example.justlocal.databinding.SupportTicketItemCardLayoutBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private Context context;
    private List<Complaint> ticketList;
    private Map<String, String> userIdToFullName;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onViewDetails(Complaint complaint);
        void onTakeTicket(Complaint complaint);
    }

    public TicketAdapter(Context context, List<Complaint> ticketList, Map<String, String> userIdToFullName, OnTicketClickListener listener) {
        this.context = context;
        this.ticketList = ticketList;
        this.userIdToFullName = userIdToFullName;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SupportTicketItemCardLayoutBinding binding = SupportTicketItemCardLayoutBinding.inflate(
                LayoutInflater.from(context), parent, false
        );
        return new TicketViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Complaint complaint = ticketList.get(position);
        holder.bind(complaint);
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public class TicketViewHolder extends RecyclerView.ViewHolder {
        SupportTicketItemCardLayoutBinding b;

        public TicketViewHolder(SupportTicketItemCardLayoutBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        public void bind(Complaint complaint) {
            b.tvTicketID.setText("Ticket #" + complaint.getComplaintID());
            b.tvDescription.setText(complaint.getMessage());
            b.tvTicketStatus.setText(complaint.getStatus());

            // Status colors
            switch (complaint.getStatus()) {
                case "Open":
                    b.tvTicketStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    break;
                case "In Progress":
                    b.tvTicketStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "Resolved":
                    b.tvTicketStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    break;
            }

            // Date
            String currentDate = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(new Date());
            b.tvTicketDate.setText(currentDate);

            // Customer Name from map
            String fullName = userIdToFullName.getOrDefault(complaint.getCustomerID(), "Unknown");
            b.tvCustomerName.setText(fullName);

            // Order reference
            b.tvOrderReference.setText("#" + complaint.getOrderID());

            // Buttons
            b.btnViewTicket.setOnClickListener(v -> listener.onViewDetails(complaint));
//            b.btnTakeTicket.setOnClickListener(v -> listener.onTakeTicket(complaint));
        }
    }
}