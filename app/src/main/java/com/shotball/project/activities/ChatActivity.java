package com.shotball.project.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shotball.project.R;
import com.shotball.project.fragments.ChatFragment;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private ChatFragment chatFragment;
    private Toolbar toolbar;
    private TextView roomTitleTextView;
    private ImageView roomImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initToolbar();
        initComponents();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponents() {
        roomTitleTextView = findViewById(R.id.room_title);
        roomImageView = findViewById(R.id.room_image);

        String toUid = getIntent().getStringExtra("toUid");
        final String roomID = getIntent().getStringExtra("roomID");
        String roomTitle = getIntent().getStringExtra("roomTitle");
        String roomImage = getIntent().getStringExtra("roomImage");
        String productKey = getIntent().getStringExtra("productKey");
        String productTitle = getIntent().getStringExtra("productTitle");

        if (roomTitle != null) {
            roomTitleTextView.setText(roomTitle);
        }

        if (roomImage != null && !roomImage.equals("")) {
            Glide.with(this)
                    .load(roomImage)
                    .into(roomImageView);
        } else {
            Glide.with(this).load(R.drawable.image_user)
                    .into(roomImageView);
        }

        chatFragment = ChatFragment.getInstance(toUid, roomID, productKey, productTitle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainFragment, chatFragment )
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }
}
