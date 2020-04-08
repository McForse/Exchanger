package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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

    private FusedLocationProviderClient fusedLocationClient;

    public static Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthState();
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), this);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0, false);

        new BottomNavigationHandler(this, viewPager);
    }

    private void checkAuthState() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

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
}
