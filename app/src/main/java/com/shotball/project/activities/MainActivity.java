package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.shotball.project.R;
import com.shotball.project.ViewPagers.BottomNavigationViewPager;
import com.shotball.project.adapters.FragmentViewPagerAdapter;
import com.shotball.project.fragments.AccountFragment;
import com.shotball.project.fragments.FavoritesFragment;
import com.shotball.project.fragments.HomeFragment;
import com.shotball.project.fragments.MessagesFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomNavigationViewPager viewPager;
    private FragmentViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthState();
        setContentView(R.layout.activity_main);

        initToolbar();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        viewPager = findViewById(R.id.view_pager);
        adapter = new FragmentViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), "HomeFragment");
        adapter.addFragment(new FavoritesFragment(), "FavoritesFragment");
        adapter.addFragment(new MessagesFragment(), "MessagesFragment");
        adapter.addFragment(new AccountFragment(), "AccountFragment");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            viewPager.setCurrentItem(0);
                            break;
                        case R.id.nav_favorites:
                            viewPager.setCurrentItem(1);
                            break;
                        case R.id.nav_messages:
                            viewPager.setCurrentItem(2);
                            break;
                        case R.id.nav_account:
                            viewPager.setCurrentItem(3);
                            break;
                    }

                    return true;
                }
            };

    private void checkAuthState() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        } else if (i == R.id.action_filter) {
            //TODO
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


}
