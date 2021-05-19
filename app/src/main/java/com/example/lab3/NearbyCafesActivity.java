package com.example.lab3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;

public class NearbyCafesActivity extends AppCompatActivity {

    private TextView header, noNearbyCafes;
    private ArrayList<CafeModel> cafes;
    private RecyclerView cafesRecView;
    private CafeRecViewAdapter adapter;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_cafes);

        header = findViewById(R.id.header);
        noNearbyCafes = findViewById(R.id.noNearbyCafes);
        cafesRecView = findViewById(R.id.cafesRecView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nearby_cafes);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent intent = new Intent(NearbyCafesActivity.this, LandingActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.nearby_cafes:
                        return true;
                }
                return false;
            }
        });

        // retrieve nearby cafes list passed from intent
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("bundle");
            cafes = (ArrayList<CafeModel>) bundle.getSerializable("cafes");
        }

        if (cafes.size() != 0) {
            adapter = new CafeRecViewAdapter(this);
            adapter.setCafes(cafes);
            cafesRecView.setAdapter(adapter);
            cafesRecView.setLayoutManager(new LinearLayoutManager(this));
            noNearbyCafes.setVisibility(View.GONE);
            cafesRecView.setVisibility(View.VISIBLE);
        }
    }
}