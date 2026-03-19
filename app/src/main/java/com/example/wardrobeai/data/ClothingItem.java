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
    private List<String> seasons;
    private List<String> occasions;

    public ClothingItem(String name, List<String> colors, Category category, Style style, List<String> seasons, List<String> occasions){
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
                ", seasons=" + seasons +
                ", occasions=" + occasions +
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

    public List<String> getSeasons() {
        return seasons;
    }

    public List<String> getOccasions() {
        return occasions;
    }

    public boolean hasOccasion(Occasion occasion) {
        return occasions.contains(occasion.name());
    }

    public boolean hasSeason(Season season) {
        return seasons.contains(season.name());
    }
}
