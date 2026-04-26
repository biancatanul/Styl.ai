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
import com.example.wardrobeai.data.*;

import java.util.ArrayList;
import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.ViewHolder> {

    private final List<ClothingItem> allItems;
    private final ItemMenuListener listener;
    private List<ClothingItem> filteredItems;

    public WardrobeAdapter(List<ClothingItem> items, ItemMenuListener listener) {
        this.allItems = items;
        this.filteredItems = new ArrayList<>(items);
        this.listener = listener;
    }

    public void applyFilters(String query, List<Category> categories, List<Style> styles,
                             List<Season> seasons, List<Occasion> occasions, List<String> colorHexes) {
        filteredItems = new ArrayList<>();
        for (ClothingItem item : allItems) {
            if (!query.isEmpty() &&
                    !item.getName().toLowerCase().contains(query.toLowerCase())) continue;
            if (!categories.isEmpty() && !categories.contains(item.getCategory())) continue;
            if (!styles.isEmpty() && !styles.contains(item.getStyle())) continue;
            if (!seasons.isEmpty()) {
                boolean match = false;
                for (Season s : seasons)
                    if (item.hasSeason(s)) {
                        match = true;
                        break;
                    }
                if (!match) continue;
            }
            if (!occasions.isEmpty()) {
                boolean match = false;
                for (Occasion o : occasions)
                    if (item.hasOccasion(o)) {
                        match = true;
                        break;
                    }
                if (!match) continue;
            }
            if (!colorHexes.isEmpty()) {
                boolean match = false;
                for (String hex : colorHexes)
                    if (item.getColors().contains(hex)) {
                        match = true;
                        break;
                    }
                if (!match) continue;
            }
            filteredItems.add(item);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clothing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClothingItem item = filteredItems.get(position);

        // Category badge
        holder.textCategory.setText(item.getCategory().name());

        // Color block — use first color as background
        if (!item.getColors().isEmpty()) {
            try {
                holder.colorBlock.setBackgroundColor(Color.parseColor(item.getColors().get(0)));
            } catch (IllegalArgumentException e) {
                holder.colorBlock.setBackgroundColor(Color.LTGRAY);
            }
        } else {
            holder.colorBlock.setBackgroundColor(Color.LTGRAY);
        }

        // Circular color swatches at the bottom
        holder.colorSwatchContainer.removeAllViews();
        for (String hex : item.getColors()) {
            android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
            circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            try {
                circle.setColor(Color.parseColor(hex));
            } catch (IllegalArgumentException e) {
                circle.setColor(Color.GRAY);
            }
            circle.setStroke(2, Color.parseColor("#E8C8CC"));

            View swatch = new View(holder.colorSwatchContainer.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(36, 36);
            params.setMargins(0, 0, 6, 0);
            swatch.setLayoutParams(params);
            swatch.setBackground(circle);
            holder.colorSwatchContainer.addView(swatch);
        }

        holder.textName.setText(item.getName());

        holder.buttonMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 0, 0, "Edit");
            popup.getMenu().add(0, 1, 1, "Delete");
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 0) listener.onEdit(item);
                else {
                    new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                            .setTitle("Delete item")
                            .setMessage("Are you sure you want to delete \"" + item.getName() + "\"?")
                            .setPositiveButton("Delete", (d, w) -> listener.onDelete(item))
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    public interface ItemMenuListener {
        void onEdit(ClothingItem item);

        void onDelete(ClothingItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textCategory;
        LinearLayout colorSwatchContainer;
        ImageButton buttonMenu;
        View colorBlock;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textCategory = itemView.findViewById(R.id.textCategory);
            colorSwatchContainer = itemView.findViewById(R.id.colorSwatchContainer);
            buttonMenu = itemView.findViewById(R.id.buttonItemMenu);
            colorBlock = itemView.findViewById(R.id.colorBlock);
        }
    }
}