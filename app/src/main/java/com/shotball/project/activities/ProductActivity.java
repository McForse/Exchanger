package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.models.Product;
import com.shotball.project.models.User;

public class ProductActivity extends AppCompatActivity {

    private static final String TAG = "ProductActivity";
    public static final String EXTRA_PRODUCT_KEY = "product_key";
    private String PRODUCT_KEY;

    private ImageView image;
    private TextView title;
    private TextView description;
    private ImageView sellerImageView;
    private TextView sellerNameField;

    private DatabaseReference mDatabase;
    private Product mProduct;
    private User mSeller;
    private DatabaseReference refProducts;
    private DatabaseReference refLocations;
    private DatabaseReference refUsers;
    private ValueEventListener dataListener;

    private SupportMapFragment googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        initToolbar();

        Bundle bundle = getIntent().getExtras();

        if (bundle == null || bundle.getString(EXTRA_PRODUCT_KEY) == null) {
            //TODO: error
        } else {
            PRODUCT_KEY = bundle.getString(EXTRA_PRODUCT_KEY);
            initComponents();
        }
    }

    private void initComponents() {
        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        sellerImageView = findViewById(R.id.seller_image);
        sellerNameField = findViewById(R.id.seller_name);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        refProducts = mDatabase.child("products");
        refLocations = mDatabase.child("locations");
        refUsers = mDatabase.child("users");

        getProduct();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getProduct() {
        dataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                mProduct = dataSnapshot.getValue(Product.class);
                initProduct();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError);
            }
        };

        refProducts.child(PRODUCT_KEY).addListenerForSingleValueEvent(dataListener);
    }

    private void getSeller() {
        dataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                mSeller = dataSnapshot.getValue(User.class);
                initSeller();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError);
            }
        };

        refUsers.child(mProduct.userId).addListenerForSingleValueEvent(dataListener);
    }

    private void initProduct() {
        stopDataListener();

        if (mProduct != null) {
            //TODO: placeholder and error
            Glide.with(this).load(mProduct.image).into(image);
            title.setText(mProduct.title);
            description.setText(mProduct.description);

            getSeller();
        } else {
            //TODO: error
        }
    }

    private void initSeller() {
        stopDataListener();

        if (mSeller != null) {
            //TODO: placeholder and error
            Glide.with(this)
                    .load(mSeller.image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(sellerImageView);
            sellerNameField.setText(mSeller.username);
        } else {
            //TODO: error
        }
    }

    private void stopDataListener() {
        mDatabase.removeEventListener(dataListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDataListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_like) {

        }

        return super.onOptionsItemSelected(item);
    }
}
