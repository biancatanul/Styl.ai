package com.example.wardrobeai.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wardrobeai.R;
import com.example.wardrobeai.data.ClothingItem;
import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.ViewHolder> {

    public interface ItemMenuListener {
        void onEdit(ClothingItem item);
        void onDelete(ClothingItem item);
    }

    private final List<ClothingItem> items;
    private final ItemMenuListener listener;

    public WardrobeAdapter(List<ClothingItem> items, ItemMenuListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clothing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClothingItem item = items.get(position);

        holder.colorSwatchContainer.removeAllViews();
        for (String hex : item.getColors()) {
            View swatch = new View(holder.colorSwatchContainer.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(40, 40);
            params.setMargins(4, 0, 4, 0);
            swatch.setLayoutParams(params);
            try {
                swatch.setBackgroundColor(Color.parseColor(hex));
            } catch (IllegalArgumentException e) {
                swatch.setBackgroundColor(Color.GRAY);
            }
            holder.colorSwatchContainer.addView(swatch);
        }

        holder.textName.setText(item.getName());
        holder.textDetails.setText(item.getCategory() + " · " + item.getStyle());

        holder.buttonMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 0, 0, "Edit");
            popup.getMenu().add(0, 1, 1, "Delete");
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 0) listener.onEdit(item);
                else listener.onDelete(item);
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDetails;
        LinearLayout colorSwatchContainer;
        ImageButton buttonMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textDetails = itemView.findViewById(R.id.textItemDetails);
            colorSwatchContainer = itemView.findViewById(R.id.colorSwatchContainer);
            buttonMenu = itemView.findViewById(R.id.buttonItemMenu);
        }
    }
}