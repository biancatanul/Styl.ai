package com.example.wardrobeai.data;

public enum ClothingColor {
    WHITE("#FFFFFF"),
    BLACK("#000000"),
    GRAY("#757575"),
    RED("#ff2626"),
    ORANGE("#ff8936"),
    YELLOW("#fcd926"),
    GREEN("#08a117"),
    TEAL("#02bfc9"),
    BLUE("#0077e6"),
    PURPLE("#9122e6"),
    PINK("#f06ecf"),
    BEIGE("#d1c3b0");

    private final String hex;
    ClothingColor(String hex) {
        this.hex = hex;
    }
    public String getHex() {
        return hex;
    }

}
