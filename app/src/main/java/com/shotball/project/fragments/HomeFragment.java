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
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.shotball.project.R;
import com.shotball.project.activities.AddProductActivity;
import com.shotball.project.activities.FilterActivity;
import com.shotball.project.activities.MainActivity;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.activities.SignInActivity;
import com.shotball.project.adapters.ProductAdapter;
import com.shotball.project.models.Filters;
import com.shotball.project.models.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductSelectedListener {

    private static final String TAG = "HomeFragment";
    private static final String TAG_GEO = "GeoListener";

    private View rootView;
    private Activity mActivity;

    private DatabaseReference mDatabase;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private GeoQueryDataEventListener geoQueryListener;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private StaggeredGridLayoutManager gridLayoutManager;
    private ProductAdapter mAdapter;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState;

    private static Filters mFilters;
    private static boolean filtersUpdated;

    private static boolean loading = false;
    private static final int item_per_display = 8;
    private final List<Product> productsList = new ArrayList<>();
    private HashSet<String> productsKeys;
    private int counter = 0;

    private static final double[] MY_LOCATION = { 55.936354, 37.494034 };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mActivity = getActivity();
        Log.d(TAG, "onCreateView");

        initToolbar();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        progressBar = rootView.findViewById(R.id.progress_bar);
        recyclerView = rootView.findViewById(R.id.product_grid);
        swipeContainer = rootView.findViewById(R.id.swipe_container);
        mAdapter = new ProductAdapter(rootView.getContext(), item_per_display);
        mFilters = Filters.getDefault();
        productsKeys = new HashSet<>();
        setReferences();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (rootView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        }

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetRecycleView();
                startSearch();
            }
        });

        mAdapter.setOnProductSelectedListener(this);
        mAdapter.setOnLoadMoreListener(new ProductAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                loadNextData();
            }
        });

        //recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        startSearch();
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) rootView.getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    private void setReferences() {
        geoFire = new GeoFire(mDatabase.child("products"));

        geoQueryListener = new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                if (counter < item_per_display) {
                    final Product product = dataSnapshot.getValue(Product.class);

                    if (product != null && product.available) {
                        String key = dataSnapshot.getKey();
                        if (!productsKeys.contains(key)) {
                            Log.d(TAG_GEO, "onDataEntered: " + dataSnapshot.toString());
                            productsKeys.add(key);
                            product.setKey(key);

                            Location locationA = new Location("point A");

                            locationA.setLatitude(MY_LOCATION[0]);
                            locationA.setLongitude(MY_LOCATION[1]);
                            Location locationB = new Location("point B");
                            locationB.setLatitude(MainActivity.location.getLatitude());
                            locationB.setLongitude(MainActivity.location.getLongitude());
                            int distance = (int) locationA.distanceTo(locationB);

                            product.setDistance(distance);
                            productsList.add(product);
                            counter++;
                        }
                    }
                } else {
                    stopGeoQueryListener();
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
                stopGeoQueryListener();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG_GEO, "onGeoQueryError: " + error);
            }
        };
    }

    private void loadNextData() {
        Log.d(TAG, "loadNextData");
        mAdapter.setLoading();
        counter = 0;
        startSearch();
    }

    private void startSearch() {
        //Log.d(TAG, "startSearch: " + MainActivity.location.getLatitude() + " " + MainActivity.location.getLongitude());
        loading = true;
        //searchNearby(location.getLatitude(), location.getLongitude(), (double) mFilters.getDistance() / 1000);
        searchNearby(MY_LOCATION[0], MY_LOCATION[1], (double) mFilters.getDistance() / 1000);
    }

    private void searchNearby(double latitude, double longitude, double radius) {
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), radius);
        geoQuery.addGeoQueryDataEventListener(geoQueryListener);
    }

    private void stopGeoQueryListener() {
        if (loading) {
            loading = false;
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "stopGeoQueryListener: " + productsList.size());
            geoQuery.removeAllListeners();
            swipeContainer.setRefreshing(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.insertData(productsList);
                    productsList.clear();
                    counter = 0;
                }
            }, 1500);
        }
    }

    private void resetRecycleView() {
        Log.d(TAG, "resetRecycleView");
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        counter = 0;
        productsKeys.clear();
        filtersUpdated = false;
        mAdapter.clear();
        loading = false;
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
                Log.d(TAG, "productTransaction:onComplete: " + databaseError);
            }
        });
    }

    private static FilterActivity.FilterListener mFilterListener = new FilterActivity.FilterListener() {
        @Override
        public void onFilter(Filters filters) {
            Log.d(TAG, "onFilter: " + filters.toString());

            if (!mFilters.equals(filters)) {
                mFilters = filters;
                filtersUpdated = true;
            }
        }

        @Override
        public Filters getCurrentFilters() {
            return mFilters;
        }
    };

    private String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
        }
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

        if (filtersUpdated) {
            resetRecycleView();
            startSearch();
        }
    }

    private void setLocationToProduct(String key, double latitude, double longitude) {
        GeoHash geoHash = new GeoHash(new GeoLocation(latitude, longitude));
        List<Double> location = new ArrayList<>();
        location.add(latitude);
        location.add(longitude);
        DatabaseReference databaseReference = mDatabase.child("products").child(key);

        databaseReference.child("g").setValue(geoHash.getGeoHashString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "newLocation writed");
            }
        });
        databaseReference.child("l").setValue(location);
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
            Intent intent = new Intent(mActivity, FilterActivity.class);
            intent.putExtra("interface", mFilterListener);
            startActivity(intent);
            return true;
        } else if  (i == R.id.action_add) {
            Intent intent = new Intent(mActivity, AddProductActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
