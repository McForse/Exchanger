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
import androidx.appcompat.app.AppCompatActivity;
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

        setReferences();
        productList = new ArrayList<>();

        mRecycler.setAdapter(mAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        //((AppCompatActivity) mActivity).setSupportActionBar(toolbar);
        //setHasOptionsMenu(true);
    }

    private void setReferences() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        refProducts = mDatabase.child("products");

        Query postsQuery = refProducts;

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(postsQuery, Product.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(options) {

            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new ProductViewHolder(inflater.inflate(R.layout.item_product, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(ProductViewHolder viewHolder, int position, final Product model) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), ProductActivity.class);
                        intent.putExtra(ProductActivity.EXTRA_PRODUCT_KEY, postKey);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.likes.containsKey(getUid())) {
                    viewHolder.like.setImageResource(R.drawable.ic_favorite);
                } else {
                    viewHolder.like.setImageResource(R.drawable.ic_favorite_border);
                }

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(mActivity, model, position, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("products").child(postRef.getKey());

                        // Run two transactions
                        onStarClicked(globalPostRef);
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Product p = mutableData.getValue(Product.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.likeCount = p.likeCount - 1;
                    p.likes.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.likeCount = p.likeCount + 1;
                    p.likes.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "Listener waiting");
        if (mAdapter != null) {
            mAdapter.startListening();
            Log.d(TAG, "Listener start");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}