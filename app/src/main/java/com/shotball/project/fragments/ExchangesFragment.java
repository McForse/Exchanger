package com.shotball.project.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.shotball.project.R;
import com.shotball.project.models.ExchangeModel;
import com.shotball.project.viewHolders.ExchangeViewHolder;

import java.util.Objects;

public abstract class ExchangesFragment extends Fragment {

    private static final String TAG = "ExchangesFragment";

    protected DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<ExchangeModel, ExchangeViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private View rootView;

    public ExchangesFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_offers, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = rootView.findViewById(R.id.offers_recycler_view);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        Query offersQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<ExchangeModel>()
                .setQuery(offersQuery, new SnapshotParser<ExchangeModel>() {
                    @NonNull
                    @Override
                    public ExchangeModel parseSnapshot(@NonNull DataSnapshot snapshot) {
                        ExchangeModel exchange = snapshot.getValue(ExchangeModel.class);
                        if (exchange != null) {
                            exchange.setKey(snapshot.getKey());
                        }
                        return Objects.requireNonNull(exchange);
                    }
                })
                .build();

        mAdapter = new FirebaseRecyclerAdapter<ExchangeModel, ExchangeViewHolder>(options) {
            @Override
            public ExchangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_exchange, parent, false);

                return new ExchangeViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ExchangeViewHolder holder, int position, @NonNull ExchangeModel model) {
                Log.d(TAG, model.what_exchange);
                holder.bind(rootView.getContext(), model);
            }
        };

        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
        }
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
