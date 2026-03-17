package com.example.wardrobeai.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        ClothingItem item = items.get(position);
        holder.textName.setText(item.getName());
        holder.textDetails.setText(
                item.getCategory() + " · " + item.getStyle()
        );
        try {
            holder.colorSwatch.setBackgroundColor(Color.parseColor(item.getColor()));
        } catch (Exception e) {
            holder.colorSwatch.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textDetails;
        View colorSwatch;

        public ViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textDetails = itemView.findViewById(R.id.textItemDetails);
            colorSwatch = itemView.findViewById(R.id.viewColorSwatch);
        }
    }
}