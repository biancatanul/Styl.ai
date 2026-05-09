package com.example.wardrobeai.data;

import static com.example.wardrobeai.data.ClothingColor.*;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.wardrobeai.logic.CompatibilityGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WardrobeRepository {

    public interface Callback<T> {
        void onResult(T result);
    }

    private static WardrobeRepository instance;
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private WardrobeRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public static WardrobeRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WardrobeRepository(context.getApplicationContext());
        }
        return instance;
    }

    // ── Clothing Items ──────────────────────────────────────────────

    public void addItem(ClothingItem item) {
        executor.execute(() -> db.clothingItemDao().insert(item));
    }

    public void updateItem(String id, ClothingItem updated) {
        executor.execute(() -> db.clothingItemDao().update(updated));
    }

    public void removeItem(String id) {
        executor.execute(() -> db.clothingItemDao().deleteById(id));
    }

    public void getAllItems(Callback<List<ClothingItem>> callback) {
        executor.execute(() -> {
            List<ClothingItem> items = db.clothingItemDao().getAll();
            mainHandler.post(() -> callback.onResult(items));
        });
    }

    public void getItemById(String id, Callback<ClothingItem> callback) {
        executor.execute(() -> {
            ClothingItem item = db.clothingItemDao().getById(id);
            mainHandler.post(() -> callback.onResult(item));
        });
    }

    // ── Outfits ─────────────────────────────────────────────────────

    public void addOutfit(Outfit outfit) {
        executor.execute(() -> {
            db.outfitDao().insertOutfit(outfit);
            for (ClothingItem item : outfit.getItems()) {
                db.outfitDao().insertCrossRef(
                        new OutfitItemCrossRef(outfit.getId(), item.getId())
                );
            }
        });
    }

    public void removeOutfit(String id) {
        executor.execute(() -> {
            db.outfitDao().deleteCrossRefsForOutfit(id);
            db.outfitDao().deleteOutfitById(id);
        });
    }
    public void updateOutfit(String id, Outfit updated) {
        executor.execute(() -> {
            updated.setId(id);
            db.outfitDao().deleteCrossRefsForOutfit(id);
            db.outfitDao().insertOutfit(updated);
            for (ClothingItem item : updated.getItems()) {
                db.outfitDao().insertCrossRef(new OutfitItemCrossRef(id, item.getId()));
            }
        });
    }

    public void getAllOutfits(Callback<List<Outfit>> callback) {
        executor.execute(() -> {
            List<OutfitWithItems> rows = db.outfitDao().getAllOutfitsWithItems();
            List<Outfit> outfits = new ArrayList<>();
            for (OutfitWithItems row : rows) {
                row.outfit.setItems(row.items);
                outfits.add(row.outfit);
            }
            mainHandler.post(() -> callback.onResult(outfits));
        });
    }

    public void getOutfitById(String id, Callback<Outfit> callback) {
        executor.execute(() -> {
            OutfitWithItems row = db.outfitDao().getOutfitWithItemsById(id);
            if (row != null) row.outfit.setItems(row.items);
            mainHandler.post(() -> callback.onResult(row == null ? null : row.outfit));
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────

    public boolean isNeutral(ClothingItem item) {
        return item.getColors().contains(WHITE.getHex())
                || item.getColors().contains(BLACK.getHex())
                || item.getColors().contains(GRAY.getHex());
    }

    public void buildCompatibilityGraph(Callback<CompatibilityGraph> callback) {
        getAllItems(items -> {
            CompatibilityGraph graph = new CompatibilityGraph();
            for (ClothingItem item : items) graph.addItem(item);
            for (int i = 0; i < items.size(); i++) {
                for (int j = i + 1; j < items.size(); j++) {
                    ClothingItem a = items.get(i), b = items.get(j);
                    if (a.getStyle() == b.getStyle() || isNeutral(a) || isNeutral(b)) {
                        graph.addEdge(a, b);
                    }
                }
            }
            callback.onResult(graph);
        });
    }
}