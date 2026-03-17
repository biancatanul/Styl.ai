package com.example.wardrobeai.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wardrobeai.R;
import com.example.wardrobeai.data.ClothingItem;
import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.ViewHolder> {

    private List<ClothingItem> items;

    public WardrobeAdapter(List<ClothingItem> items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clothing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.colorSwatchContainer.removeAllViews();
        for (String hex : items.get(position).getColors()) {
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
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textDetails;
        LinearLayout colorSwatchContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textDetails = itemView.findViewById(R.id.textItemDetails);
            colorSwatchContainer = itemView.findViewById(R.id.colorSwatchContainer);        }
    }
}