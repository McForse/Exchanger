package com.shotball.project.activities;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.shotball.project.R;
import com.shotball.project.fragments.AcceptedExchangesFragment;
import com.shotball.project.fragments.OffersExchangesFragment;
import com.shotball.project.fragments.RefusedExchangesFragment;

public class ExchangeActivity extends BaseActivity {

    private static final String TAG = "ExchangeActivity";

    private TabLayout tabLayout;
    private ViewPager viewPager;

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
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(1);

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
