package com.shotball.project.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.Utils.ViewAnimation;
import com.shotball.project.activities.FilterActivity;
import com.shotball.project.activities.SignInActivity;
import com.shotball.project.adapters.ProductAdapter;
import com.shotball.project.models.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String TAG_GEO = "GeoListener";

    private ProductAdapter mAdapter;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;

    private View rootView;
    private List<Product> productList;

    private Activity mActivity;
    private DatabaseReference mDatabase;
    private DatabaseReference refLocation;
    private DatabaseReference refProducts;

    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private GeoQueryEventListener geoQueryEventListener;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState = null;

    private SwipeRefreshLayout swipeContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d(TAG, "onCreateView");

        mActivity = getActivity();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initToolbar();

        recyclerView = rootView.findViewById(R.id.product_grid);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LinearLayout lyt_progress = mActivity.findViewById(R.id.lyt_progress);

        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(mActivity, 2);
        } else {
            gridLayoutManager = new GridLayoutManager(mActivity, 3);
        }

        swipeContainer = mActivity.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.clear();
                searchNearby(55.93633, 37.494045, 1.0);
            }
        });

        recyclerView.setLayoutManager(gridLayoutManager);

        setReferences();
        productList = new ArrayList<>();

        searchNearby(55.93633, 37.494045, 1.0);
        mAdapter = new ProductAdapter(mActivity, productList);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        mBundleRecyclerViewState = new Bundle();
        mListState = recyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, mListState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mListState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                    recyclerView.getLayoutManager().onRestoreInstanceState(mListState);

                }
            }, 50);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");

        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mListState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                    recyclerView.getLayoutManager().onRestoreInstanceState(mListState);

                }
            }, 50);
        }

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager.setSpanCount(2);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            gridLayoutManager.setSpanCount(3);
        }

        recyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        mListState = recyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, mListState);
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) mActivity).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    private void setReferences() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        refProducts = mDatabase.child("products");
        refLocation = mDatabase.child("locations");
        geoFire = new GeoFire(refLocation);

        geoQueryEventListener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                String loc = String.valueOf(location.latitude) + ", " + String.valueOf(location.longitude);
                Log.d(TAG_GEO, "onKeyEntered: " + key + " @ " + loc);

                refProducts.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG_GEO, "onDataChange: " + dataSnapshot.toString());
                        Product product = dataSnapshot.getValue(Product.class);
                        if (product.available) {
                            product.setKey(dataSnapshot.getKey());
                            //product.setGeo((Double) dataSnapshot.child("geo/l/0").getValue(), (Double) dataSnapshot.child("geo/l/1").getValue());
                            productList.add(product);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError firebaseError) {
                        Log.e(TAG_GEO, "onCancelled: " + firebaseError.getMessage());
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG_GEO, "onKeyExited: " + key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(TAG_GEO, "onKeyMoved: " + key);
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG_GEO, "onGeoQueryReady");
                Log.d(TAG_GEO, "Products list size is " + productList.size());
                stopGeoQueryListener();
                Log.d(TAG_GEO, "geoQueryEventListener stopped");
                displayContent();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG_GEO, "onGeoQueryError: " + error.getMessage());
            }
        };
    }

    private void searchNearby(double latitude, double longitude, double radius) {
        this.searchNearby(new GeoLocation(latitude, longitude), radius);
    }

    private void searchNearby(GeoLocation location, double radius) {
        geoQuery = geoFire.queryAtLocation(location, radius);
        geoQuery.addGeoQueryEventListener(geoQueryEventListener);
    }

    private void stopGeoQueryListener() {
        geoQuery.removeGeoQueryEventListener(geoQueryEventListener);
        swipeContainer.setRefreshing(false);
    }

    private void setLocationToProduct(String key) {
        GeoFire geoFire = new GeoFire(refLocation);

        geoFire.setLocation(key, new GeoLocation(65.936330, 37.494045), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.e(TAG_GEO, "setLocationToProduct: " + error);
            }
        });
    }

    private void displayContent() {
        final LinearLayout lyt_progress = mActivity.findViewById(R.id.lyt_progress);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewAnimation.fadeOut(lyt_progress);
            }
        }, 100);

        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int i = item.getItemId();

        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(mActivity, SignInActivity.class));
            mActivity.finish();
            return true;
        } else if (i == R.id.action_filter) {
            startActivity(new Intent(mActivity, FilterActivity.class));
            return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

}
