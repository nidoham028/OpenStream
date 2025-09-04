package com.nidoham.openstream.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nidoham.openstream.adapters.SuggestionsAdapter;
import com.nidoham.openstream.databinding.ActivitySearchBinding;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding; // ViewBinding instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout using ViewBinding
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupSuggestions();
        setupListeners();
    }

    /**
     * Setup the Toolbar with back button functionality
     */
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }

        // Back button closes activity
        binding.ivBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Setup Suggestions RecyclerView
     */
    private void setupSuggestions() {
        binding.suggestions.setLayoutManager(new LinearLayoutManager(this));

        // Initially show suggestions
        binding.suggestions.setVisibility(android.view.View.VISIBLE);
    }

    /**
     * Setup listeners (currently only clear button placeholder)
     */
    private void setupListeners() {
        // Clear button currently just clears text
        binding.ivClear.setOnClickListener(v -> binding.etSearch.setText(""));
    }
}