package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.shotball.project.R;
import com.shotball.project.models.ExchangeModel;
import com.shotball.project.utils.ViewAnimation;
import com.shotball.project.viewHolders.ExchangeViewHolder;

public class ExchangeActivity extends BaseActivity {

    private static final String TAG = "ExchangeActivity";

    private CoordinatorLayout mainContainer;
    private RecyclerView recyclerView;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<ExchangeModel, ExchangeViewHolder> mAdapter;

    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.exchange_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chevron_left);
    }

    private void initComponent() {
        mainContainer = findViewById(R.id.exchange_container);
        //ViewAnimation.showOut(mainContainer);
        recyclerView = findViewById(R.id.exchange_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query baseQuery = mDatabase.child("exchanges").orderByChild("whom").equalTo(getUid());

        FirebaseRecyclerOptions<ExchangeModel> options = new FirebaseRecyclerOptions.Builder<ExchangeModel>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, ExchangeModel.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<ExchangeModel, ExchangeViewHolder>(options) {
            @Override
            public ExchangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_exchange, parent, false);

                return new ExchangeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ExchangeViewHolder holder, int position, @NonNull ExchangeModel model) {
                Log.d(TAG, model.what_exchange);
                holder.bind(ExchangeActivity.this, model);
            }
        };

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exchange, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
