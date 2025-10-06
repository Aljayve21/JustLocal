package com.example.justlocal.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Order;
import com.example.justlocal.Models.Payment;
import com.example.justlocal.R;

import java.util.List;
import java.util.Map;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private final Context context;
    private final List<Payment> paymentList;
    private final Map<String, Order> orderMap;
    private final Map<String, String> customerNames;
    private final Map<String, String> sellerNames;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Payment payment);
    }

    public PaymentAdapter(Context context, List<Payment> paymentList,
                          Map<String, Order> orderMap,
                          Map<String, String> customerNames,
                          Map<String, String> sellerNames) {
        this.context = context;
        this.paymentList = paymentList;
        this.orderMap = orderMap;
        this.customerNames = customerNames;
        this.sellerNames = sellerNames;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_item_payment_cardlayout, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        // Payment Method & Icon
        holder.tvPaymentMethod.setText(payment.getMethod() != null ? payment.getMethod() : "Unknown Method");
        if (payment.getMethod() != null && payment.getMethod().toLowerCase().contains("gcash")) {
            holder.ivPaymentIcon.setImageResource(R.drawable.ic_gcash);
        } else {
            holder.ivPaymentIcon.setImageResource(R.drawable.ic_payment);
        }

        // Paid Date
        holder.tvPaymentDate.setText(payment.getPaidAt() != null ? payment.getPaidAt() : "N/A");

        // Status
        if (payment.getStatus() != null) {
            holder.tvPaymentStatus.setText(payment.getStatus());
            switch (payment.getStatus().toLowerCase()) {
                case "paid":
                case "approved":
                    holder.tvPaymentStatus.setTextColor(context.getColor(R.color.green_700));
                    holder.tvPaymentStatus.setBackgroundColor(context.getColor(R.color.green_100));
                    break;
                case "pending":
                    holder.tvPaymentStatus.setTextColor(context.getColor(R.color.yellow_800));
                    holder.tvPaymentStatus.setBackgroundColor(context.getColor(R.color.yellow_100));
                    break;
                case "rejected":
                case "failed":
                    holder.tvPaymentStatus.setTextColor(context.getColor(R.color.red_700));
                    holder.tvPaymentStatus.setBackgroundColor(context.getColor(R.color.red_100));
                    break;
                default:
                    holder.tvPaymentStatus.setTextColor(context.getColor(R.color.gray_700));
                    holder.tvPaymentStatus.setBackgroundColor(context.getColor(R.color.gray_200));
                    break;
            }
        }

        // Order ID & Reference
        holder.tvOrderID.setText(payment.getOrderID() != null ? payment.getOrderID() : "-");
        holder.tvReferenceNo.setText(payment.getReference() != null ? payment.getReference() : "-");

        // Message
        if (payment.getMessage() != null && !payment.getMessage().trim().isEmpty()) {
            holder.layoutMessage.setVisibility(View.VISIBLE);
            holder.tvPaymentMessage.setText(payment.getMessage());
        } else {
            holder.layoutMessage.setVisibility(View.GONE);
        }

        // Customer & Seller Name
        String customerName = "Unknown Customer";
        String sellerName = "Unknown Seller";
        if (payment.getOrderID() != null && orderMap.containsKey(payment.getOrderID())) {
            Order order = orderMap.get(payment.getOrderID());
            if (order != null) {
                if (order.getCustomerID() != null) {
                    customerName = customerNames.getOrDefault(order.getCustomerID(), "Unknown Customer");
                }
                if (order.getSellerID() != null) {
                    sellerName = sellerNames.getOrDefault(order.getSellerID(), "Unknown Seller");
                }
            }
        }
        holder.tvCustomerName.setText(customerName);
        holder.tvSellerName.setText(sellerName);

        // Amount Paid
        double amount = 0;
        if (payment.getOrderID() != null && orderMap.containsKey(payment.getOrderID())) {
            Order order = orderMap.get(payment.getOrderID());
            if (order != null && order.getTotalAmount() != null) {
                try {
                    amount = Double.parseDouble(order.getTotalAmount());
                } catch (NumberFormatException e) {
                    amount = 0;
                }
            }
        }
        holder.tvAmount.setText("₱" + String.format("%,.2f", amount));

        Log.d("PAYMENT_ADAPTER", "Payment OrderID=" + payment.getOrderID() +
                ", Amount=" + amount + ", Customer=" + customerName + ", Seller=" + sellerName);

        // Item Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(payment);
        });
    }

    @Override
    public int getItemCount() {
        return paymentList.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPaymentIcon;
        TextView tvPaymentMethod, tvPaymentDate, tvPaymentStatus,
                tvOrderID, tvReferenceNo, tvCustomerName, tvSellerName,
                tvPaymentMessage, tvAmount;
        LinearLayout layoutMessage;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPaymentIcon = itemView.findViewById(R.id.ivPaymentIcon);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvPaymentDate = itemView.findViewById(R.id.tvPaymentDate);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvOrderID = itemView.findViewById(R.id.tvOrderID);
            tvReferenceNo = itemView.findViewById(R.id.tvReferenceNo);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvSellerName = itemView.findViewById(R.id.tvSellerName);
            layoutMessage = itemView.findViewById(R.id.layoutMessage);
            tvPaymentMessage = itemView.findViewById(R.id.tvPaymentMessage);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
