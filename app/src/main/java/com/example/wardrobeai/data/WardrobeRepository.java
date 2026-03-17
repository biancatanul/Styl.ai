package com.example.wardrobeai.data;

import java.util.ArrayList;
import java.util.List;

public class WardrobeRepository {
    private static WardrobeRepository instance;

    public static WardrobeRepository getInstance() {
        if (instance == null) {
            instance = new WardrobeRepository();
        }
        return instance;
    }
    private List<ClothingItem> items;
    private List<Outfit> outfits;

    private WardrobeRepository() {
        items = new ArrayList<>();
        outfits = new ArrayList<>();
    }
    public void addItem(ClothingItem item) {
        items.add(item);
    }

    public List<ClothingItem> getAllItems() {
        return items;
    }

    public List<ClothingItem> getItemsByCategory(Category category) {
        List<ClothingItem> result = new ArrayList<>();
        for (ClothingItem item : items) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    public void removeItem(String id) {
        for (ClothingItem item : items) {
            if (item.getId().equals(id)) {
                items.remove(item);
                break;
            }
        }
    }
    public void addOutfit(Outfit outfit) {
        outfits.add(outfit);
    }

    public void removeOutfit(String id) {
        for (Outfit outfit : outfits) {
            if (outfit.getId().equals(id)) {
                outfits.remove(outfit);
                break;
            }
        }
    }

    public List<Outfit> getAllOutfits() {
        return outfits;
    }
}
