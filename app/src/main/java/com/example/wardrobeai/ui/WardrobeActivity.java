package com.example.wardrobeai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.*;

import java.util.ArrayList;
import java.util.List;

public class WardrobeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WardrobeAdapter adapter;
    ActivityResultLauncher<Intent> addItemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK)
                    adapter.notifyDataSetChanged();
            }
    );
    private Button buttonAddItem, buttonBuildOutfit, buttonViewOutfits,
            buttonAiSuggest, buttonDataStructures;
    private EditText editTextSearch;
    private ImageButton buttonFilter;
    // active filters
    private List<Category> filterCategories = new ArrayList<>();
    private List<Style> filterStyles = new ArrayList<>();
    private List<Season> filterSeasons = new ArrayList<>();
    private List<Occasion> filterOccasions = new ArrayList<>();
    private List<String> filterColorHexes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        recyclerView = findViewById(R.id.recyclerViewWardrobe);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonBuildOutfit = findViewById(R.id.buttonBuildOutfit);
        buttonViewOutfits = findViewById(R.id.buttonViewOutfits);
        buttonAiSuggest = findViewById(R.id.buttonAiSuggest);
        buttonDataStructures = findViewById(R.id.buttonDataStructures);
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonFilter = findViewById(R.id.buttonFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<ClothingItem> items = WardrobeRepository.getInstance().getAllItems();
        adapter = new WardrobeAdapter(items, new WardrobeAdapter.ItemMenuListener() {
            @Override
            public void onEdit(ClothingItem item) {
                Intent intent = new Intent(WardrobeActivity.this, AddItemActivity.class);
                intent.putExtra("edit_item_id", item.getId());
                addItemLauncher.launch(intent);
            }

            @Override
            public void onDelete(ClothingItem item) {
                WardrobeRepository.getInstance().removeItem(item.getId());
                adapter.notifyDataSetChanged();
            }
        });
        recyclerView.setAdapter(adapter);

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonFilter.setOnClickListener(v -> showFilterDialog());

        buttonAddItem.setOnClickListener(v ->
                addItemLauncher.launch(new Intent(this, AddItemActivity.class)));
        buttonBuildOutfit.setOnClickListener(v ->
                startActivity(new Intent(this, BuildOutfitActivity.class)));
        buttonViewOutfits.setOnClickListener(v ->
                startActivity(new Intent(this, OutfitsActivity.class)));
        buttonAiSuggest.setOnClickListener(v ->
                startActivity(new Intent(this, AiActivity.class)));
        buttonDataStructures.setOnClickListener(v ->
                startActivity(new Intent(this, DataStructuresActivity.class)));
    }

    private void showFilterDialog() {
        String[] fields = {"Category", "Style", "Season", "Occasion", "Color", "Clear all filters"};
        new AlertDialog.Builder(this)
                .setTitle("Filter by")
                .setItems(fields, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            pickCategory();
                            break;
                        case 1:
                            pickStyle();
                            break;
                        case 2:
                            pickSeason();
                            break;
                        case 3:
                            pickOccasion();
                            break;
                        case 4:
                            pickColor();
                            break;
                        case 5:
                            filterCategories.clear();
                            filterStyles.clear();
                            filterSeasons.clear();
                            filterOccasions.clear();
                            filterColorHexes.clear();
                            applyFilters();
                            break;
                    }
                })
                .show();
    }

    private void pickCategory() {
        Category[] values = Category.values();
        String[] names = new String[values.length];
        boolean[] checked = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
            checked[i] = filterCategories.contains(values[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle("Category")
                .setMultiChoiceItems(names, checked, (d, which, isChecked) -> {
                    if (isChecked) filterCategories.add(values[which]);
                    else filterCategories.remove(values[which]);
                })
                .setPositiveButton("OK", (d, which) -> applyFilters())
                .show();
    }

    private void pickStyle() {
        Style[] values = Style.values();
        String[] names = new String[values.length];
        boolean[] checked = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
            checked[i] = filterStyles.contains(values[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle("Style")
                .setMultiChoiceItems(names, checked, (d, which, isChecked) -> {
                    if (isChecked) filterStyles.add(values[which]);
                    else filterStyles.remove(values[which]);
                })
                .setPositiveButton("OK", (d, which) -> applyFilters())
                .show();
    }

    private void pickSeason() {
        Season[] values = Season.values();
        String[] names = new String[values.length];
        boolean[] checked = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
            checked[i] = filterSeasons.contains(values[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle("Season")
                .setMultiChoiceItems(names, checked, (d, which, isChecked) -> {
                    if (isChecked) filterSeasons.add(values[which]);
                    else filterSeasons.remove(values[which]);
                })
                .setPositiveButton("OK", (d, which) -> applyFilters())
                .show();
    }

    private void pickOccasion() {
        Occasion[] values = Occasion.values();
        String[] names = new String[values.length];
        boolean[] checked = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
            checked[i] = filterOccasions.contains(values[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle("Occasion")
                .setMultiChoiceItems(names, checked, (d, which, isChecked) -> {
                    if (isChecked) filterOccasions.add(values[which]);
                    else filterOccasions.remove(values[which]);
                })
                .setPositiveButton("OK", (d, which) -> applyFilters())
                .show();
    }

    private void pickColor() {
        ClothingColor[] values = ClothingColor.values();
        String[] names = new String[values.length];
        boolean[] checked = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
            checked[i] = filterColorHexes.contains(values[i].getHex());
        }
        new AlertDialog.Builder(this)
                .setTitle("Color")
                .setMultiChoiceItems(names, checked, (d, which, isChecked) -> {
                    if (isChecked) filterColorHexes.add(values[which].getHex());
                    else filterColorHexes.remove(values[which].getHex());
                })
                .setPositiveButton("OK", (d, which) -> applyFilters())
                .show();
    }

    private void applyFilters() {
        String query = editTextSearch.getText().toString();
        adapter.applyFilters(query, filterCategories, filterStyles,
                filterSeasons, filterOccasions, filterColorHexes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}