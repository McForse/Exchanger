package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;
import com.shotball.project.R;
import com.shotball.project.adapters.AdapterSnapGeneric;
import com.shotball.project.helpers.StartSnapHelper;
import com.shotball.project.models.Image;
import com.shotball.project.models.Product;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends BaseActivity implements OnMapReadyCallback, AdapterSnapGeneric.OnItemClickListener {

    private static final String TAG = "AddProductActivity";

    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    private Marker mMarker;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private NestedScrollView mainContainer;
    private List<Image> items;
    private AdapterSnapGeneric mAdapter;

    private TextInputLayout productTitleInputLayout;
    private TextInputLayout productDescriptionInputLayout;
    private EditText productTitle;
    private EditText productDescription;

    private AlertDialog mDialog;

    public static final byte MAX_IMAGES = 10;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        initToolbar();
        initComponent();
        initMapFragment();
    }

    private void initMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initToolbar() {
        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_product_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chevron_left);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initComponent() {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = findViewById(R.id.images_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mAdapter = new AdapterSnapGeneric(this, R.layout.item_snap_basic);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setOnFlingListener(null);
        recyclerView.setAdapter(mAdapter);
        new StartSnapHelper().attachToRecyclerView(recyclerView);
        mAdapter.addButton();

        productTitleInputLayout = findViewById(R.id.product_title_input_layout);
        productDescriptionInputLayout = findViewById(R.id.product_description_input_layout);
        productTitle = findViewById(R.id.product_title);
        productDescription = findViewById(R.id.product_description);

        mDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_product_publication)
                .setMessage(R.string.wait)
                .setCancelable(false)
                .create();

        mainContainer = findViewById(R.id.scroll_view);
        ImageView transparentImageView = findViewById(R.id.transparent_image);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        mainContainer.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP:
                        mainContainer.requestDisallowInterceptTouchEvent(false);
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_product, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_done) {
            onDoneClicked();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onDoneClicked() {
        if (!validateForm()) {
            return;
        }

        mDialog.show();
        writeNewProduct();
    }

    private boolean validateForm() {
        boolean valid = true;

        String title = productTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            productTitleInputLayout.setError(getString(R.string.required));
            productTitleInputLayout.requestFocus();
            valid = false;
        } else {
            productTitleInputLayout.setError(null);
        }

        String description = productDescription.getText().toString();
        if (TextUtils.isEmpty(description)) {
            productDescriptionInputLayout.setError(getString(R.string.required));
            productDescriptionInputLayout.requestFocus();
            valid = false;
        } else {
            productDescriptionInputLayout.setError(null);
        }

        if (mLastKnownLocation == null) {
            Snackbar.make(mainContainer, R.string.no_location_selected, Snackbar.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void writeNewProduct() {
        String key = mDatabase.child("products").push().getKey();

        if (key == null) {
            Snackbar.make(mainContainer, R.string.error_publish_product, Snackbar.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> images = new ArrayList<>();
        images.add("https://lh3.googleusercontent.com/FDlLqxdGjraCR8J0QzcuCh7eRHvnXNyWwP9mOTMviY2IX4WglCbAobxBD8tfYvUk-gZeOwvv02IC5vhbgEAk7F7gV2XwLJpAaw");

        GeoHash geoHash = new GeoHash(new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
        ArrayList<Double> location = new ArrayList<>();
        location.add(mLastKnownLocation.getLatitude());
        location.add( mLastKnownLocation.getLongitude());

        final Product product = new Product(productTitle.getText().toString(), images, productDescription.getText().toString(), getUid());
        product.setKey(key);
        product.g = geoHash.getGeoHashString();
        product.l = location;

        mDatabase.child("products").child(key).setValue(product.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Product was successfully recorded in the database!");
                mDialog.dismiss();
                new MaterialAlertDialogBuilder(AddProductActivity.this)
                        .setTitle(R.string.dialog_success)
                        .setMessage(R.string.dialog_publication_success)
                        //.setIcon(R.drawable.ic_done)
                        .setCancelable(false)
                        .setPositiveButton(R.string.go_to_product, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AddProductActivity.this, ProductActivity.class);
                                intent.putExtra(ProductActivity.EXTRA_PRODUCT_KEY, product.getKey());
                                startActivity(intent);
                                finish();
                            }
                        })
                        .show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Snackbar.make(mainContainer, R.string.error_publish_product, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void pickCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            getDeviceLocation();
        } else {
            Log.i(TAG, "The user did not grant location permission.");
            getLocationPermission();
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.d(TAG, "Latitude: " + location.getLatitude());
                            Log.d(TAG, "Longitude: " + location.getLongitude());
                            onLocationChanged(location);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    public void onLocationChanged(Location location) {
        if (mMarker != null) {
            mMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        mMarker = mMap.addMarker(markerOptions);
        if (mLastKnownLocation == null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        else mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mLastKnownLocation = location;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        getLocationPermission();
        pickCurrentPlace();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Location location = new Location("Selected Position");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                onLocationChanged(location);
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                getDeviceLocation();
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Define.ALBUM_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                items = new ArrayList<>();
                ArrayList<Uri> path = data.getParcelableArrayListExtra(Define.INTENT_PATH);

                if (path != null) {
                    for (Uri image : path) {
                        items.add(new Image(image));
                    }

                    mAdapter.insertData(items);
                }
            }
        }
    }

    @Override
    public void onProductImageClick(final Image image, final int position) {
        String[] options = { "Remove", "Make the main" };
        new MaterialAlertDialogBuilder(this)
                .setTitle("Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mAdapter.removeAt(position);
                        } else if (which == 1 && position != 0) {
                            Image temp = mAdapter.getItem(0);
                            mAdapter.replaceItem(image, 0);
                            mAdapter.replaceItem(temp, position);
                        }
                    }
                })
                .setPositiveButton("Cancel", null)
                .show();
    }

    @Override
    public void onAddButtonClick() {
        FishBun.with(AddProductActivity.this)
                .setImageAdapter(new GlideAdapter())
                .setMaxCount(MAX_IMAGES - mAdapter.getItemCount() + 1)
                .setPickerSpanCount(4)
                .setActionBarColor(Color.parseColor("#ffffff"), Color.parseColor("#ffffff"), true)
                .setActionBarTitleColor(Color.parseColor("#000000"))
                .setAlbumSpanCount(1, 2)
                .setButtonInAlbumActivity(true)
                .setCamera(false)
                .setReachLimitAutomaticClose(false)
                .setHomeAsUpIndicatorDrawable(ContextCompat.getDrawable(AddProductActivity.this, R.drawable.ic_chevron_left))
                .setDoneButtonDrawable(ContextCompat.getDrawable(AddProductActivity.this, R.drawable.ic_done))
                .setAllViewTitle("All of your photos")
                .setActionBarTitle("Selected photos")
                .textOnImagesSelectionLimitReached("You can't select any more.")
                .textOnNothingSelected("I need a photo!")
                .setCamera(true)
                .startAlbum();
    }
}