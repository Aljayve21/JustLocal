package com.example.justlocal.DeliveryTimeline;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.DeliveryInfo;
import com.example.justlocal.databinding.ItemDeliveryTimelineBinding;

import java.util.List;

public class DeliveryTimelineAdapter extends RecyclerView.Adapter<DeliveryTimelineAdapter.TimelineViewHolder> {

    private final List<DeliveryInfo> timeline;

    public DeliveryTimelineAdapter(List<DeliveryInfo> timeline) {
        this.timeline = timeline;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TimelineViewHolder(ItemDeliveryTimelineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        DeliveryInfo status = timeline.get(position);
        holder.binding.tvDeliveryStatus.setText(status.getStatus());
        holder.binding.tvDeliveryDateTime.setText(status.getTimestamp());

        if (status.getCarrier() != null && !status.getCarrier().isEmpty()) {
            holder.binding.tvCarrierInfo.setVisibility(View.VISIBLE);
            holder.binding.tvCarrierInfo.setText("Carrier: " + status.getCarrier());
        } else {
            holder.binding.tvCarrierInfo.setVisibility(View.GONE);
        }

        // Hide timeline lines for first/last item
        holder.binding.viewLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.binding.viewLineBottom.setVisibility(position == timeline.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return timeline.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        ItemDeliveryTimelineBinding binding;

        public TimelineViewHolder(ItemDeliveryTimelineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}