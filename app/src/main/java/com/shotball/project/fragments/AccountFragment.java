package com.shotball.project.fragments;

import android.content.DialogInterface;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.activities.ExchangeActivity;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.models.Product;
import com.shotball.project.models.User;
import com.shotball.project.utils.ViewAnimation;
import com.shotball.project.viewHolders.MyProductViewHolder;

public class AccountFragment extends BaseFragment {

    private static final String TAG = "AccountFragment";

    private View rootView;
    private CoordinatorLayout mainContainer;
    private ImageView userImageView;
    private TextView exhibitCount;
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
        exhibitCount = rootView.findViewById(R.id.exhibit_count);
        exchangesCount = rootView.findViewById(R.id.exchanges_count);

        LinearLayout proposedExchangesButton = rootView.findViewById(R.id.proposed_exchanges_button);
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
            Glide.with(this)
                    .load(mUser.getImage())
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImageView);
            exhibitCount.setText(String.valueOf(mUser.getExhibited()));
            exchangesCount.setText(String.valueOf(mUser.getExchanges()));
            ViewAnimation.showIn(mainContainer);
            initRecycler();
        }
    }

    private void initRecycler() {
        Query query = mDatabase.child("products").orderByChild("user").equalTo(getUid());

        FirebaseRecyclerOptions<Product> options =
                new FirebaseRecyclerOptions.Builder<Product>()
                        .setQuery(query, new SnapshotParser<Product>() {
                            @NonNull
                            @Override
                            public Product parseSnapshot(@NonNull DataSnapshot snapshot) {
                                Product product = snapshot.getValue(Product.class);

                                if (product != null) {
                                    product.setKey(snapshot.getKey());
                                }

                                return product;
                            }
                        })
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Product, MyProductViewHolder>(options) {
            @NonNull
            @Override
            public MyProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_my_product, parent, false);

                return new MyProductViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyProductViewHolder holder, int position, @NonNull Product model) {
                holder.bind(rootView.getContext(), model, new OnProductSelectedListener() {
                    @Override
                    public void onProductSelected(Product product) {
                        Intent intent = new Intent(getActivity(), ProductActivity.class);
                        intent.putExtra(ProductActivity.EXTRA_PRODUCT_KEY, product.getKey());
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClicked(String productKey) {
                        new MaterialAlertDialogBuilder(rootView.getContext())
                                .setTitle(R.string.delete_product)
                                .setMessage(R.string.delte_product_confirmation)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteProduct(productKey);
                                        decProducts(model.getUser());
                                    }
                                })
                                .show();
                    }
                });
            }
        };

        recyclerView = rootView.findViewById(R.id.my_goods_recycler);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        mAdapter.startListening();
    }

    private void deleteProduct(String productKey) {
        mDatabase.child("products").child(productKey).removeValue().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar snackbar = Snackbar.make(mainContainer, R.string.delete_product_error, Snackbar.LENGTH_LONG);
                snackbar.setAnchorView(getActivity().findViewById(R.id.bottom_navigation));
            }
        });
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

    public interface OnProductSelectedListener {
        void onProductSelected(Product product);
        void onDeleteClicked(String productKey);
    }
}
