package com.example.justlocal.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Message;
import com.example.justlocal.databinding.ItemMessageCustomerBinding;
import com.example.justlocal.databinding.ItemMessageSupportBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CUSTOMER = 1;
    private static final int TYPE_SUPPORT = 2;

    private List<Message> messageList;
    private String currentUserID;

    public ComplaintMessageAdapter(List<Message> messageList, String currentUserID) {
        this.messageList = messageList;
        this.currentUserID = currentUserID;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        return msg.getSenderID().equals(currentUserID) ? TYPE_CUSTOMER : TYPE_SUPPORT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CUSTOMER) {
            ItemMessageCustomerBinding binding = ItemMessageCustomerBinding .inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new CustomerViewHolder(binding);
        } else {
            ItemMessageSupportBinding  binding = ItemMessageSupportBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SupportViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof CustomerViewHolder) {
            ((CustomerViewHolder) holder).bind(message);
        } else if (holder instanceof SupportViewHolder) {
            ((SupportViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        ItemMessageCustomerBinding  binding;
        CustomerViewHolder(ItemMessageCustomerBinding  binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvCustomerMessage.setText(message.getContent());
            binding.tvCustomerMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    static class SupportViewHolder extends RecyclerView.ViewHolder {
        ItemMessageSupportBinding binding;
        SupportViewHolder(ItemMessageSupportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message) {
            binding.tvSupportMessage.setText(message.getContent());
            binding.tvSupportMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    private static String formatTime(long ts) {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(ts));
    }
}