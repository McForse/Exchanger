package com.shotball.project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.activities.ExchangeActivity;
import com.shotball.project.models.Product;
import com.shotball.project.models.User;
import com.shotball.project.utils.ViewAnimation;
import com.shotball.project.viewHolders.FavoriteProductViewHolder;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";

    private View rootView;
    private CoordinatorLayout mainContainer;
    private ImageView userImageView;
    private TextView userNameField;
    private TextView exchangesCount;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter mAdapter;

    private DatabaseReference mDatabase;
    private User mUser;
    private DatabaseReference refUsers;
    private ValueEventListener dataListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_account, container, false);
        Log.d(TAG, "onCreateView");
        initComponents();
        setReferences();
        return rootView;
    }

    private void initComponents() {
        mainContainer = rootView.findViewById(R.id.account_fragment);
        mainContainer.setVisibility(View.GONE);
        userImageView = rootView.findViewById(R.id.account_image);
        userNameField = rootView.findViewById(R.id.account_name);
        exchangesCount = rootView.findViewById(R.id.exchanges_count);

        LinearLayout proposedExchangesButton = rootView.findViewById(R.id.proposed_ecxhanges_button);
        proposedExchangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ExchangeActivity.class);
                startActivity(intent);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(mUser.getUsername());
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
        if (mUser != null) {
            initToolbar();
            //TODO: placeholder and error
            Glide.with(this)
                    .load(mUser.getImage())
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImageView);
            //userNameField.setText(mUser.getUsername());
            exchangesCount.setText(String.valueOf(mUser.getExchanges()));

            ViewAnimation.showIn(mainContainer);

            initRecycler();
        } else {
            //TODO: error
        }
    }

    private void initRecycler() {
        Query query = mDatabase.child("products").orderByChild("user").equalTo(getUid());

        FirebaseRecyclerOptions<Product> options =
                new FirebaseRecyclerOptions.Builder<Product>()
                        .setQuery(query, Product.class)
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Product, FavoriteProductViewHolder>(options) {
            @Override
            public FavoriteProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_favourite_product, parent, false);

                return new FavoriteProductViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FavoriteProductViewHolder holder, int position, @NonNull Product model) {
                holder.bind(rootView.getContext(), model, null);
            }
        };

        recyclerView = rootView.findViewById(R.id.my_goods_recycler);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        mAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
        }
    }
}
