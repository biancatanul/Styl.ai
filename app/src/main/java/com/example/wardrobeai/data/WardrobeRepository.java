package com.example.wardrobeai.data;

import static com.example.wardrobeai.data.ClothingColor.*;

import com.example.wardrobeai.logic.CompatibilityGraph;

import java.util.ArrayList;
import java.util.List;

public class WardrobeRepository {
    private static WardrobeRepository instance;
    private List<ClothingItem> items;
    private List<Outfit> outfits;
    private WardrobeRepository() {
        items = new ArrayList<>();
        outfits = new ArrayList<>();
        seedData();
    }

    public static WardrobeRepository getInstance() {
        if (instance == null) {
            instance = new WardrobeRepository();
        }
        return instance;
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

    public boolean isNeutral(ClothingItem item) {
        return item.getColors().contains(WHITE.getHex()) || item.getColors().contains(BLACK.getHex()) || item.getColors().contains(GRAY.getHex());
    }

    public CompatibilityGraph buildCompatibilityGraph() {
        CompatibilityGraph graph = new CompatibilityGraph();
        for (ClothingItem item : items) {
            graph.addItem(item);
        }
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                ClothingItem item1 = items.get(i);
                ClothingItem item2 = items.get(j);

                if (item1.getStyle() == item2.getStyle()) {
                    graph.addEdge(item1, item2);
                }

                if (isNeutral(item1) || isNeutral(item2)) {
                    graph.addEdge(item1, item2);
                }
            }
        }
        return graph;
    }

    private void seedData() {
        addItem(new ClothingItem("White Shirt",
                List.of(ClothingColor.WHITE.getHex()),
                Category.TOP, Style.FORMAL,
                List.of(Season.SPRING.name(), Season.SUMMER.name(), Season.AUTUMN.name()),
                List.of(Occasion.WORK.name(), Occasion.EVENT.name())));

        addItem(new ClothingItem("Black Trousers",
                List.of(ClothingColor.BLACK.getHex()),
                Category.BOTTOM, Style.FORMAL,
                List.of(Season.AUTUMN.name(), Season.WINTER.name(), Season.SPRING.name()),
                List.of(Occasion.WORK.name(), Occasion.EVENT.name())));

        addItem(new ClothingItem("Black Heels",
                List.of(ClothingColor.BLACK.getHex()),
                Category.SHOES, Style.FORMAL,
                List.of(Season.SPRING.name(), Season.AUTUMN.name()),
                List.of(Occasion.WORK.name(), Occasion.EVENT.name())));

        addItem(new ClothingItem("Silver Necklace",
                List.of(ClothingColor.GRAY.getHex()),
                Category.ACCESSORY, Style.FORMAL,
                List.of(Season.SPRING.name(), Season.SUMMER.name(), Season.AUTUMN.name(), Season.WINTER.name()),
                List.of(Occasion.WORK.name(), Occasion.EVENT.name(), Occasion.DATE.name())));

        addItem(new ClothingItem("Blue Jeans",
                List.of(ClothingColor.BLUE.getHex()),
                Category.BOTTOM, Style.CASUAL,
                List.of(Season.SPRING.name(), Season.AUTUMN.name(), Season.WINTER.name()),
                List.of(Occasion.CASUAL.name(), Occasion.OUTDOOR.name())));

        addItem(new ClothingItem("White Sneakers",
                List.of(ClothingColor.WHITE.getHex()),
                Category.SHOES, Style.CASUAL,
                List.of(Season.SPRING.name(), Season.SUMMER.name(), Season.AUTUMN.name()),
                List.of(Occasion.CASUAL.name(), Occasion.OUTDOOR.name())));

        addItem(new ClothingItem("Grey Hoodie",
                List.of(ClothingColor.GRAY.getHex()),
                Category.TOP, Style.CASUAL,
                List.of(Season.AUTUMN.name(), Season.WINTER.name()),
                List.of(Occasion.CASUAL.name(), Occasion.OUTDOOR.name())));

        addItem(new ClothingItem("Black Cap",
                List.of(ClothingColor.BLACK.getHex()),
                Category.ACCESSORY, Style.CASUAL,
                List.of(Season.SPRING.name(), Season.SUMMER.name(), Season.AUTUMN.name()),
                List.of(Occasion.CASUAL.name(), Occasion.OUTDOOR.name())));
    }
}
