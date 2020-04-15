package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.shotball.project.R;
import com.shotball.project.adapters.FragmentViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ViewPager2 viewPager;

    public static Location location;

    private int current_page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthState();
        setContentView(R.layout.activity_main);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            setLocation(location);
                        }
                    }
                });
    }

    private void setLocation(Location location) {
        MainActivity.location = location;
        initComponents();
    }

    private void initComponents() {
        viewPager = findViewById(R.id.view_pager);
        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), this);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(adapter);
        viewPager.setClipToPadding(false);
        viewPager.setCurrentItem(current_page, false);

        new BottomNavigationHandler(this, viewPager);
    }

    private void checkAuthState() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: " + current_page);
        viewPager.setCurrentItem(current_page - 1, false);
        viewPager.setCurrentItem(current_page, false);
    }

    class BottomNavigationHandler implements BottomNavigationView.OnNavigationItemSelectedListener {

        private ViewPager2 viewPager;

        BottomNavigationHandler(Activity activity, ViewPager2 viewPager) {
            this.viewPager = viewPager;
            BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
            bottomNav.setOnNavigationItemSelectedListener(this);
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    current_page = 0;
                    break;
                case R.id.nav_favorites:
                    current_page = 1;
                    break;
                case R.id.nav_messages:
                    current_page = 2;
                    break;
                case R.id.nav_account:
                    current_page = 3;
                    break;
            }

            viewPager.setCurrentItem(current_page, false);
            return true;
        }
    }

}
