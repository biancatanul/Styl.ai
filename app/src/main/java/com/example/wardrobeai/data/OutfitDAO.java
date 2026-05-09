package com.example.wardrobeai.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface OutfitDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOutfit(Outfit outfit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCrossRef(OutfitItemCrossRef crossRef);

    @Query("DELETE FROM outfits WHERE id = :id")
    void deleteOutfitById(String id);

    @Query("DELETE FROM outfit_item_cross_ref WHERE outfitId = :outfitId")
    void deleteCrossRefsForOutfit(String outfitId);

    @Transaction
    @Query("SELECT * FROM outfits")
    List<OutfitWithItems> getAllOutfitsWithItems();

    @Transaction
    @Query("SELECT * FROM outfits WHERE id = :id")
    OutfitWithItems getOutfitWithItemsById(String id);
}