package com.shotball.project.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
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
import android.widget.Filter;
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
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.shotball.project.R;
import com.shotball.project.Utils.ViewAnimation;
import com.shotball.project.activities.FilterActivity;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.activities.SignInActivity;
import com.shotball.project.adapters.ProductAdapter;
import com.shotball.project.listeners.EndlessRecyclerViewScrollListener;
import com.shotball.project.models.Filters;
import com.shotball.project.models.Product;

import java.util.Objects;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductSelectedListener, FilterActivity.FilterListener {

    private static final String TAG = "HomeFragment";
    private static final String TAG_GEO = "GeoListener";

    private View rootView;
    private Activity mActivity;

    private DatabaseReference mDatabase;
    private DatabaseReference refLocation;
    private DatabaseReference refProducts;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private GeoQueryDataEventListener geoQueryListener;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private GridLayoutManager gridLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ProductAdapter mAdapter;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState;

    private static final int ITEMS_PER_PAGE = 5;

    private static final double[] MY_LOCATION = { 55.936354, 37.494034 };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mActivity = getActivity();
        Log.d(TAG, "onCreateView");

        initToolbar();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        recyclerView = rootView.findViewById(R.id.product_grid);
        swipeContainer = rootView.findViewById(R.id.swipe_container);
        mAdapter = new ProductAdapter(rootView.getContext(), this);
        setReferences();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (rootView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(rootView.getContext(), 2);
        } else {
            gridLayoutManager = new GridLayoutManager(rootView.getContext(), 3);
        }

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //reset();
                //startSearch();
            }
        });

        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d(TAG, "onLoadMore, page=" + page);
                //f (page > 1) loadData(page);
            }
        };

        //recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(scrollListener);

        startSearch();
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) rootView.getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    private void setReferences() {
        refProducts = mDatabase.child("products");
        refLocation = mDatabase.child("locations");
        geoFire = new GeoFire(refProducts);

        geoQueryListener = new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                Log.d(TAG_GEO, "onDataEntered: " + dataSnapshot.toString());
                final Product product = dataSnapshot.getValue(Product.class);

                if (product != null && product.available) {
                    product.setKey(dataSnapshot.getKey());

                    Location locationA = new Location("point A");

                    locationA.setLatitude(product.getLatitude());
                    locationA.setLongitude(product.getLongitude());
                    Location locationB = new Location("point B");
                    locationB.setLatitude(MY_LOCATION[0]);
                    locationB.setLongitude(MY_LOCATION[1]);
                    int distance = (int) locationA.distanceTo(locationB);

                    product.setDistance(distance);
                    mAdapter.add(product);
                }
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) { }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) { }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) { }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG_GEO, "onGeoQueryReady");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG_GEO, "onGeoQueryError: " + error);
            }
        };
    }

    private void startSearch() {
        searchNearby(MY_LOCATION[0], MY_LOCATION[1], 1.0);
    }

    private void loadData(int page) {
        //if (page < productsLocations.size()) getProduct(productsLocations.get(page), page);
    }

    private void searchNearby(double latitude, double longitude, double radius) {
        searchNearby(new GeoLocation(latitude, longitude), radius);
    }

    private void searchNearby(GeoLocation location, double radius) {
        geoQuery = geoFire.queryAtLocation(location, radius);
        geoQuery.addGeoQueryDataEventListener(geoQueryListener);
    }

    private void stopGeoQueryListener() {
        //geoQuery.removeAllListeners();
        swipeContainer.setRefreshing(false);
    }

    private void reset() {
        Log.d(TAG, "reset");
        mAdapter.clear();
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void noMoreProducts() {
        Log.d(TAG, "noMoreProducts");
        stopGeoQueryListener();
        recyclerView.removeOnScrollListener(scrollListener);
    }

    private void setLocationToProduct(String key, double latitude, double longitude) {
        GeoFire geoFire = new GeoFire(refLocation);

        geoFire.setLocation(key, new GeoLocation(latitude, longitude));
    }

    private void displayContent() {
        final LinearLayout lyt_progress = rootView.findViewById(R.id.lyt_progress);
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
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        mBundleRecyclerViewState = new Bundle();
        mListState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
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
                    Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(mListState);
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
                    Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(mListState);

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

        mListState = Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, mListState);
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

    @Override
    public void onProductSelected(Product product) {
        Intent intent = new Intent(mActivity, ProductActivity.class);
        intent.putExtra(ProductActivity.EXTRA_PRODUCT_KEY, product.getKey());
        mActivity.startActivity(intent);
    }

    @Override
    public void onLikeClicked(String productKey) {
        DatabaseReference reference = mDatabase.child("products").child(productKey);

        reference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Product product = mutableData.getValue(Product.class);
                if (product == null) {
                    return Transaction.success(mutableData);
                }

                if (product.likes.containsKey(getUid())) {
                    product.likeCount = product.likeCount - 1;
                    product.likes.remove(getUid());
                } else {
                    product.likeCount = product.likeCount + 1;
                    product.likes.put(getUid(), true);
                }

                mutableData.setValue(product);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "productTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void onFilter(Filters filters) {

    }

    private String getUid() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

}
