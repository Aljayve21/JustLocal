package com.example.justlocal.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Message;
import com.example.justlocal.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CUSTOMER = 0;  // left
    private static final int TYPE_SUPPORT = 1;   // right

    private Context context;
    private List<Message> messages;
    private String currentUserId;   // logged-in user (CSR/Admin)
    private boolean isEditable;     // true if user can reply (CSR)

    public MessageAdapter(Context context, List<Message> messages, String currentUserId, boolean isEditable) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.isEditable = isEditable;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        // If the message sender is the logged-in user → support (right)
        if (msg.getSenderID().equals(currentUserId)) {
            return TYPE_SUPPORT;
        } else {
            // Otherwise, customer (left)
            return TYPE_CUSTOMER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_SUPPORT) {
            View view = inflater.inflate(R.layout.item_message_support, parent, false);
            return new SupportViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_customer, parent, false);
            return new CustomerViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);

        if (holder instanceof SupportViewHolder) {
            ((SupportViewHolder) holder).bind(msg, isEditable);
        } else if (holder instanceof CustomerViewHolder) {
            ((CustomerViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // --- View Holders ---

    static class SupportViewHolder extends RecyclerView.ViewHolder {
        TextView tvSupportMessage, tvSupportMessageTime, tvSenderName;

        SupportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSupportMessage = itemView.findViewById(R.id.tvSupportMessage);
            tvSupportMessageTime = itemView.findViewById(R.id.tvSupportMessageTime);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
        }

        void bind(Message msg, boolean isEditable) {
            tvSupportMessage.setText(msg.getContent());
            tvSupportMessageTime.setText(formatTime(msg.getTimestamp()));

            if (isEditable) {
                tvSenderName.setText("Support Team"); // show CSR/Admin name if needed
                tvSenderName.setVisibility(View.VISIBLE);
            } else {
                tvSenderName.setText(""); // hide name for view-only users
                tvSenderName.setVisibility(View.GONE);
            }
        }
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerMessage, tvCustomerMessageTime;

        CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerMessage = itemView.findViewById(R.id.tvCustomerMessage);
            tvCustomerMessageTime = itemView.findViewById(R.id.tvCustomerMessageTime);
        }

        void bind(Message msg) {
            tvCustomerMessage.setText(msg.getContent());
            tvCustomerMessageTime.setText(formatTime(msg.getTimestamp()));
        }
    }

    private static String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

