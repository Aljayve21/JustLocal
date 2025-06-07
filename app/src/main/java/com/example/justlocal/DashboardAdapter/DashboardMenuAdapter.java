package com.example.justlocal.DashboardAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.R;

import java.util.List;

public class DashboardMenuAdapter extends RecyclerView.Adapter<DashboardMenuAdapter.MenuViewHolder> {

    private List<DashboardMenuItem> menuItems;
    private OnMenuItemClickListener listener;

    public interface OnMenuItemClickListener {
        void onMenuItemClick(DashboardMenuItem item);
    }

    public DashboardMenuAdapter(List<DashboardMenuItem> menuItems, OnMenuItemClickListener listener) {
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboardmenuitem, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        DashboardMenuItem item = menuItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuIcon;
        TextView tvMenuTitle, tvMenuCount;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuIcon = itemView.findViewById(R.id.ivMenuIcon);
            tvMenuTitle = itemView.findViewById(R.id.tvMenuTitle);
            tvMenuCount = itemView.findViewById(R.id.tvMenuCount);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuItemClick(menuItems.get(getAdapterPosition()));
                }
            });
        }

        public void bind(DashboardMenuItem item) {
            tvMenuTitle.setText(item.getTitle());
            ivMenuIcon.setImageResource(item.getIconRes());

            if (item.getCount() > 0) {
                tvMenuCount.setText(String.valueOf(item.getCount()));
                tvMenuCount.setVisibility(View.VISIBLE);
            } else {
                tvMenuCount.setVisibility(View.GONE);
            }
        }
    }
}

