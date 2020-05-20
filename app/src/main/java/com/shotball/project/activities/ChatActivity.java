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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initToolbar();
        initComponents();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponents() {
        TextView roomTitleTextView = findViewById(R.id.room_title);
        ImageView roomImageView = findViewById(R.id.room_image);

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
            Glide.with(this).load(R.drawable.img_user)
                    .into(roomImageView);
        }

        ChatFragment chatFragment = ChatFragment.getInstance(toUid, roomID, productKey, productTitle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_fragment, chatFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }
}
