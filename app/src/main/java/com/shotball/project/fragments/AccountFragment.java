package com.shotball.project.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.models.Product;
import com.shotball.project.models.User;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";

    private DatabaseReference mDatabase;
    private User mUser;
    private DatabaseReference refUsers;
    private ValueEventListener dataListener;

    private ImageView userImageView;
    private TextView userNameField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setReferences();

        userImageView = rootView.findViewById(R.id.account_image);
        userNameField = rootView.findViewById(R.id.account_name);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getUser();
    }

    private void setReferences() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        refUsers = mDatabase.child("users");
    }

    private void getUser() {
        dataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                mUser = dataSnapshot.getValue(User.class);
                initUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError);
            }
        };

        refUsers.child(getUid()).addListenerForSingleValueEvent(dataListener);
    }

    private void initUser() {
        stopDataListener();

        if (mUser != null) {
            //TODO: placeholder and error
            Glide.with(this)
                    .load(mUser.image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImageView);
            userNameField.setText(mUser.username);
        } else {
            //TODO: error
        }
    }

    private void stopDataListener() {
        mDatabase.removeEventListener(dataListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDataListener();
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
