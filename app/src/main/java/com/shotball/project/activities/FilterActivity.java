package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.shotball.project.R;
import com.shotball.project.models.Filters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FilterActivity extends AppCompatActivity {

    public interface FilterListener extends Serializable {
        void onFilter(Filters filters);
        Filters getCurrentFilters();
    }

    private SeekBar seekBar;
    private TextView distanceMaxTextView;
    private ExtendedFloatingActionButton applyButton;

    private FilterListener mFilterListener;

    private Map<Integer, Boolean> selectedCategories = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        initToolbar();
        initComponent();
        findViewById(R.id.category_0).setSelected(true);
        ((Button)findViewById(R.id.category_0)).setTextColor(Color.WHITE);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.filter_title));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chevron_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        seekBar = findViewById(R.id.filter_seek_bar);
        distanceMaxTextView = findViewById(R.id.filter_distance_max);
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

    public void btToggleClick(View view) {
        if (view instanceof Button) {
            Button b = (Button) view;

            switch (view.getId()) {
                case R.id.category_0:
                    break;
                case R.id.category_1:
                    break;
                case R.id.category_2:
                    break;
                case R.id.category_3:
                    break;
                case R.id.category_4:
                    break;
                case R.id.category_5:
                    break;
                case R.id.category_6:
                    break;
                case R.id.category_7:
                    break;

            }


            if (b.isSelected()) {
                b.setTextColor(getResources().getColor(R.color.grey_40));
            } else {
                b.setTextColor(Color.WHITE);
            }
            b.setSelected(!b.isSelected());
        }
    }

    private void initFilters(Filters filter) {
        int progress = filter.getDistance() / 100;
        seekBar.setProgress(progress);
        setSeekBarProgress(progress);
    }

    private void setSeekBarProgress(int progress) {
        int distance = progress * 100;
        if (distance <= 1000) {
            distanceMaxTextView.setText(String.valueOf(distance) + " meters");
        } else {
            distanceMaxTextView.setText(String.valueOf((float)distance / 1000) + " km");
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
            mFilterListener.onFilter(getFilters());
        }

        finish();
    }

    private int getSelectedDistance() {
        return seekBar.getProgress() * 100;
    }

    private Map<Integer, Boolean> getSelectedCategory() {
        return null;
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        filters.setDistance(getSelectedDistance());
        filters.setCategories(getSelectedCategory());

        return filters;
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
