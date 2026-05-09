package com.example.wardrobeai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.*;
import com.example.wardrobeai.logic.BinomialHeap;
import com.example.wardrobeai.logic.CSPSolver;
import com.example.wardrobeai.logic.CompatibilityGraph;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.*;

public class AiActivity extends AppCompatActivity {

    private Spinner occasionSpinner, seasonSpinner, styleSpinner;
    private MaterialButton suggestButton, prevButton, nextButton, saveButton;
    private TextView suggestionIndexText;
    // resultsLayout is now a MaterialCardView — View is the safe common type
    private View resultsLayout;
    private LinearLayout itemsLayout;

    private List<Outfit> suggestions = new ArrayList<>();
    private int currentIndex = 0;
    private Season selectedSeason;
    private Occasion selectedOccasion;
    private Style selectedStyle;

    private WardrobeRepository repo;
    private CompatibilityGraph graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        repo = WardrobeRepository.getInstance(this);
        repo.buildCompatibilityGraph(g -> {
            graph = g;
        });

        occasionSpinner     = findViewById(R.id.occasionSpinner);
        seasonSpinner       = findViewById(R.id.seasonSpinner);
        styleSpinner        = findViewById(R.id.styleSpinner);
        suggestButton       = findViewById(R.id.suggestButton);
        prevButton          = findViewById(R.id.previousButton);
        nextButton          = findViewById(R.id.nextButton);
        saveButton          = findViewById(R.id.saveButton);
        suggestionIndexText = findViewById(R.id.suggestionIndexText);
        resultsLayout       = findViewById(R.id.resultsLayout);
        itemsLayout         = findViewById(R.id.itemsLayout);

        ArrayAdapter<Season> a1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Season.values());
        a1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonSpinner.setAdapter(a1);

        ArrayAdapter<Occasion> a2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Occasion.values());
        a2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        occasionSpinner.setAdapter(a2);

        ArrayAdapter<Style> a3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Style.values());
        a3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        styleSpinner.setAdapter(a3);

        suggestButton.setOnClickListener(v -> {
            selectedOccasion = (Occasion) occasionSpinner.getSelectedItem();
            selectedSeason   = (Season)   seasonSpinner.getSelectedItem();
            selectedStyle    = (Style)    styleSpinner.getSelectedItem();

            repo.getAllItems(items -> {
                CSPSolver solver = new CSPSolver(items, graph);
                List<Outfit> candidates = solver.suggestOutfits(selectedStyle, selectedSeason, selectedOccasion, 10);

                if (candidates.isEmpty()) {
                    Toast.makeText(this, "No outfits found for these filters", Toast.LENGTH_SHORT).show();
                } else {
                    BinomialHeap heap = BinomialHeap.fromOutfits(candidates, graph);
                    suggestions  = heap.drainSorted();
                    currentIndex = 0;
                    resultsLayout.setVisibility(View.VISIBLE);
                    displayOutfit();
                }
            });
        });

        prevButton.setOnClickListener(v -> {
            if (currentIndex > 0) { currentIndex--; displayOutfit(); }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < suggestions.size() - 1) { currentIndex++; displayOutfit(); }
        });

        saveButton.setOnClickListener(v -> {
            repo.addOutfit(suggestions.get(currentIndex));
            Toast.makeText(this, "Outfit saved", Toast.LENGTH_SHORT).show();
        });

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_ai);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ai)       return true;
            if (id == R.id.nav_wardrobe) { navigateTo(WardrobeActivity.class, 0); return true; }
            if (id == R.id.nav_outfits)  { navigateTo(OutfitsActivity.class, 1); return true; }
            if (id == R.id.nav_data)     { navigateTo(DataStructuresActivity.class, 3); return true; }
            return false;
        });
    }

    // currentTabIndex for AiActivity is 2
    private void navigateTo(Class<?> target, int targetIndex) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void displayOutfit() {
        Outfit current = suggestions.get(currentIndex);
        suggestionIndexText.setText("Suggestion " + (currentIndex + 1) + " of " + suggestions.size());
        itemsLayout.removeAllViews();

        StringBuilder fullReasoning = new StringBuilder();
        for (ClothingItem item : current.getItems()) {
            String itemReasoning = generateReasoning(item);
            TextView tv = new TextView(this);
            tv.setTypeface(ResourcesCompat.getFont(this, R.font.elms_sans));
            tv.setText(item.getName() + "\n" + itemReasoning);
            tv.setPadding(0, 12, 0, 12);
            tv.setTextColor(getColor(R.color.text_dark));
            itemsLayout.addView(tv);

            fullReasoning.append(item.getName())
                    .append(" (").append(item.getCategory().name()).append(")")
                    .append("\n").append(itemReasoning).append("\n\n");
        }
        current.setReasoning(fullReasoning.toString().trim());
    }

    private String generateReasoning(ClothingItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.hasSeason(selectedSeason))    sb.append("matches season: ").append(selectedSeason.name()).append("  ");
        if (item.hasOccasion(selectedOccasion)) sb.append("matches occasion: ").append(selectedOccasion.name()).append("  ");
        if (item.getStyle() == selectedStyle)  sb.append("matches style: ").append(selectedStyle.name()).append("  ");
        if (repo.isNeutral(item))              sb.append("neutral color — goes with anything");
        return sb.toString().trim();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_ai);
    }
}