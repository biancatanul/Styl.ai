package com.example.wardrobeai.data;

import androidx.room.Entity;
import androidx.annotation.NonNull;

@Entity(tableName = "outfit_item_cross_ref",
        primaryKeys = {"outfitId", "itemId"})
public class OutfitItemCrossRef {

    @NonNull
    public String outfitId;

    @NonNull
    public String itemId;

    public OutfitItemCrossRef(@NonNull String outfitId, @NonNull String itemId) {
        this.outfitId = outfitId;
        this.itemId = itemId;
    }
}