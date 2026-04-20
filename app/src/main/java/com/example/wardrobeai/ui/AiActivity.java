package com.example.wardrobeai.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.*;
import com.example.wardrobeai.logic.BinomialHeap;
import com.example.wardrobeai.logic.CSPSolver;
import com.example.wardrobeai.logic.CompatibilityGraph;

import java.util.*;


public class AiActivity extends AppCompatActivity {

    private Spinner occasionSpinner, seasonSpinner, styleSpinner;
    private Button suggestButton, prevButton, nextButton, saveButton;
    private TextView suggestionIndexText;
    private LinearLayout resultsLayout, itemsLayout;

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

        repo = WardrobeRepository.getInstance();
        graph = repo.buildCompatibilityGraph();

        occasionSpinner = findViewById(R.id.occasionSpinner);
        seasonSpinner = findViewById(R.id.seasonSpinner);
        styleSpinner = findViewById(R.id.styleSpinner);
        suggestButton = findViewById(R.id.suggestButton);
        prevButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        saveButton = findViewById(R.id.saveButton);
        suggestionIndexText = findViewById(R.id.suggestionIndexText);
        resultsLayout = findViewById(R.id.resultsLayout);
        itemsLayout = findViewById(R.id.itemsLayout);

        ArrayAdapter<Season> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Season.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seasonSpinner.setAdapter(adapter);

        ArrayAdapter<Occasion> adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Occasion.values());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        occasionSpinner.setAdapter(adapter2);

        ArrayAdapter<Style> adapter3 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Style.values());
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        styleSpinner.setAdapter(adapter3);

        suggestButton.setOnClickListener(v -> {
            selectedOccasion = (Occasion) occasionSpinner.getSelectedItem();
            selectedSeason = (Season) seasonSpinner.getSelectedItem();
            selectedStyle = (Style) styleSpinner.getSelectedItem();

            CSPSolver solver = new CSPSolver(repo.getAllItems(), graph);
            List<Outfit> candidates = solver.suggestOutfits(selectedStyle, selectedSeason, selectedOccasion, 10);

            if (candidates.isEmpty()) {
                Toast.makeText(AiActivity.this, "No outfits found", Toast.LENGTH_SHORT).show();
            } else {
                BinomialHeap heap = BinomialHeap.fromOutfits(candidates, graph);
                suggestions = heap.drainSorted();
                currentIndex = 0;
                resultsLayout.setVisibility(View.VISIBLE);
                displayOutfit();
            }
        });

        prevButton.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                displayOutfit();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (currentIndex < suggestions.size() - 1) {
                currentIndex++;
                displayOutfit();
            }
        });

        saveButton.setOnClickListener(v -> {
            repo.addOutfit(suggestions.get(currentIndex));
            Toast.makeText(AiActivity.this, "Outfit saved", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayOutfit() {
        suggestionIndexText.setText("Suggestion " + (currentIndex + 1) + " of " + suggestions.size());
        itemsLayout.removeAllViews();
        for (ClothingItem item : suggestions.get(currentIndex).getItems()) {
            TextView itemTextView = new TextView(this);
            itemTextView.setText(item.getName() + "\n" + generateReasoning(item));
            itemsLayout.addView(itemTextView);
        }
    }

    private String generateReasoning(ClothingItem item) {
        StringBuilder reasoning = new StringBuilder();
        if (item.hasSeason(selectedSeason))
            reasoning.append("(matches season: ").append(selectedSeason.name()).append(") ");
        if (item.hasOccasion(selectedOccasion))
            reasoning.append("(matches occasion: ").append(selectedOccasion.name()).append(") ");
        if (item.getStyle() == selectedStyle)
            reasoning.append("(matches style: ").append(selectedStyle.name()).append(") ");
        if (repo.isNeutral(item))
            reasoning.append("(neutral color: goes with anything) ");
        return reasoning.toString().trim();
    }
}