package com.example.wardrobeai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wardrobeai.R;
import com.example.wardrobeai.data.Outfit;
import com.example.wardrobeai.data.WardrobeRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class OutfitsActivity extends AppCompatActivity {

    private OutfitAdapter adapter;
    private TextView emptyText;

    ActivityResultLauncher<Intent> editOutfitLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) refreshOutfits();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfits);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewOutfits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyText = findViewById(R.id.textEmptyOutfits);

        adapter = new OutfitAdapter(new ArrayList<>(), new OutfitAdapter.OutfitMenuListener() {
            @Override
            public void onEdit(Outfit outfit) {
                Intent intent = new Intent(OutfitsActivity.this, BuildOutfitActivity.class);
                intent.putExtra("edit_outfit_id", outfit.getId());
                editOutfitLauncher.launch(intent);
            }
            @Override
            public void onDelete(Outfit outfit) {
                WardrobeRepository.getInstance(OutfitsActivity.this).removeOutfit(outfit.getId());
                refreshOutfits();
            }
        });
        recyclerView.setAdapter(adapter);
        refreshOutfits();

        com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd =
                findViewById(R.id.buttonAddOutfit);
        fabAdd.setOnClickListener(v ->
                editOutfitLauncher.launch(new Intent(this, BuildOutfitActivity.class)));

        setupBottomNav();
    }

    private void refreshOutfits() {
        WardrobeRepository.getInstance(this).getAllOutfits(outfits -> {
            adapter.updateOutfits(outfits);
            emptyText.setVisibility(outfits.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshOutfits();
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_outfits);
    }
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_outfits);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_outfits)  return true;
            if (id == R.id.nav_wardrobe) { navigateTo(WardrobeActivity.class, 0); return true; }
            if (id == R.id.nav_ai)       { navigateTo(AiActivity.class, 2); return true; }
            if (id == R.id.nav_data)     { navigateTo(DataStructuresActivity.class, 3); return true; }
            return false;
        });
    }

    // currentTabIndex for OutfitsActivity is 1
    private void navigateTo(Class<?> target, int targetIndex) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

}