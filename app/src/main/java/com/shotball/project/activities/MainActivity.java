package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.shotball.project.R;
import com.shotball.project.adapters.FragmentViewPagerAdapter;
import com.shotball.project.utils.Preferences;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private ViewPager2 viewPager;
    private int current_page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthState();
        setContentView(R.layout.activity_main);
        initComponents();
        initGeolocation();
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

    private void initGeolocation() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Preferences.saveLocation(MainActivity.this, location);
                }
            }
        });
    }

    private void checkAuthState() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void getInstanceToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();
                        Log.d(TAG, "FCM token: " + token);
                        FirebaseDatabase.getInstance().getReference().child("users").child(getUid()).child("fcm").setValue(token);
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("common");
    }

    @Override
    protected void onStart() {
        super.onStart();
        getInstanceToken();
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
