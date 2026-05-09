package com.example.wardrobeai.data;

import androidx.room.TypeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Converters {

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(",", list);
    }

    @TypeConverter
    public static List<String> toList(String value) {
        if (value == null || value.isEmpty()) return Collections.emptyList();
        return Arrays.asList(value.split(","));
    }

    @TypeConverter
    public static String fromCategory(Category category) {
        return category == null ? null : category.name();
    }

    @TypeConverter
    public static Category toCategory(String value) {
        return value == null ? null : Category.valueOf(value);
    }

    @TypeConverter
    public static String fromStyle(Style style) {
        return style == null ? null : style.name();
    }

    @TypeConverter
    public static Style toStyle(String value) {
        return value == null ? null : Style.valueOf(value);
    }
}