package com.example.wardrobeai.logic;

import com.example.wardrobeai.data.*;

import java.util.*;

public class CSPSolver {
    private List<ClothingItem> items;
    private CompatibilityGraph graph;

    private List<ClothingItem> getItemsForSlot(Category category, Season season, Occasion occasion){
        List<ClothingItem> result = new ArrayList<>();
        for (ClothingItem item : items) {
            if (item.getCategory() == category && item.hasSeason(season) && item.hasOccasion(occasion))
                result.add(item);
        }
        return result;
    }

    public CSPSolver(List<ClothingItem> items, CompatibilityGraph graph) {
        this.items = items;
        this.graph = graph;
    }

    public List<Outfit> suggestOutfits(String style, Season season, Occasion occasion, int count) {
        List<ClothingItem> tops = getItemsForSlot(Category.TOP, season, occasion);
        List<ClothingItem> bottoms = getItemsForSlot(Category.BOTTOM, season, occasion);
        List<ClothingItem> shoes = getItemsForSlot(Category.SHOES, season, occasion);
        List<ClothingItem> accessories = getItemsForSlot(Category.ACCESSORY, season, occasion);
        List<Outfit> outfits = new ArrayList<>();

        for(ClothingItem top : tops) {
            for(ClothingItem bottom : bottoms){
                for(ClothingItem shoe : shoes) {
                    for (ClothingItem accessory : accessories) {
                        if (graph.areCompatible(shoe, accessory) && graph.areCompatible(shoe, top) && graph.areCompatible(shoe, bottom) && graph.areCompatible(top, bottom) && graph.areCompatible(top, accessory) && graph.areCompatible(bottom, accessory)) {
                            List<ClothingItem> outfit = new ArrayList<>();
                            outfit.add(top);
                            outfit.add(bottom);
                            outfit.add(shoe);
                            outfit.add(accessory);
                            Outfit newOutfit = new Outfit("AI suggestion " + (outfits.size() + 1), outfit, true);
                            outfits.add(newOutfit);
                            if (outfits.size() == count) {
                                return outfits;
                            }
                        }
                    }
                }
            }
        }
        return outfits;
    }
}
