package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.shotball.project.R;
import com.shotball.project.adapters.FragmentViewPagerAdapter;
import com.shotball.project.fragments.AccountFragment;
import com.shotball.project.fragments.FavoritesFragment;
import com.shotball.project.fragments.HomeFragment;
import com.shotball.project.fragments.MessagesFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewPager2 viewPager;
    private FragmentViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthState();
        setContentView(R.layout.activity_main);

        initComponents();
    }

    private void initComponents() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        viewPager = findViewById(R.id.view_pager);
        adapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new FavoritesFragment());
        adapter.addFragment(new MessagesFragment());
        adapter.addFragment(new AccountFragment());
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0, false);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            viewPager.setCurrentItem(0, false);
                            break;
                        case R.id.nav_favorites:
                            viewPager.setCurrentItem(1, false);
                            break;
                        case R.id.nav_messages:
                            viewPager.setCurrentItem(2, false);
                            break;
                        case R.id.nav_account:
                            viewPager.setCurrentItem(3, false);
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

}
