package com.shotball.project.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.shotball.project.R;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.models.Product;
import com.shotball.project.viewHolders.ProductViewHolder;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "FavoritesFragment";

    private FirebaseRecyclerAdapter<Product, ProductViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private GridLayoutManager mManager;

    private View rootView;
    private List<Product> productList;

    private Activity mActivity;
    private DatabaseReference mDatabase;
    private DatabaseReference refProducts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        Log.d(TAG, "onCreateView");

        mActivity = getActivity();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initToolbar();

        mRecycler = rootView.findViewById(R.id.productGrid);
        mRecycler.setVisibility(View.GONE);
        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new GridLayoutManager(mActivity, 2);
        mRecycler.setLayoutManager(mManager);

        productList = new ArrayList<>();

        mRecycler.setAdapter(mAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        //((AppCompatActivity) mActivity).setSupportActionBar(toolbar);
        //setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}