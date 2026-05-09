package com.example.wardrobeai.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ClothingItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ClothingItem item);

    @Update
    void update(ClothingItem item);

    @Query("DELETE FROM clothing_items WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM clothing_items")
    List<ClothingItem> getAll();

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    ClothingItem getById(String id);
}