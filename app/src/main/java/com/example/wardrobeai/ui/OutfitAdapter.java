package com.example.wardrobeai.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.ClothingItem;
import com.example.wardrobeai.data.Outfit;

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
        holder.textAITag.setVisibility(outfit.isAIGenerated() ? View.VISIBLE : View.GONE);

        holder.itemTilesContainer.removeAllViews();
        Context ctx = holder.itemView.getContext();
        float density = ctx.getResources().getDisplayMetrics().density;
        int tileSizePx = (int) (68 * density);
        int cornerPx   = (int) (10 * density);
        int marginPx   = (int) (6  * density);
        int namePadPx  = (int) (3  * density);

        for (ClothingItem item : outfit.getItems()) {
            LinearLayout tile = new LinearLayout(ctx);
            tile.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tileParams =
                    new LinearLayout.LayoutParams(tileSizePx, LinearLayout.LayoutParams.WRAP_CONTENT);
            tileParams.setMargins(0, 0, marginPx, 0);
            tile.setLayoutParams(tileParams);

            // Color block
            FrameLayout colorBlock = new FrameLayout(ctx);
            colorBlock.setLayoutParams(new LinearLayout.LayoutParams(tileSizePx, tileSizePx));

            int bgColor = Color.LTGRAY;
            if (item.getColors() != null && !item.getColors().isEmpty()) {
                try { bgColor = Color.parseColor(item.getColors().get(0)); }
                catch (IllegalArgumentException ignored) {}
            }
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(cornerPx);
            bg.setColor(bgColor);
            colorBlock.setBackground(bg);

            // Category icon
            ImageView iconView = new ImageView(ctx);
            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                    (int)(32 * density), (int)(32 * density));
            iconParams.gravity = Gravity.CENTER;
            iconView.setLayoutParams(iconParams);
            iconView.setImageResource(item.getCategory().getIconRes());
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            double lum = (0.299 * Color.red(bgColor)
                    + 0.587 * Color.green(bgColor)
                    + 0.114 * Color.blue(bgColor)) / 255.0;
            iconView.setColorFilter(lum > 0.5 ? Color.BLACK : Color.WHITE);

            colorBlock.addView(iconView);

            // Item name
            TextView nameView = new TextView(ctx);
            nameView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            nameView.setText(item.getName());
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
            nameView.setMaxLines(1);
            nameView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            nameView.setGravity(Gravity.CENTER);
            nameView.setTextColor(Color.parseColor("#888888"));
            nameView.setPadding(0, namePadPx, 0, 0);

            tile.addView(colorBlock);
            tile.addView(nameView);
            holder.itemTilesContainer.addView(tile);
        }

        holder.buttonMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 0, 0, "Edit");
            popup.getMenu().add(0, 1, 1, "Delete");
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 0) listener.onEdit(outfit);
                else {
                    new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                            .setTitle("Delete outfit")
                            .setMessage("Are you sure you want to delete \"" + outfit.getName() + "\"?")
                            .setPositiveButton("Delete", (d, w) -> listener.onDelete(outfit))
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return outfits.size(); }

    static class OutfitViewHolder extends RecyclerView.ViewHolder {
        TextView textOutfitName, textAITag;
        LinearLayout itemTilesContainer;
        ImageButton buttonMenu;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            textOutfitName     = itemView.findViewById(R.id.textOutfitName);
            textAITag          = itemView.findViewById(R.id.textAITag);
            buttonMenu         = itemView.findViewById(R.id.buttonOutfitMenu);
            itemTilesContainer = itemView.findViewById(R.id.itemTilesContainer);
        }
    }
}