package com.example.wardrobeai.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wardrobeai.R;
import com.example.wardrobeai.data.ClothingItem;
import com.example.wardrobeai.data.Outfit;
import java.util.ArrayList;
import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    public interface OutfitMenuListener {
        void onEdit(Outfit outfit);
        void onDelete(Outfit outfit);
    }

    private final List<Outfit> outfits;
    private final OutfitMenuListener listener;

    public OutfitAdapter(List<Outfit> outfits, OutfitMenuListener listener) {
        this.outfits = outfits;
        this.listener = listener;
    }

    @NonNull @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Outfit outfit = outfits.get(position);
        holder.textOutfitName.setText(outfit.getName());

        List<String> names = new ArrayList<>();
        for (ClothingItem item : outfit.getItems())
            names.add(item.getName());
        holder.textOutfitItems.setText(String.join(", ", names));
        holder.textAITag.setVisibility(outfit.isAIGenerated() ? View.VISIBLE : View.GONE);

        holder.buttonMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 0, 0, "Edit");
            popup.getMenu().add(0, 1, 1, "Delete");
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 0) listener.onEdit(outfit);
                else listener.onDelete(outfit);
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return outfits.size(); }

    static class OutfitViewHolder extends RecyclerView.ViewHolder {
        TextView textOutfitName, textOutfitItems, textAITag;
        ImageButton buttonMenu;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            textOutfitName = itemView.findViewById(R.id.textOutfitName);
            textOutfitItems = itemView.findViewById(R.id.textOutfitItems);
            textAITag = itemView.findViewById(R.id.textAITag);
            buttonMenu = itemView.findViewById(R.id.buttonOutfitMenu);
        }
    }
}