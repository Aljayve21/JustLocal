package com.example.justlocal.UserAdapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.justlocal.Models.User;
import com.example.justlocal.R;
import com.example.justlocal.databinding.UserItemLayoutBinding;

import java.util.List;
import java.util.function.Consumer;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final List<User> users;
    private final Consumer<User> onItemClick;
    private final Consumer<User> onEditClick;
    private final Consumer<User> onDeleteClick;

    public UserAdapter(List<User> users, Consumer<User> onItemClick, Consumer<User> onEditClick, Consumer<User> onDeleteClick) {
        this.users = users;
        this.onItemClick = onItemClick;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UserItemLayoutBinding binding = UserItemLayoutBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final UserItemLayoutBinding binding;

        ViewHolder(UserItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.tvUserName.setText(user.getFullName());
            binding.tvUserEmail.setText(user.getEmail());
            binding.tvUserRole.setText(user.getRole());
            binding.tvUserStatus.setText(user.getStatus());

            int roleBg = "Customer".equalsIgnoreCase(user.getRole()) ? R.drawable.role_badge_customer :
                    "Seller".equalsIgnoreCase(user.getRole()) ? R.drawable.role_badge_seller :
                            R.drawable.role_badge_csr;
            binding.tvUserRole.setBackgroundResource(roleBg);

            int statusBg = "Active".equalsIgnoreCase(user.getStatus()) ?
                    R.drawable.status_badge_active : R.drawable.status_badge_inactive;
            binding.tvUserStatus.setBackgroundResource(statusBg);

            // Decode base64 avatar
            try {
                String base64String = user.getAvatarUrl(); // assuming this is the base64 string
                if (base64String != null && !base64String.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    binding.ivUserAvatar.setImageBitmap(bitmap);
                } else {
                    binding.ivUserAvatar.setImageResource(R.drawable.ic_default_avatar);
                }
            } catch (Exception e) {
                e.printStackTrace();
                binding.ivUserAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            binding.getRoot().setOnClickListener(v -> onItemClick.accept(user));
            binding.btnUserActions.setOnClickListener(v -> showUserActions(user));
        }


        private void showUserActions(User user) {
            PopupMenu popup = new PopupMenu(binding.getRoot().getContext(), binding.btnUserActions);
            popup.inflate(R.menu.user_actions_layout);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.user_action_edit) {
                    // Handle edit action
                    onEditClick.accept(user);
                    return true;
                } else if (id == R.id.user_action_delete) {
                    // Handle delete action
                    onDeleteClick.accept(user);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}

