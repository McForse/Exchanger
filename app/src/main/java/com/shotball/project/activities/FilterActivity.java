package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.shotball.project.R;
import com.shotball.project.models.Categories;
import com.shotball.project.models.Filters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "FilterActivity";

    public interface FilterListener extends Serializable {
        void onFilter(Filters filters);
        Filters getCurrentFilters();
    }

    private LinearLayout mainContainer;
    private SeekBar seekBar;
    private TextView distanceMaxTextView;
    private ChipGroup categoriesGroup;
    private ExtendedFloatingActionButton applyButton;

    private FilterListener mFilterListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.filter_title));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chevron_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        mainContainer = findViewById(R.id.filter_container);
        seekBar = findViewById(R.id.filter_seek_bar);
        distanceMaxTextView = findViewById(R.id.filter_distance_max);
        categoriesGroup = findViewById(R.id.categories_group);
        applyButton = findViewById(R.id.filter_apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onApplyClicked();
            }
        });

        mFilterListener = (FilterListener) getIntent().getSerializableExtra("interface");

        if (mFilterListener != null && mFilterListener.getCurrentFilters() != null) {
            initFilters(mFilterListener.getCurrentFilters());
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekBarProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void initFilters(Filters filter) {
        int progress = filter.getDistance() / 100;
        seekBar.setProgress(progress);
        setSeekBarProgress(progress);

        for (int i = 0; i < categoriesGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoriesGroup.getChildAt(i);

            if (filter.hasCategory(i)) {
                chip.setChecked(true);
                if (i == Categories.All.getValue()) break;
            }
        }
    }

    private void setSeekBarProgress(int progress) {
        int distance = progress * 100;
        if (distance <= 1000) {
            distanceMaxTextView.setText(String.format(getString(R.string.distance_meters), distance));
        } else {
            distanceMaxTextView.setText(String.format(getString(R.string.distance_km), (float)distance / 1000));
        }
    }

    public void onCategoryClick(View view) {
        Chip chip = (Chip) view;

        if (view.getId() == R.id.category_0) {
            if (chip.isChecked()) {
                for (int i = 1; i < categoriesGroup.getChildCount(); i++) {
                    ((Chip) categoriesGroup.getChildAt(i)).setChecked(false);
                }
            }
        } else {
            ((Chip) findViewById(R.id.category_0)).setChecked(false);
        }
    }

    private void onClearClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(Filters.getDefault());
        }

        finish();
    }

    private void onApplyClicked() {
        if (mFilterListener != null) {
            boolean valid = false;
            Filters filters = getFilters();
            for (Map.Entry<Integer, Boolean> category : filters.getCategories().entrySet()) {
                if (category.getValue()) {
                    valid = true;
                    break;
                }
            }

            if (valid) {
                mFilterListener.onFilter(filters);
                finish();
            } else {
                applyButton.setVisibility(View.GONE);
                Snackbar.make(mainContainer, R.string.dialog_no_category_selected, Snackbar.LENGTH_SHORT).addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        applyButton.show();
                    }

                    @Override
                    public void onShown(Snackbar snackbar) { }
                }).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { }
                }).show();
            }
        }
    }

    private int getSelectedDistance() {
        return seekBar.getProgress() * 100;
    }

    private Map<Integer, Boolean> getSelectedCategories() {
        return getCheckedCategories();
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        filters.setDistance(getSelectedDistance());
        filters.setCategories(getSelectedCategories());

        return filters;
    }

    @NonNull
    public Map<Integer, Boolean> getCheckedCategories() {
        Map<Integer, Boolean> checkedCategories = new HashMap<>();

        for (int i = 0; i < categoriesGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoriesGroup.getChildAt(i);
            checkedCategories.put(i, chip.isChecked());
        }

        return checkedCategories;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_clear) {
            onClearClicked();
        }

        return super.onOptionsItemSelected(item);
    }

}
