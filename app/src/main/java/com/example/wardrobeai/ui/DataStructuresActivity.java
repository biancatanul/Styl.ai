package com.example.wardrobeai.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.*;
import com.example.wardrobeai.logic.*;

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
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void buildStructures() {
        WardrobeRepository repo = WardrobeRepository.getInstance();
        List<ClothingItem> items = repo.getAllItems();

        rbt = new RedBlackTree();
        for (ClothingItem item : items) rbt.insert(item);

        graph = repo.buildCompatibilityGraph();

        // heap needs outfits, but passing an empty one for now,
        // visualization will still show structure
        heap = new BinomialHeap();
        for (ClothingItem item : items) {
            // insert dummy single-item outfits so the heap has nodes to display
            com.example.wardrobeai.data.Outfit o = new com.example.wardrobeai.data.Outfit(
                    item.getName(), java.util.List.of(item), false);
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
}