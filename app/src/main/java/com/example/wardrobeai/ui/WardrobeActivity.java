package com.example.wardrobeai.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wardrobeai.R;
import com.example.wardrobeai.data.ClothingItem;
import com.example.wardrobeai.data.WardrobeRepository;
import java.util.List;


public class WardrobeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button buttonAddItem;
    private WardrobeAdapter adapter;
    private Button buttonBuildOutfit;
    private Button buttonViewOutfits;
    private Button buttonAiSuggest;

    ActivityResultLauncher<Intent> addItemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    adapter.notifyDataSetChanged();
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        recyclerView = findViewById(R.id.recyclerViewWardrobe);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonBuildOutfit = findViewById(R.id.buttonBuildOutfit);
        buttonViewOutfits = findViewById(R.id.buttonViewOutfits);
        buttonAiSuggest = findViewById(R.id.buttonAiSuggest);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<ClothingItem> items = WardrobeRepository.getInstance().getAllItems();
        adapter = new WardrobeAdapter(items);
        recyclerView.setAdapter(adapter);

        buttonAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            addItemLauncher.launch(intent);
        });

        buttonBuildOutfit.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuildOutfitActivity.class);
            startActivity(intent);
        });

        buttonViewOutfits.setOnClickListener(v -> {
            Intent intent = new Intent(this, OutfitsActivity.class);
            startActivity(intent);
        });

        buttonAiSuggest.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiActivity.class);
            startActivity(intent);
        });
        }
    @Override
    protected void onResume(){
        super.onResume();
        adapter.notifyDataSetChanged();
    }

}