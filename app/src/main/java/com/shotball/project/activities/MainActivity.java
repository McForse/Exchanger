package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.shotball.project.R;
import com.shotball.project.adapters.FragmentViewPagerAdapter;
import com.shotball.project.fragments.AccountFragment;
import com.shotball.project.fragments.FavoritesFragment;
import com.shotball.project.fragments.HomeFragment;
import com.shotball.project.fragments.MessagesFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        getWindow().setSharedElementsUseOverlay(false);
        super.onCreate(savedInstanceState);
        checkAuthState();
        setContentView(R.layout.activity_main);

        initComponents();
    }

    private void initComponents() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new FavoritesFragment());
        adapter.addFragment(new MessagesFragment());
        adapter.addFragment(new AccountFragment());
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setSaveFromParentEnabled(false);
        viewPager.setOffscreenPageLimit(adapter.getItemCount() - 1);
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
