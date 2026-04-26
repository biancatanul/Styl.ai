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

import java.util.List;

public class DataStructuresActivity extends AppCompatActivity {

    private FrameLayout container;
    private RedBlackTree rbt;
    private BinomialHeap heap;
    private CompatibilityGraph graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_structures);

        container = findViewById(R.id.visualizationContainer);
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

    private void buildStructures() {
        WardrobeRepository repo = WardrobeRepository.getInstance();
        List<ClothingItem> items = repo.getAllItems();

        rbt = new RedBlackTree();
        for (ClothingItem item : items) rbt.insert(item);

        graph = repo.buildCompatibilityGraph();

        heap = new BinomialHeap();
        for (ClothingItem item : items) {
            Outfit o = new Outfit(item.getName(), java.util.List.of(item), false);
            heap.insert(o, BinomialHeap.scoreOutfit(o, graph));
        }
    }

    private void showView(int position) {
        container.removeAllViews();
        switch (position) {
            case 0:
                container.addView(new RedBlackTreeView(this, rbt));
                break;
            case 1:
                container.addView(new BinomialHeapView(this, heap));
                break;
            case 2:
                container.addView(new CompatibilityGraphView(this, WardrobeRepository.getInstance().getAllItems(), graph));
                break;
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

    // currentTabIndex for DataStructuresActivity is 3
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