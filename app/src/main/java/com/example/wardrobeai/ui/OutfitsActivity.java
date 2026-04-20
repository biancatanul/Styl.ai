package com.example.wardrobeai.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wardrobeai.R;
import com.example.wardrobeai.data.Outfit;
import com.example.wardrobeai.data.WardrobeRepository;
import java.util.List;

public class OutfitsActivity extends AppCompatActivity {

    private OutfitAdapter adapter;

    ActivityResultLauncher<Intent> editOutfitLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK)
                    adapter.notifyDataSetChanged();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfits);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewOutfits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Outfit> outfits = WardrobeRepository.getInstance().getAllOutfits();

        adapter = new OutfitAdapter(outfits, new OutfitAdapter.OutfitMenuListener() {
            @Override
            public void onEdit(Outfit outfit) {
                Intent intent = new Intent(OutfitsActivity.this, BuildOutfitActivity.class);
                intent.putExtra("edit_outfit_id", outfit.getId());
                editOutfitLauncher.launch(intent);
            }

            @Override
            public void onDelete(Outfit outfit) {
                WardrobeRepository.getInstance().removeOutfit(outfit.getId());
                adapter.notifyDataSetChanged();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}