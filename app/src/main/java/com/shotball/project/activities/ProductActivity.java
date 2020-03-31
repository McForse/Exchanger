package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.shotball.project.adapters.AdapterImageSlider;
import com.shotball.project.models.Product;
import com.shotball.project.models.User;

import java.util.ArrayList;
import java.util.List;

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

    private ViewPager viewPager;
    private LinearLayout layout_dots;
    private AdapterImageSlider adapterImageSlider;

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

        layout_dots = (LinearLayout) findViewById(R.id.layout_dots);
        viewPager = (ViewPager) findViewById(R.id.pager);
        adapterImageSlider = new AdapterImageSlider(this, new ArrayList<String>());


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

        refUsers.child(mProduct.user).addListenerForSingleValueEvent(dataListener);
    }

    private void initProduct() {
        stopDataListener();

        if (mProduct != null) {
            title.setText(mProduct.title);
            description.setText(mProduct.description);

            adapterImageSlider.setItems(mProduct.images);
            viewPager.setAdapter(adapterImageSlider);

            // displaying selected image first
            viewPager.setCurrentItem(0);
            addBottomDots(layout_dots, adapterImageSlider.getCount(), 0);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int pos) {
                    addBottomDots(layout_dots, adapterImageSlider.getCount(), pos);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

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

    private void addBottomDots(LinearLayout layout_dots, int size, int current) {
        if (size > 1) {
            ImageView[] dots = new ImageView[size];

            layout_dots.removeAllViews();
            for (int i = 0; i < dots.length; i++) {
                dots[i] = new ImageView(this);
                int width_height = 15;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
                params.setMargins(10, 10, 10, 10);
                dots[i].setLayoutParams(params);
                dots[i].setImageResource(R.drawable.shape_circle);
                dots[i].setColorFilter(ContextCompat.getColor(this, R.color.overlay_dark_10), PorterDuff.Mode.SRC_ATOP);
                layout_dots.addView(dots[i]);
            }

            if (dots.length > 0) {
                dots[current].setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            }
        }
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
