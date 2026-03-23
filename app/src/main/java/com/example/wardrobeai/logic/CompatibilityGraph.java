package com.example.wardrobeai.logic;

import com.example.wardrobeai.data.ClothingItem;

import java.util.*;

public class CompatibilityGraph {
    private HashMap<String, List<String>> adjacencyList;

    public CompatibilityGraph() {
        adjacencyList = new HashMap<>();
    }

    public void addItem(ClothingItem item){
        if (!adjacencyList.containsKey(item.getId()))
            adjacencyList.put(item.getId(), new ArrayList<>());}

    public void addEdge(ClothingItem item1, ClothingItem item2){
        if (!adjacencyList.containsKey(item1.getId()) || !adjacencyList.containsKey(item2.getId())) return;
        adjacencyList.get(item1.getId()).add(item2.getId());
        adjacencyList.get(item2.getId()).add(item1.getId());
    }

    public boolean areCompatible(ClothingItem item1, ClothingItem item2){
        return adjacencyList.get(item1.getId()).contains(item2.getId());
    }
    public List<String> getCompatibleItems(ClothingItem item){
        return adjacencyList.getOrDefault(item.getId(), new ArrayList<>());    }
}
