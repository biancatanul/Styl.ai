package com.example.wardrobeai.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wardrobeai.R;
import com.example.wardrobeai.data.Outfit;
import com.example.wardrobeai.data.WardrobeRepository;
import java.util.List;

public class OutfitsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfits);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewOutfits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Outfit> outfits = WardrobeRepository.getInstance().getAllOutfits();
        recyclerView.setAdapter(new OutfitAdapter(outfits));
    }
}