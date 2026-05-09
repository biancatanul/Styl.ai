package com.example.wardrobeai.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.*;

import java.util.*;

public class AddItemActivity extends AppCompatActivity {

    List<String> selectedColors = new ArrayList<>();
    List<String> selectedSeasons = new ArrayList<>();
    List<String> selectedOccasions = new ArrayList<>();

    private String editItemId = null;
    private GridLayout colorGrid;
    private ImageView iconCategoryPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        EditText editTextItemName = findViewById(R.id.editTextItemName);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        Spinner spinnerStyle = findViewById(R.id.spinnerStyle);
        TextView textSelectedSeasons = findViewById(R.id.textSelectedSeasons);
        TextView textSelectedOccasions = findViewById(R.id.textSelectedOccasions);
        Button buttonAddSeasons = findViewById(R.id.buttonAddSeasons);
        Button buttonAddOccasions = findViewById(R.id.buttonAddOccasions);
        Button buttonSave = findViewById(R.id.buttonSave);
        colorGrid = findViewById(R.id.colorGrid);

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Category.values());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        iconCategoryPreview = findViewById(R.id.iconCategoryPreview);

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                Category selected = (Category) spinnerCategory.getSelectedItem();
                iconCategoryPreview.setImageResource(selected.getIconRes());
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        ArrayAdapter<Style> styleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Style.values());
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStyle.setAdapter(styleAdapter);

        // check if we're in edit mode
        editItemId = getIntent().getStringExtra("edit_item_id");
        if (editItemId != null) {
            WardrobeRepository.getInstance(this).getItemById(editItemId, existing -> {
                if (existing != null) {
                    editTextItemName.setText(existing.getName());

                    Category[] categories = Category.values();
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i] == existing.getCategory()) {
                            spinnerCategory.setSelection(i);
                            iconCategoryPreview.setImageResource(categories[i].getIconRes());
                            break;
                        }
                    }

                    Style[] styles = Style.values();
                    for (int i = 0; i < styles.length; i++) {
                        if (styles[i] == existing.getStyle()) {
                            spinnerStyle.setSelection(i);
                            break;
                        }
                    }

                    selectedColors.addAll(existing.getColors());
                    selectedSeasons.addAll(existing.getSeasons());
                    selectedOccasions.addAll(existing.getOccasions());

                    textSelectedSeasons.setText(String.join(", ", selectedSeasons));
                    textSelectedOccasions.setText(String.join(", ", selectedOccasions));
                }
                setupColorPicker(); // must run after selectedColors is populated
            });
        } else {
            setupColorPicker();
        }
        buttonAddSeasons.setOnClickListener(v -> {
            String[] seasonNames = new String[Season.values().length];
            for (int i = 0; i < Season.values().length; i++)
                seasonNames[i] = Season.values()[i].name();
            boolean[] checked = new boolean[Season.values().length];
            for (int i = 0; i < Season.values().length; i++)
                checked[i] = selectedSeasons.contains(seasonNames[i]);
            new AlertDialog.Builder(this)
                    .setTitle("Select Seasons")
                    .setMultiChoiceItems(seasonNames, checked, (dialog, which, isChecked) -> {
                        if (isChecked) selectedSeasons.add(seasonNames[which]);
                        else selectedSeasons.remove(seasonNames[which]);
                    })
                    .setPositiveButton("OK", (dialog, which) ->
                            textSelectedSeasons.setText(String.join(", ", selectedSeasons)))
                    .show();
        });

        buttonAddOccasions.setOnClickListener(v -> {
            String[] occasionNames = new String[Occasion.values().length];
            for (int i = 0; i < Occasion.values().length; i++)
                occasionNames[i] = Occasion.values()[i].name();
            boolean[] checked = new boolean[Occasion.values().length];
            for (int i = 0; i < Occasion.values().length; i++)
                checked[i] = selectedOccasions.contains(occasionNames[i]);
            new AlertDialog.Builder(this)
                    .setTitle("Select Occasions")
                    .setMultiChoiceItems(occasionNames, checked, (dialog, which, isChecked) -> {
                        if (isChecked) selectedOccasions.add(occasionNames[which]);
                        else selectedOccasions.remove(occasionNames[which]);
                    })
                    .setPositiveButton("OK", (dialog, which) ->
                            textSelectedOccasions.setText(String.join(", ", selectedOccasions)))
                    .show();
        });

        buttonSave.setOnClickListener(v -> {
            String name = editTextItemName.getText().toString().trim();
            if (name.isEmpty()) {
                editTextItemName.setError("Name required");
                return;
            }

            Category category = (Category) spinnerCategory.getSelectedItem();
            Style style = (Style) spinnerStyle.getSelectedItem();
            ClothingItem item = new ClothingItem(name, selectedColors, category, style,
                    selectedSeasons, selectedOccasions);

            if (editItemId != null) {
                item.setId(editItemId); // preserve original ID so @Update finds the row
                WardrobeRepository.getInstance(this).updateItem(editItemId, item);
            } else {
                WardrobeRepository.getInstance(this).addItem(item);
            }

            setResult(RESULT_OK);
            finish();
        });
    }

    private void setupColorPicker() {
        colorGrid.removeAllViews();
        int circleSize = dpToPx(44);
        int margin = dpToPx(6);

        for (ClothingColor color : ClothingColor.values()) {
            View circle = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = circleSize;
            params.height = circleSize;
            params.setMargins(margin, margin, margin, margin);
            circle.setLayoutParams(params);

            setCircleDrawable(circle, color, selectedColors.contains(color.getHex()));

            circle.setOnClickListener(v -> {
                String hex = color.getHex();
                if (selectedColors.contains(hex)) {
                    selectedColors.remove(hex);
                    setCircleDrawable(circle, color, false);
                } else {
                    selectedColors.add(hex);
                    setCircleDrawable(circle, color, true);
                }
            });

            colorGrid.addView(circle);
        }
    }

    private void setCircleDrawable(View circle, ClothingColor color, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor(color.getHex()));
        if (selected) {
            int strokeColor = (color == ClothingColor.WHITE || color == ClothingColor.BEIGE)
                    ? Color.parseColor("#555555")
                    : Color.parseColor("#1a1a1a");
            drawable.setStroke(dpToPx(3), strokeColor);
        } else {
            drawable.setStroke(dpToPx(1), Color.parseColor("#CCCCCC"));
        }
        circle.setBackground(drawable);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}