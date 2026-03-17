package com.example.wardrobeai.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        recyclerView = findViewById(R.id.recyclerViewWardrobe);
        buttonAddItem = findViewById(R.id.buttonAddItem);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<ClothingItem> items = WardrobeRepository.getInstance().getAllItems();
        adapter = new WardrobeAdapter(items);
        recyclerView.setAdapter(adapter);

        buttonAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onResume(){
        super.onResume();
        adapter.notifyDataSetChanged();
    }

}