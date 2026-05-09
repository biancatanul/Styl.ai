package com.example.wardrobeai.data;

import java.util.ArrayList;
import java.util.List;

public class Outfit {
    private String id;
    private String name;
    private List<ClothingItem> items;
    private boolean isAIGenerated;
    private String reasoning;

    public Outfit(String name, List<ClothingItem> items, boolean isAIGenerated) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.items = items;
        this.isAIGenerated = isAIGenerated;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ClothingItem> getItems() {
        return items;
    }

    public boolean isAIGenerated() {
        return isAIGenerated;
    }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }


    @Override
    public String toString() {
        return "Outfit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", items=" + items +
                ", isAIGenerated=" + isAIGenerated +
                '}';
    }
}
