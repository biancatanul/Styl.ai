package com.example.wardrobeai.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {ClothingItem.class, Outfit.class, OutfitItemCrossRef.class},
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract ClothingItemDAO clothingItemDao();
    public abstract OutfitDAO outfitDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "wardrobe_db"
                    ).build();
                }
            }
        }
        return instance;
    }
}