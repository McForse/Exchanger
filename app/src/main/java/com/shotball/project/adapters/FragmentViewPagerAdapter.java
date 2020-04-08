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

    private Context ctx;

    public FragmentViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Context context) {
        super(fragmentManager, lifecycle);
        ctx = context;
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

    private MaterialFadeThrough createTransition() {
        MaterialFadeThrough fadeThrough = MaterialFadeThrough.create(ctx);

        // Add targets for this transition to explicitly run transitions only on these views. Without
        // targeting, a MaterialFadeThrough would be run for every view in the Fragment's layout.
        fadeThrough.addTarget(R.id.home_fragment);
        fadeThrough.addTarget(R.id.favourites_fragment);
        fadeThrough.addTarget(R.id.messages_fragment);
        fadeThrough.addTarget(R.id.account_fragment);

        return fadeThrough;
    }
}
