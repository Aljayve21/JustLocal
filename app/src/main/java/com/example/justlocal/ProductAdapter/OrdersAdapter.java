package com.example.justlocal.ProductAdapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Order;
import com.example.justlocal.databinding.ItemOrderBinding;

import java.util.List;
import java.util.function.Consumer;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private final List<Order> orders;
    private final Consumer<Order> onClick;

    public OrdersAdapter(List<Order> orders, Consumer<Order> onClick) {
        this.orders = orders;
        this.onClick = onClick;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.binding.tvOrderID.setText("Order #" + order.getOrderID());
        holder.binding.tvOrderDate.setText(order.getOrderDate());
        holder.binding.tvOrderStatus.setText("Status: " + order.getStatus());

        try {
            double total = Double.parseDouble(order.getTotalAmount());
            holder.binding.tvTotalAmount.setText("Total: ₱" + String.format("%.2f", total));
        } catch (NumberFormatException e) {
            holder.binding.tvTotalAmount.setText("Total: ₱0.00");
        }

        holder.itemView.setOnClickListener(v -> onClick.accept(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
