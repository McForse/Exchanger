package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.shotball.project.R;
import com.shotball.project.models.ExchangeModel;
import com.shotball.project.viewHolders.ExchangeViewHolder;

public class ExchangeActivity extends BaseActivity {

    private static final String TAG = "ExchangeActivity";

    private CoordinatorLayout mainContainer;
    private RecyclerView recyclerView;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerPagingAdapter<ExchangeModel, ExchangeViewHolder> mAdapter;

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
        recyclerView = findViewById(R.id.exchange_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query baseQuery = mDatabase.child("exchanges").orderByChild("whom").equalTo(getUid());

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        DatabasePagingOptions<ExchangeModel> options = new DatabasePagingOptions.Builder<ExchangeModel>()
                .setLifecycleOwner(this)
                .setQuery(baseQuery, config, ExchangeModel.class)
                .build();

        mAdapter = new FirebaseRecyclerPagingAdapter<ExchangeModel, ExchangeViewHolder>(options) {
            @Override
            public ExchangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_exchange, parent, false);

                return new ExchangeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ExchangeViewHolder holder, int position, @NonNull ExchangeModel model) {
                Log.d(TAG, model.what_exchange);
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                        // The initial load has begun
                        // ...
                    case LOADING_MORE:
                        // The adapter has started to load an additional page
                        // ...
                    case LOADED:
                        // The previous load (either initial or additional) completed
                        // ...
                    case ERROR:
                        // The previous load (either initial or additional) failed. Call
                        // the retry() method in order to retry the load operation.
                        // ...
                }
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
