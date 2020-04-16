package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.HashMap;
import java.util.Map;

public class ProductActivity extends AppCompatActivity {

    private static final String TAG = "ProductActivity";
    public static final String EXTRA_PRODUCT_KEY = "product_key";
    private String PRODUCT_KEY;

    private Toolbar toolbar;

    private ImageView image;
    private TextView title;
    private TextView description;
    private ImageView sellerImageView;
    private TextView sellerNameField;
    private Button sendMessageButton;
    private Button exchangeButton;

    private View.OnClickListener sendMessageOnClickListener;
    private View.OnClickListener exchangeOnClickListener;

    private DatabaseReference mDatabase;
    private Product mProduct;
    private User mSeller;
    private DatabaseReference refProducts;
    private DatabaseReference refUsers;
    private ValueEventListener dataListener;

    private ViewPager viewPager;
    private LinearLayout layout_dots;
    private AdapterImageSlider adapterImageSlider;

    private String roomId;

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

    @SuppressLint("ClickableViewAccessibility")
    private void initComponents() {
        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        sellerImageView = findViewById(R.id.seller_image);
        sellerNameField = findViewById(R.id.seller_name);
        sendMessageButton = findViewById(R.id.send_message_button);
        exchangeButton = findViewById(R.id.exchange_button);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        refProducts = mDatabase.child("products");
        refUsers = mDatabase.child("users");

        getProduct();

        layout_dots = findViewById(R.id.layout_dots);
        viewPager = findViewById(R.id.pager);
        adapterImageSlider = new AdapterImageSlider(this, new ArrayList<String>(), PRODUCT_KEY);

        final NestedScrollView mainScrollView = findViewById(R.id.scroll_view);
        ImageView transparentImageView = findViewById(R.id.transparent_image);

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:

                    case MotionEvent.ACTION_MOVE:
                        // Disallow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mainScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    default:
                        return true;
                }
            }
        });
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable drawable = toolbar.getOverflowIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), getColor(R.color.red_400));
            toolbar.setOverflowIcon(drawable);
            toolbar.getOverflowIcon().setColorFilter(new PorterDuffColorFilter(getColor(R.color.red_400), PorterDuff.Mode.SRC_ATOP));
        }
    }

    private void getProduct() {
        dataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                mProduct = dataSnapshot.getValue(Product.class);
                mProduct.setKey(dataSnapshot.getKey());
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
                mSeller.setUid(dataSnapshot.getKey());
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
            initMapFragment();
            invalidateOptionsMenu();
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
            findChatRoom(mSeller.getUid());

            sendMessageOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (roomId == null || roomId.equals("")) {
                        Map<String, String> selectedUsers = new HashMap<>();
                        selectedUsers.put(getUid(), "i");
                        selectedUsers.put(mSeller.getUid(), "i");
                        final String room_id = mDatabase.child("rooms").push().getKey();

                        mDatabase.child("rooms/" + room_id).child("users").setValue(selectedUsers).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(ProductActivity.this, ChatActivity.class);
                                intent.putExtra("toUid", mSeller.getUid());
                                intent.putExtra("roomID", room_id);
                                intent.putExtra("roomTitle", mSeller.getUsername());
                                intent.putExtra("roomImage", mSeller.getImage());
                                intent.putExtra("productKey", mProduct.getKey());
                                intent.putExtra("productTitle", mProduct.getTitle());
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        Intent intent = new Intent(ProductActivity.this, ChatActivity.class);
                        intent.putExtra("toUid", mSeller.getUid());
                        intent.putExtra("roomID", roomId);
                        intent.putExtra("roomTitle", mSeller.getUsername());
                        intent.putExtra("roomImage", mSeller.getImage());
                        intent.putExtra("productKey", mProduct.getKey());
                        intent.putExtra("productTitle", mProduct.getTitle());
                        startActivity(intent);
                        finish();
                    }
                }
            };
            sendMessageButton.setOnClickListener(sendMessageOnClickListener);
        } else {
            //TODO: error
        }
    }

    private void findChatRoom(final String toUid) {
        mDatabase.child("rooms").orderByChild("users/" + getUid()).equalTo("i").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    Map<String, String> users = (Map<String, String>) item.child("users").getValue();

                    if (users.size() == 2 & users.get(toUid) != null) {
                        roomId = item.getKey();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled findChatRoom: " + databaseError.getMessage());
            }
        });
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
                dots[i].setImageResource(R.drawable.shape_circle_white);
                dots[i].setColorFilter(ContextCompat.getColor(this, R.color.overlay_dark_10), PorterDuff.Mode.SRC_ATOP);
                layout_dots.addView(dots[i]);
            }

            dots[current].setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void initMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setScrollGesturesEnabled(true);
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mProduct.getLatitude(), mProduct.getLongitude()));
                googleMap.addMarker(markerOptions);
                googleMap.moveCamera(zoomingLocation());
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        try {
                            googleMap.animateCamera(zoomingLocation());
                        } catch (Exception e) {
                        }
                        return true;
                    }
                });
            }
        });
    }

    private CameraUpdate zoomingLocation() {
        return CameraUpdateFactory.newLatLngZoom(new LatLng(mProduct.getLatitude(), mProduct.getLongitude()), 15);
    }

    public String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
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

        if (mProduct != null) {
            if (mProduct.likes.containsKey(getUid()));
            MenuItem likeItem = menu.findItem(R.id.action_like);
            likeItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite));
        }

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
