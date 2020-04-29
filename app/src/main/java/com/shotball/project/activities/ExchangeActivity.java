package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.shotball.project.R;
import com.shotball.project.fragments.AcceptedExchangesFragment;
import com.shotball.project.fragments.OffersExchangesFragment;
import com.shotball.project.fragments.RefusedExchangesFragment;
import com.shotball.project.models.ExchangeModel;
import com.shotball.project.viewHolders.ExchangeViewHolder;

public class ExchangeActivity extends BaseActivity {

    private static final String TAG = "ExchangeActivity";

    private CoordinatorLayout mainContainer;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<ExchangeModel, ExchangeViewHolder> mAdapter;

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
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);

        FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            private final Fragment[] mFragments = new Fragment[] {
                    new OffersExchangesFragment(),
                    new AcceptedExchangesFragment(),
                    new RefusedExchangesFragment()
            };

            private final String[] mFragmentNames = new String[] {
                    getString(R.string.tab_offers),
                    getString(R.string.tab_accepted),
                    getString(R.string.tab_refused)
            };

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        viewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

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
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
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
