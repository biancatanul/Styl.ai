package com.example.wardrobeai.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "outfits")
public class Outfit {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private boolean isAIGenerated;
    private String reasoning;

    // Room doesn't store this -- it's populated via @Relation in the DAO
    @Ignore
    private List<ClothingItem> items;

    // Constructor used by the rest of the app
    @Ignore
    public Outfit(String name, List<ClothingItem> items, boolean isAIGenerated) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.items = items;
        this.isAIGenerated = isAIGenerated;
    }

    // Required by Room
    public Outfit() {
        this.items = new ArrayList<>();
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAIGenerated(boolean AIGenerated) { isAIGenerated = AIGenerated; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public void setItems(List<ClothingItem> items) { this.items = items; }

    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isAIGenerated() { return isAIGenerated; }
    public String getReasoning() { return reasoning; }
    public List<ClothingItem> getItems() { return items; }

    @Override
    public String toString() {
        return "Outfit{id='" + id + "', name='" + name + "', isAIGenerated=" + isAIGenerated + '}';
    }
}