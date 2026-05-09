package com.example.wardrobeai.data;

import com.example.wardrobeai.*;

public enum Category {
    TOP, BOTTOM, SHOES, ACCESSORY, ONEPIECE;

    public int getIconRes() {
        switch (this) {
            case TOP:       return R.drawable.t_shirt;
            case BOTTOM:    return R.drawable.pants;
            case SHOES:     return R.drawable.high_heel;
            case ACCESSORY: return R.drawable.hat;
            case ONEPIECE:  return R.drawable.dress;
            default:        return R.drawable.t_shirt;
        }
    }
}