package com.shotball.project.fragments;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.activities.ChatActivity;
import com.shotball.project.interfaces.IsAvailableCallback;
import com.shotball.project.models.ExchangeModel;
import com.shotball.project.viewHolders.ExchangeViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ExchangesFragment extends Fragment {

    private static final String TAG = "ExchangesFragment";

    protected DatabaseReference mDatabase;
    private OnButtonClickListener listener;
    private String roomId;

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

        setupListener();

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
                holder.bind(rootView.getContext(), model, listener);
            }
        };

        mRecycler.setAdapter(mAdapter);
    }

    private void setupListener() {
        listener = new OnButtonClickListener() {
            @Override
            public void acceptButton(ExchangeModel model) {
                model.setStatus(1);
                mDatabase.child("exchanges").child("accepted").child(model.getKey()).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabase.child("exchanges").child("proposed").child(model.getKey()).removeValue();
                    }
                });
            }

            @Override
            public void refuseButton(ExchangeModel model) {
                model.setStatus(2);
                mDatabase.child("exchanges").child("refused").child(model.getKey()).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabase.child("exchanges").child("proposed").child(model.getKey()).removeValue();
                    }
                });
            }

            @Override
            public void messageButton(ExchangeModel model, String exchangeProductTitle) {
                findChatRoom(model.who, new IsAvailableCallback() {
                    @Override
                    public void onAvailableCallback(boolean isAvailable) {
                        if (isAvailable) {
                            openChat(model.who, roomId, "", model.exchange_for, exchangeProductTitle);
                        } else {
                            Map<String, String> selectedUsers = new HashMap<>();
                            selectedUsers.put(getUid(), "i");
                            selectedUsers.put(model.who, "i");
                            final String room_id = mDatabase.child("rooms").push().getKey();

                            mDatabase.child("rooms/" + room_id).child("users").setValue(selectedUsers).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    openChat(model.who, roomId, "", model.exchange_for, exchangeProductTitle);
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void tookPlaceButton(ExchangeModel model) {
                new MaterialAlertDialogBuilder(rootView.getContext())
                        .setTitle("Exchange completed")
                        .setMessage("Are you sure that the exchange took place?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNeutralButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        };
    }

    private void findChatRoom(final String toUid, final IsAvailableCallback callback) {
        final boolean[] isAvailable = {false};
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

    private void openChat(String uid, String username, String image, String productKey, String prductTitle) {
        Intent intent = new Intent(rootView.getContext(), ChatActivity.class);
        intent.putExtra("toUid", uid);
        intent.putExtra("roomID", roomId);
        intent.putExtra("roomTitle", username);
        intent.putExtra("roomImage", image);
        intent.putExtra("productKey", productKey);
        intent.putExtra("productTitle", prductTitle);
        startActivity(intent);
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

    public interface OnButtonClickListener {
        void acceptButton(ExchangeModel model);
        void refuseButton(ExchangeModel model);
        void messageButton(ExchangeModel model, String exchangeProductTitle);
        void tookPlaceButton(ExchangeModel model);
    }

}
