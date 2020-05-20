package com.shotball.project.activities;

import androidx.annotation.NonNull;
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
import com.shotball.project.fragments.ChatFragment;
import com.shotball.project.interfaces.IsAvailableCallback;
import com.shotball.project.services.MyFirebaseMessagingService;

import java.util.Map;

public class ChatActivity extends BaseActivity {

    private static final String TAG = "ChatActivity";

    private String roomId;
    private String toUid;
    private String roomTitle;
    private String roomImage;
    private String productKey;
    private String productTitle;

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

        toUid = getIntent().getStringExtra("toUid");
        roomId = getIntent().getStringExtra("roomId");
        roomTitle = getIntent().getStringExtra("roomTitle");
        roomImage = getIntent().getStringExtra("roomImage");
        productKey = getIntent().getStringExtra("productKey");
        productTitle = getIntent().getStringExtra("productTitle");

        if (roomTitle != null) {
            roomTitleTextView.setText(roomTitle);
        }

        if (roomImage != null && !roomImage.equals("")) {
            Glide.with(this)
                    .load(roomImage)
                    .into(roomImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.img_user)
                    .into(roomImageView);
        }

        if (roomId == null) {
            findChatRoom(toUid, new IsAvailableCallback() {
                @Override
                public void onAvailableCallback(boolean isAvailable) {
                    if (isAvailable) {
                        startFragment();
                    }
                }
            });
        } else {
            startFragment();
        }
    }

    private void findChatRoom(final String toUid, final IsAvailableCallback callback) {
        final boolean[] isAvailable = {false};
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("rooms").orderByChild("users/" + getUid()).equalTo("i").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    Map<String, String> users = (Map<String, String>) item.child("users").getValue();

                    if (users.size() == 2 & users.get(toUid) != null) {
                        roomId = item.getKey();
                        isAvailable[0] = true;
                        break;
                    }
                }

                callback.onAvailableCallback(isAvailable[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled findChatRoom: " + databaseError.getMessage());
                callback.onAvailableCallback(isAvailable[0]);
            }
        });
    }

    private void startFragment() {
        ChatFragment chatFragment = ChatFragment.getInstance(toUid, roomId, productKey, productTitle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_fragment, chatFragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        MyFirebaseMessagingService.registerInterlocutor(toUid);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        MyFirebaseMessagingService.unregisterInterlocutor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
