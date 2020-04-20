package com.shotball.project.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
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
import com.shotball.project.adapters.ProductAdapter;
import com.shotball.project.models.Product;
import com.shotball.project.utils.ViewAnimation;
import com.shotball.project.viewHolders.ProductViewHolder;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "FavoritesFragment";

    private View rootView;
    private Snackbar snackbar;

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Product, ProductViewHolder> mAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager mManager;
    private boolean onUndoClicked;

    @Nullable
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        Log.d(TAG, "onCreateView");

        initToolbar();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = rootView.findViewById(R.id.productGrid);
        recyclerView.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(mManager);


        Query productsQuery = mDatabase.child("products").orderByChild("likes/" + getUid()).equalTo(true);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(productsQuery, new SnapshotParser<Product>() {
                    @NonNull
                    @Override
                    public Product parseSnapshot(@NonNull DataSnapshot snapshot) {
                        Product product = snapshot.getValue(Product.class);
                        product.setKey(snapshot.getKey());
                        return product;
                    }
                })
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(options) {

            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                View view = inflater.inflate(R.layout.item_favourite_product, viewGroup, false);
                return new ProductViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder viewHolder, final int position, @NonNull final Product product) {
                viewHolder.bind(rootView.getContext(), product, null);
                View view = viewHolder.itemView;

                Button openProduct = view.findViewById(R.id.product_open);
                openProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ProductActivity.class);
                        intent.putExtra(ProductActivity.EXTRA_PRODUCT_KEY, product.getKey());
                        startActivity(intent);
                    }
                });

                final CoordinatorLayout container = rootView.findViewById(R.id.favourites_container);
                SwipeDismissBehavior<View> swipeDismissBehavior = new SwipeDismissBehavior<>();
                swipeDismissBehavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);

                final MaterialCardView cardContentLayout = view.findViewById(R.id.product_favourite_card);
                CoordinatorLayout.LayoutParams coordinatorParams =
                        (CoordinatorLayout.LayoutParams) cardContentLayout.getLayoutParams();

                coordinatorParams.setBehavior(swipeDismissBehavior);

                swipeDismissBehavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        ViewAnimation.collapse(cardContentLayout);
                        //cardContentLayout.setVisibility(View.GONE);
                        snackbar = Snackbar.make(container, "Product removed from favorites", Snackbar.LENGTH_SHORT)
                                .setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        resetCard(cardContentLayout, product.getKey());
                                        onUndoClicked = true;
                                    }
                                }).addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        Log.d(TAG, "onDismissed");
                                        super.onDismissed(transientBottomBar, event);
                                        if (!onUndoClicked) {
                                            unLike(product.getKey());
                                        }
                                        onUndoClicked = false;
                                    }
                                });
                        snackbar.setAnchorView(getActivity().findViewById(R.id.bottom_navigation));
                        snackbar.show();
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                        Log.d(TAG, String.valueOf(state));
                        switch (state) {
                            case SwipeDismissBehavior.STATE_DRAGGING:
                                break;
                            case SwipeDismissBehavior.STATE_SETTLING:
                                cardContentLayout.setDragged(true);
                                break;
                            case SwipeDismissBehavior.STATE_IDLE:
                                cardContentLayout.setDragged(false);
                                break;
                            default:
                        }
                    }
                });
            }
        };

        recyclerView.setAdapter(mAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setTitle("Favorites");
        //((AppCompatActivity) mActivity).setSupportActionBar(toolbar);
        //setHasOptionsMenu(true);
    }

    private void resetCard(MaterialCardView cardContentLayout, String key) {
        ViewAnimation.expand(cardContentLayout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) cardContentLayout
                .getLayoutParams();
        cardContentLayout.setAlpha(1.0f);
        cardContentLayout.requestLayout();
    }

    private void unLike(String productKey) {
        Log.d(TAG, "setLike");
        DatabaseReference reference = mDatabase.child("products").child(productKey);

        reference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Product product = mutableData.getValue(Product.class);
                if (product == null) {
                    return Transaction.success(mutableData);
                }

                if (product.likes.containsKey(getUid())) {
                    product.likeCount = product.likeCount - 1;
                    product.likes.remove(getUid());
                    mutableData.setValue(product);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(TAG, "productTransaction:onComplete: " + databaseError);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
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
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    private String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
        }
    }

}