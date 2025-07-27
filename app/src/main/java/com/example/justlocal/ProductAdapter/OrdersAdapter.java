package com.example.justlocal.ProductAdapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.Order;
import com.example.justlocal.databinding.ItemOrderBinding;

import java.util.List;

// ✅ Adapter using ViewBinding
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private final List<Order> orders;

    public OrdersAdapter(List<Order> orders) {
        this.orders = orders;
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
        holder.binding.tvTotalAmount.setText("Total: ₱" + order.getTotalAmount());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}
