package com.example.wardrobeai.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import androidx.annotation.NonNull;
import java.util.List;

@Entity(tableName = "clothing_items")
@TypeConverters(Converters.class)
public class ClothingItem {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private List<String> colors;
    private Category category;
    private Style style;
    private List<String> seasons;
    private List<String> occasions;

    // Constructor used in the rest of the app (generates UUID)
    public ClothingItem(String name, List<String> colors, Category category, Style style,
                        List<String> seasons, List<String> occasions) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.colors = colors;
        this.category = category;
        this.style = style;
        this.seasons = seasons;
        this.occasions = occasions;
    }

    // Required by Room to reconstruct objects from DB rows
    public ClothingItem() {}

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public void setCategory(Category category) { this.category = category; }
    public void setStyle(Style style) { this.style = style; }
    public void setSeasons(List<String> seasons) { this.seasons = seasons; }
    public void setOccasions(List<String> occasions) { this.occasions = occasions; }


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
