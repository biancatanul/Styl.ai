package com.example.wardrobeai.logic;

import com.example.wardrobeai.data.*;

import java.util.*;

public class CSPSolver {
    private List<ClothingItem> items;
    private CompatibilityGraph graph;

    private List<ClothingItem> getItemsForSlot(Style style, Category category, Season season, Occasion occasion){
        List<ClothingItem> result = new ArrayList<>();
        for (ClothingItem item : items) {
            if (item.getCategory() == category && item.hasSeason(season) && item.hasOccasion(occasion) && item.getStyle() == style)
                result.add(item);
        }
        return result;
    }

    public CSPSolver(List<ClothingItem> items, CompatibilityGraph graph) {
        this.items = items;
        this.graph = graph;
    }

    public List<Outfit> suggestOutfits(Style style, Season season, Occasion occasion, int count) {
        List<ClothingItem> tops        = getItemsForSlot(style, Category.TOP, season, occasion);
        List<ClothingItem> bottoms     = getItemsForSlot(style, Category.BOTTOM, season, occasion);
        List<ClothingItem> shoes       = getItemsForSlot(style, Category.SHOES, season, occasion);
        List<ClothingItem> accessories = getItemsForSlot(style, Category.ACCESSORY, season, occasion);

        // null sentinel so the accessory loop always runs at least once
        if (accessories.isEmpty()) accessories.add(null);

        List<Outfit> outfits = new ArrayList<>();

        for (ClothingItem top : tops) {
            for (ClothingItem bottom : bottoms) {
                for (ClothingItem shoe : shoes) {
                    for (ClothingItem accessory : accessories) {
                        boolean compatible = graph.areCompatible(shoe, top)
                                && graph.areCompatible(shoe, bottom)
                                && graph.areCompatible(top, bottom);
                        if (accessory != null) {
                            compatible = compatible
                                    && graph.areCompatible(top, accessory)
                                    && graph.areCompatible(bottom, accessory)
                                    && graph.areCompatible(shoe, accessory);
                        }
                        if (compatible) {
                            List<ClothingItem> chosen = new ArrayList<>();
                            chosen.add(top);
                            chosen.add(bottom);
                            chosen.add(shoe);
                            if (accessory != null) chosen.add(accessory);
                            outfits.add(new Outfit("AI suggestion " + (outfits.size() + 1), chosen, true));
                            if (outfits.size() == count) return outfits;
                        }
                    }
                }
            }
        }
        return outfits;
    }
}
