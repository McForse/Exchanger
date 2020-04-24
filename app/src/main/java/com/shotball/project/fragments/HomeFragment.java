package com.shotball.project.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
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
import com.shotball.project.activities.AddProductActivity;
import com.shotball.project.activities.FilterActivity;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.activities.SignInActivity;
import com.shotball.project.adapters.ProductAdapter;
import com.shotball.project.models.Filters;
import com.shotball.project.models.Product;
import com.shotball.project.utils.ViewAnimation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductSelectedListener {

    private static final String TAG = "HomeFragment";
    private static final String TAG_GEO = "GeoListener";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private static final int GPS_REQUEST = 1;

    private View rootView;
    private Activity mActivity;
    private RelativeLayout mainContainer;
    private NestedScrollView geolocationContainer;
    private NestedScrollView noItemPage;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private StaggeredGridLayoutManager gridLayoutManager;
    private ProductAdapter mAdapter;

    private DatabaseReference mDatabase;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private GeoQueryDataEventListener geoQueryListener;

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
    public static Location myLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        initComponents();
        checkPermissionsAndGps();
        initToolbar();
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

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
    }

    private void initComponents() {
        mActivity = getActivity();
        mainContainer = rootView.findViewById(R.id.home_main_container);
        geolocationContainer = rootView.findViewById(R.id.lyt_no_permission_location);
        noItemPage = rootView.findViewById(R.id.lyt_no_items);
        mainContainer.setVisibility(View.GONE);
        geolocationContainer.setVisibility(View.GONE);
        noItemPage.setVisibility(View.GONE);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        progressBar = rootView.findViewById(R.id.home_progress_bar);
        recyclerView = rootView.findViewById(R.id.product_grid);
        swipeContainer = rootView.findViewById(R.id.swipe_container);
        mAdapter = new ProductAdapter(rootView.getContext(), item_per_display);
        mFilters = Filters.getDefault();
        productsKeys = new HashSet<>();
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) rootView.getContext()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
    }

    private boolean checkPermissionsAndGps() {
        if (!mLocationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(mActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                getLocationPermission();
            } else {
                mLocationPermissionGranted = true;
            }
        }

        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.w(TAG, "checkPermissionsAndGps: " + e.getMessage());
            return false;
        }

        if (!mLocationPermissionGranted || !gpsEnabled) {
            mainContainer.setVisibility(View.GONE);
            geolocationContainer.setVisibility(View.VISIBLE);
            Button allowLocationButton = rootView.findViewById(R.id.button_allow_location);
            allowLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mLocationPermissionGranted) {
                        getLocationPermission();
                    } else {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_REQUEST);
                    }
                }
            });
        } else {
            geolocationContainer.setVisibility(View.GONE);
            myLocation = new Location("1");
            myLocation.setLatitude(MY_LOCATION[0]);
            myLocation.setLongitude(MY_LOCATION[1]);
            setMyLocation(myLocation);
            mainContainer.setVisibility(View.VISIBLE);
            setReferences();
            return true;
            /*FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(mActivity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.d(TAG, "CRCERCECERcre");
                                setMyLocation(location);
                                mainContainer.setVisibility(View.VISIBLE);
                                setReferences();
                            }
                        }
                    });*/
        }

        return false;
    }

    private void setReferences() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stopGeoQueryListener();
                resetRecycleView();
                if (checkPermissionsAndGps()) {
                    startSearch();
                }
            }
        });

        mAdapter.setOnProductSelectedListener(this);
        mAdapter.setOnLoadMoreListener(new ProductAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                loadNextData();
            }
        });

        geoFire = new GeoFire(mDatabase.child("products"));
        geoQueryListener = new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                if (counter < item_per_display) {
                    final Product product = dataSnapshot.getValue(Product.class);

                    if (product != null) {
                        String key = dataSnapshot.getKey();
                        if (!productsKeys.contains(key) && product.available && mFilters.hasCategory(product.getCategory())) {
                            Log.d(TAG_GEO, "onDataEntered: " + dataSnapshot.toString());
                            productsKeys.add(key);
                            product.setKey(key);

                            Location locationA = new Location("point A");
                            locationA.setLatitude(myLocation.getLatitude());
                            locationA.setLongitude(myLocation.getLongitude());
                            Location locationB = new Location("point B");
                            locationB.setLatitude(location.latitude);
                            locationB.setLongitude(location.longitude);
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

        startSearch();
    }

    private void setMyLocation(Location location) {
        myLocation = location;
    }

    private void loadNextData() {
        Log.d(TAG, "loadNextData");
        mAdapter.setLoading();
        counter = 0;
        startSearch();
    }

    private void startSearch() {
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
            if (productsList.isEmpty() && mAdapter.getItemCount() < item_per_display) {
                mAdapter.setOnLoadMoreListener(null);
                if (mAdapter.getItemCount() == 0) {
                    ViewAnimation.showIn(noItemPage);
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.insertData(productsList);
                    productsList.clear();
                    counter = 0;
                }}, 1500);
            swipeContainer.setRefreshing(false);
        }
    }

    private void resetRecycleView() {
        Log.d(TAG, "resetRecycleView");
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        noItemPage.setVisibility(View.GONE);
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

    private void getLocationPermission() {
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
            mAdapter.setOnLoadMoreListener(new ProductAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore(int current_page) {
                    loadNextData();
                }
            });
            startSearch();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_REQUEST) {
            checkPermissionsAndGps();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                checkPermissionsAndGps();
            }
        }
    }

}
