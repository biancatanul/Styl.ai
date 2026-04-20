package com.example.wardrobeai.ui;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        EditText editTextItemName = findViewById(R.id.editTextItemName);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        Spinner spinnerStyle = findViewById(R.id.spinnerStyle);
        Button buttonAddColor = findViewById(R.id.buttonAddColor);
        TextView textSelectedColors = findViewById(R.id.textSelectedColors);
        TextView textSelectedSeasons = findViewById(R.id.textSelectedSeasons);
        TextView textSelectedOccasions = findViewById(R.id.textSelectedOccasions);
        Button buttonAddSeasons = findViewById(R.id.buttonAddSeasons);
        Button buttonAddOccasions = findViewById(R.id.buttonAddOccasions);
        Button buttonSave = findViewById(R.id.buttonSave);

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Category.values());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<Style> styleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Style.values());
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStyle.setAdapter(styleAdapter);

        // check if we're in edit mode
        editItemId = getIntent().getStringExtra("edit_item_id");
        if (editItemId != null) {
            ClothingItem existing = WardrobeRepository.getInstance().getItemById(editItemId);
            if (existing != null) {
                editTextItemName.setText(existing.getName());

                // pre-select category spinner
                Category[] categories = Category.values();
                for (int i = 0; i < categories.length; i++) {
                    if (categories[i] == existing.getCategory()) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }

                // pre-select style spinner
                Style[] styles = Style.values();
                for (int i = 0; i < styles.length; i++) {
                    if (styles[i] == existing.getStyle()) {
                        spinnerStyle.setSelection(i);
                        break;
                    }
                }

                // pre-populate multi-select lists
                selectedColors.addAll(existing.getColors());
                selectedSeasons.addAll(existing.getSeasons());
                selectedOccasions.addAll(existing.getOccasions());

                textSelectedColors.setText(String.join(", ", selectedColors));
                textSelectedSeasons.setText(String.join(", ", selectedSeasons));
                textSelectedOccasions.setText(String.join(", ", selectedOccasions));
            }
        }

        buttonAddColor.setOnClickListener(v -> {
            String[] colorNames = new String[ClothingColor.values().length];
            for (int i = 0; i < ClothingColor.values().length; i++)
                colorNames[i] = ClothingColor.values()[i].name();
            boolean[] checked = new boolean[ClothingColor.values().length];
            for (int i = 0; i < ClothingColor.values().length; i++)
                checked[i] = selectedColors.contains(ClothingColor.values()[i].getHex());
            new AlertDialog.Builder(this)
                    .setTitle("Select Colors")
                    .setMultiChoiceItems(colorNames, checked, (dialog, which, isChecked) -> {
                        String hex = ClothingColor.values()[which].getHex();
                        if (isChecked) selectedColors.add(hex);
                        else selectedColors.remove(hex);
                    })
                    .setPositiveButton("OK", (dialog, which) ->
                            textSelectedColors.setText(String.join(", ", selectedColors)))
                    .show();
        });

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
                WardrobeRepository.getInstance().updateItem(editItemId, item);
            } else {
                WardrobeRepository.getInstance().addItem(item);
            }

            setResult(RESULT_OK);
            finish();
        });
    }
}