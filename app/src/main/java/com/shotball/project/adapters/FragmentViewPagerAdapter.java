package com.shotball.project.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.transition.MaterialFadeThrough;
import com.shotball.project.R;
import com.shotball.project.fragments.AccountFragment;
import com.shotball.project.fragments.FavoritesFragment;
import com.shotball.project.fragments.HomeFragment;
import com.shotball.project.fragments.MessagesFragment;

public class FragmentViewPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 4;

    public FragmentViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new FavoritesFragment();
            case 2:
                return new MessagesFragment();
            case 3:
                return new AccountFragment();
        }

        return new HomeFragment();
    }

}
