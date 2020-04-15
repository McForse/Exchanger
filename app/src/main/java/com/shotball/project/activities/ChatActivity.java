package com.shotball.project.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.shotball.project.R;
import com.shotball.project.fragments.ChatFragment;

public class ChatActivity extends AppCompatActivity {

    private ChatFragment chatFragment;
    private Toolbar toolbar;
    private TextView roomTitleTextView;
    private ImageView roomImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initToolbar();
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

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

}
