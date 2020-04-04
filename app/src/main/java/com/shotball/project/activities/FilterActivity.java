package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shotball.project.R;
import com.shotball.project.models.Filters;

import java.io.Serializable;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener {

    public interface FilterListener extends Serializable {
        void onFilter(Filters filters);
        Filters getCurrentFilters();
    }

    private SeekBar seekBar;
    private TextView distanceMaxTextView;
    private Button clearButton;
    private Button applyButton;

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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        seekBar = findViewById(R.id.filter_seek_bar);
        distanceMaxTextView = findViewById(R.id.filter_distance_max);
        clearButton = findViewById(R.id.filter_clear);
        applyButton = findViewById(R.id.filter_apply);

        clearButton.setOnClickListener(this);
        applyButton.setOnClickListener(this);

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
            if (b.isSelected()) {
                b.setTextColor(getResources().getColor(R.color.grey_40));
            } else {
                b.setTextColor(Color.WHITE);
            }
            b.setSelected(!b.isSelected());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_clear:
                onClearClicked();
                break;
            case R.id.filter_apply:
                onApplyClicked();
                break;
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

    public Filters getFilters() {
        Filters filters = new Filters();

        filters.setDistance(getSelectedDistance());
        //filters.setCategory(getSelectedCategory());

        return filters;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

}
