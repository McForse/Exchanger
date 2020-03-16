package com.shotball.project.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.models.Product;

public class ProductActivity extends AppCompatActivity {

    private static final String TAG = "ProductActivity";
    private String PRODUCT_KEY;

    private ImageView image;
    private TextView title;
    private TextView description;

    private DatabaseReference mDatabase;
    private Product mProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        initToolbar();

        Bundle bundle = getIntent().getExtras();

        if (bundle == null || bundle.getString("PRODUCT_KEY") == null) {
            //TODO: error
        } else {
            PRODUCT_KEY = bundle.getString("PRODUCT_KEY");
            initComponents();
        }
    }

    private void initComponents() {
        image = findViewById(R.id.image);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mProduct = null;

        getProduct();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getProduct() {
        mDatabase.child("products").child(PRODUCT_KEY).addValueEventListener(new ValueEventListener() {
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
        });
    }

    private void initProduct() {
        if (mProduct != null) {
            //TODO: placeholder and error
            Glide.with(this).load(mProduct.image).into(image);
            title.setText(mProduct.title);
            description.setText(mProduct.description);
        } else {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
