package com.example.wardrobeai.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;
import androidx.room.TypeConverters;

import java.util.List;

@TypeConverters(Converters.class)
public class OutfitWithItems {

    @Embedded
    public Outfit outfit;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = OutfitItemCrossRef.class,
                    parentColumn = "outfitId",
                    entityColumn = "itemId"
            )
    )
    public List<ClothingItem> items;
}