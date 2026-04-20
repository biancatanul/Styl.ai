package com.example.wardrobeai.ui;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wardrobeai.R;
import com.example.wardrobeai.data.*;
import java.util.*;

public class BuildOutfitActivity extends AppCompatActivity {

    List<ClothingItem> selectedItems = new ArrayList<>();
    private String editOutfitId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_outfit);

        EditText editTextOutfitName = findViewById(R.id.editTextOutfitName);
        Button buttonOpenWardrobe = findViewById(R.id.buttonOpenWardrobe);
        Button buttonSave = findViewById(R.id.buttonSave);
        TextView textSelectedItems = findViewById(R.id.textSelectedItems);

        editOutfitId = getIntent().getStringExtra("edit_outfit_id");
        if (editOutfitId != null) {
            Outfit existing = WardrobeRepository.getInstance().getOutfitById(editOutfitId);
            if (existing != null) {
                editTextOutfitName.setText(existing.getName());
                selectedItems.addAll(existing.getItems());
                List<String> names = new ArrayList<>();
                for (ClothingItem item : selectedItems) names.add(item.getName());
                textSelectedItems.setText(String.join(", ", names));
            }
        }

        buttonOpenWardrobe.setOnClickListener(v -> {
            List<ClothingItem> allItems = WardrobeRepository.getInstance().getAllItems();
            String[] itemNames = new String[allItems.size()];
            boolean[] checked = new boolean[allItems.size()];
            for (int i = 0; i < allItems.size(); i++) {
                itemNames[i] = allItems.get(i).getName();
                checked[i] = selectedItems.contains(allItems.get(i));
            }
            new AlertDialog.Builder(this)
                    .setTitle("Select Items")
                    .setMultiChoiceItems(itemNames, checked, (dialog, which, isChecked) -> {
                        if (isChecked) selectedItems.add(allItems.get(which));
                        else selectedItems.remove(allItems.get(which));
                    })
                    .setPositiveButton("OK", (dialog, which) -> {
                        List<String> names = new ArrayList<>();
                        for (ClothingItem item : selectedItems) names.add(item.getName());
                        textSelectedItems.setText(String.join(", ", names));
                    })
                    .show();
        });

        buttonSave.setOnClickListener(v -> {
            String name = editTextOutfitName.getText().toString().trim();
            if (name.isEmpty()) {
                editTextOutfitName.setError("Name required");
                return;
            }
            Outfit outfit = new Outfit(name, selectedItems, false);
            if (editOutfitId != null)
                WardrobeRepository.getInstance().updateOutfit(editOutfitId, outfit);
            else
                WardrobeRepository.getInstance().addOutfit(outfit);
            setResult(RESULT_OK);
            finish();
        });
    }
}