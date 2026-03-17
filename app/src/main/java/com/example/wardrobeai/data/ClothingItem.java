package com.example.wardrobeai.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClothingItem {
    private String id;
    private String name;
    private List<String> colors;
    private Category category;
    private Style style;
    private Season[] seasons;
    private Occasion[] occasions;

    public ClothingItem(String name, List<String> colors, Category category, Style style, Season[] seasons, Occasion[] occasions){
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.colors = colors;
        this.category = category;
        this.style = style;
        this.seasons = seasons;
        this.occasions = occasions;
    }

    @Override
    public String toString() {
        return "ClothingItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", colors='" + colors + '\'' +
                ", category=" + category +
                ", style=" + style +
                ", seasons=" + Arrays.toString(seasons) +
                ", occasions=" + Arrays.toString(occasions) +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getColors() {
        return colors;
    }

    public Category getCategory() {
        return category;
    }

    public Style getStyle() {
        return style;
    }

    public Season[] getSeasons() {
        return seasons;
    }

    public Occasion[] getOccasions() {
        return occasions;
    }

    public boolean hasOccasion(Occasion occasion) {
        for (Occasion o : occasions) {
            if (o == occasion) return true;
        }
        return false;
    }

    public boolean hasSeason(Season season) {
        for (Season s : seasons) {
            if (s == season) return true;
        }
        return false;
    }
}
