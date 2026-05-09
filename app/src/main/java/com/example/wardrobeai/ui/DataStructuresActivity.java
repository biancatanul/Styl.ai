package com.example.wardrobeai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.*;
import com.example.wardrobeai.logic.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class DataStructuresActivity extends AppCompatActivity {

    private FrameLayout container;
    private LinearLayout nodeDetailPanel;
    private TextView nodeDetailTitle;
    private TextView nodeDetailBody;

    private RedBlackTree rbt;
    private BinomialHeap heap;
    private CompatibilityGraph graph;
    private List<ClothingItem> cachedItems = new ArrayList<>();
    private boolean structuresReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_structures);

        container = findViewById(R.id.visualizationContainer);
        nodeDetailPanel = findViewById(R.id.nodeDetailPanel);
        nodeDetailTitle = findViewById(R.id.nodeDetailTitle);
        nodeDetailBody = findViewById(R.id.nodeDetailBody);

        findViewById(R.id.nodeDetailDismiss).setOnClickListener(v -> {
            nodeDetailPanel.setVisibility(View.GONE);
            clearCurrentSelection();
        });

        buildStructures();

        String[] options = {"Red-Black Tree", "Binomial Heap", "Compatibility Graph"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = findViewById(R.id.spinnerStructure);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showView(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupBottomNav();
    }

    private void clearCurrentSelection() {
        if (container.getChildCount() == 0) return;
        View v = container.getChildAt(0);
        if (v instanceof RedBlackTreeView) ((RedBlackTreeView) v).clearSelection();
        if (v instanceof BinomialHeapView) ((BinomialHeapView) v).clearSelection();
        if (v instanceof CompatibilityGraphView) ((CompatibilityGraphView) v).clearSelection();
    }

    private void showPanel(String title, String details) {
        nodeDetailTitle.setText(title);
        nodeDetailBody.setText(details);
        nodeDetailPanel.setVisibility(View.VISIBLE);
        nodeDetailPanel.post(() -> {
            View v = container.getChildAt(0);
            if (v instanceof RedBlackTreeView)       ((RedBlackTreeView)       v).executePendingCenter();
            if (v instanceof BinomialHeapView)       ((BinomialHeapView)       v).executePendingCenter();
            if (v instanceof CompatibilityGraphView) ((CompatibilityGraphView) v).executePendingCenter();
        });
    }

    private void buildStructures() {
        WardrobeRepository repo = WardrobeRepository.getInstance(this);
        repo.buildCompatibilityGraph(g -> {
            graph = g;
            repo.getAllItems(items -> {
                cachedItems = items;

                rbt = new RedBlackTree();
                for (ClothingItem item : items) rbt.insert(item);

                CSPSolver csp = new CSPSolver(items, graph);
                heap = new BinomialHeap();
                int total = 0;

                outer:
                for (Style style : Style.values()) {
                    for (Season season : Season.values()) {
                        for (Occasion occasion : Occasion.values()) {
                            List<Outfit> outfits = csp.suggestOutfits(style, season, occasion, 2);
                            for (Outfit outfit : outfits) {
                                heap.insert(outfit, BinomialHeap.scoreOutfit(outfit, graph));
                                if (++total >= 20) break outer;
                            }
                        }
                    }
                }

                structuresReady = true;
                Spinner spinner = findViewById(R.id.spinnerStructure);
                showView(spinner.getSelectedItemPosition());
            });
        });
    }

    private void showView(int position) {
        if (!structuresReady) return;
        container.removeAllViews();
        nodeDetailPanel.setVisibility(View.GONE);
        switch (position) {
            case 0: {
                RedBlackTreeView view = new RedBlackTreeView(this, rbt);
                view.setOnNodeTappedListener((title, details) -> showPanel(title, details));
                container.addView(view);
                break;
            }
            case 1: {
                BinomialHeapView view = new BinomialHeapView(this, heap);
                view.setOnNodeTappedListener((title, details) -> showPanel(title, details));
                container.addView(view);
                break;
            }
            case 2: {
                CompatibilityGraphView view = new CompatibilityGraphView(this, cachedItems, graph);
                view.setOnNodeTappedListener((title, details) -> showPanel(title, details));
                container.addView(view);
                break;
            }
        }
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_data);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_data) return true;
            if (id == R.id.nav_wardrobe) {
                navigateTo(WardrobeActivity.class, 0);
                return true;
            }
            if (id == R.id.nav_outfits) {
                navigateTo(OutfitsActivity.class, 1);
                return true;
            }
            if (id == R.id.nav_ai) {
                navigateTo(AiActivity.class, 2);
                return true;
            }
            return false;
        });
    }

    private void navigateTo(Class<?> target, int targetIndex) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_data);
    }
}