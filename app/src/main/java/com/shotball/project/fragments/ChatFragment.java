package com.shotball.project.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shotball.project.R;
import com.shotball.project.Utils.TextUtil;
import com.shotball.project.activities.ProductActivity;
import com.shotball.project.models.Message;
import com.shotball.project.models.User;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private View rootView;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private LinearLayoutManager linearLayoutManager;
    private EditText messageInput;
    private ImageView sendButton;

    private DatabaseReference mDatabase;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private DateFormat dateFormat;

    private String roomID;
    private String myUid;
    private String toUid;
    private String productKey;
    private String productTitle;
    private Map<String, User> usersList = new HashMap<>();

    public static ChatFragment getInstance(String toUid, String roomID, String productKey, String productTitle) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("toUid", toUid);
        bundle.putString("roomID", roomID);
        bundle.putString("productKey", productKey);
        bundle.putString("productTitle", productTitle);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        initComponents();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (!toUid.equals("") && !roomID.equals("")) {
            getUserInfoFromServer(myUid);
            getUserInfoFromServer(toUid);
        }

        mAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);

        if (productKey != null && productTitle != null) {
            sendProduct(productKey, productTitle);
        }

        return rootView;
    }

    private void initComponents() {
        recyclerView = rootView.findViewById(R.id.chat_recyclerView);
        linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        messageInput = rootView.findViewById(R.id.message_input);
        sendButton = rootView.findViewById(R.id.btn_send);
        sendButton.setOnClickListener(sendButtonClickListener);

        if (getArguments() != null) {
            roomID = getArguments().getString("roomID");
            toUid = getArguments().getString("toUid");
            productKey = getArguments().getString("productKey");
            productTitle = getArguments().getString("productTitle");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
    }

    void getUserInfoFromServer(String id) {
        Log.d(TAG, "getUserInfoFromServer: " + id);
        mDatabase.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.setUid(dataSnapshot.getKey());
                usersList.put(user.getUid(), user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled getUserInfoFromServer: " + databaseError.getMessage());
            }
        });
    }

    private Button.OnClickListener sendButtonClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            String message = messageInput.getText().toString();
            sendMessage(message);
            messageInput.setText("");
        }
    };

    private void sendProduct(String key, String title) {
        Message message = new Message();
        message.uid = key;
        message.msg = title;
        message.msgtype = 1;
        message.timestamp = ServerValue.TIMESTAMP;

        sendMessageToDatabase(message);
    }

    private void sendMessage(String msg) {
        if (msg.equals("")) {
            return;
        }

        sendButton.setEnabled(false);

        Message message = new Message();
        message.uid = myUid;
        message.msg = msg;
        message.msgtype = 0;
        message.timestamp = ServerValue.TIMESTAMP;

        sendMessageToDatabase(message);
    }

    private void sendMessageToDatabase(Message message) {
        if (roomID == null) {
            Objects.requireNonNull(getActivity()).finish();
            return;
        }

        // save last message
        mDatabase.child("rooms").child(roomID).child("lastmessage").setValue(message);

        // save message
        message.readUsers.put(myUid, true);
        mDatabase.child("rooms").child(roomID).child("messages").push().setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //sendGCM();
                sendButton.setEnabled(true);
            }
        });

        // inc unread message count
        mDatabase.child("rooms").child(roomID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    final String uid = item.getKey();

                    if (!myUid.equals(item.getKey())) {
                        mDatabase.child("rooms").child(roomID).child("unread").child(item.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Integer count = dataSnapshot.getValue(Integer.class);
                                if (count == null) count = 0;
                                mDatabase.child("rooms").child(roomID).child("unread").child(uid).setValue(count + 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled sendMessage: " + databaseError.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled sendMessage: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_MESSAGE_ME = 100;
        private final int VIEW_MESSAGE_YOU = 200;
        private final int VIEW_PRODUCT = 2;

        private List<Message> messagesList;
        private String beforeDay;
        private MessageViewHolder beforeViewHolder;

        RecyclerViewAdapter() {
            messagesList = new ArrayList<>();
            startListening();
        }

        private void startListening() {
            beforeDay = null;
            messagesList.clear();

            // get messages from server
            databaseReference = mDatabase.child("rooms").child(roomID).child("messages");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    beforeDay = null;
                    messagesList.clear();

                    // Update number of messages unread to 0 => read all
                    mDatabase.child("rooms").child(roomID).child("unread").child(myUid).setValue(0);
                    Map<String, Object> unreadMessages = new HashMap<>();

                    for (DataSnapshot item: dataSnapshot.getChildren()) {
                        final Message message = item.getValue(Message.class);

                        if (!message.readUsers.containsKey(myUid)) {
                            message.readUsers.put(myUid, true);
                            unreadMessages.put(item.getKey(), message);
                        }

                        messagesList.add(message);
                    }

                    if (unreadMessages.size() > 0) {
                        // Marks read about unread messages
                        mDatabase.child("rooms").child(roomID).child("messages").updateChildren(unreadMessages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyDataSetChanged();
                                recyclerView.scrollToPosition(messagesList.size() - 1);
                            }
                        });
                    } else {
                        notifyDataSetChanged();
                        recyclerView.scrollToPosition(messagesList.size() - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled startListening: " + databaseError.getMessage());
                }
            });
        }

        private void stopListening() {
            if (valueEventListener != null) {
                mDatabase.removeEventListener(valueEventListener);
            }

            messagesList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messagesList.get(position);

            if (message.msgtype == 0) {
                if (message.uid.equals(myUid)) {
                    return VIEW_MESSAGE_ME;
                } else {
                    return VIEW_MESSAGE_YOU;
                }
            } else {
                return VIEW_PRODUCT;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_MESSAGE_ME) {
                View v = inflater.inflate(R.layout.item_chatmsg_me, parent, false);
                vh = new MessageViewHolder(v);
            } else if (viewType == VIEW_MESSAGE_YOU) {
                View v = inflater.inflate(R.layout.item_chatmsg_you, parent, false);
                vh = new MessageViewHolder(v);
            } else {
                View v = inflater.inflate(R.layout.item_chatproduct, parent, false);
                vh = new MessageProductViewHolder(v);
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final Message message = messagesList.get(position);

            if (holder instanceof MessageViewHolder) {
                final MessageViewHolder view = (MessageViewHolder) holder;
                String day = dateFormat.format(new Date((long) message.timestamp));
                setReadCounter(position, view.read_counter);
                view.divider.setVisibility(View.GONE);

                if (position == 0 || position == 1 & getItemViewType(0) == VIEW_PRODUCT) {
                    view.divider_date.setText(day);
                    view.divider.setVisibility(View.VISIBLE);
                }

                if (!day.equals(beforeDay) && beforeDay != null) {
                    beforeViewHolder.divider_date.setText(beforeDay);
                    beforeViewHolder.divider.setVisibility(View.VISIBLE);
                }
                beforeViewHolder = view;
                beforeDay = day;

                view.bind(message);
            } else {
                MessageProductViewHolder view = (MessageProductViewHolder) holder;
                view.bind(message);
            }
        }

        void setReadCounter(int position, ImageView imageView) {
            int cnt = usersList.size() - messagesList.get(position).readUsers.size();
            if (cnt > 0) {
                imageView.setColorFilter(rootView.getContext().getColor(R.color.unread_message));
            } else {
                imageView.setColorFilter(rootView.getContext().getColor(R.color.colorPrimary));
            }
        }

        @Override
        public int getItemCount() {
            return messagesList.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            private TextView msg_item;
            private TextView timestamp;
            private ImageView read_counter;
            private LinearLayout divider;
            private TextView divider_date;

            MessageViewHolder(View view) {
                super(view);
                msg_item = view.findViewById(R.id.msg_item);
                timestamp = view.findViewById(R.id.timestamp);
                read_counter = view.findViewById(R.id.read_counter);
                divider = view.findViewById(R.id.divider);
                divider_date = view.findViewById(R.id.divider_date);
            }

            public void bind(final Message message) {
                String day = dateFormat.format(new Date((long) message.timestamp));
                String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date((long) message.timestamp));
                timestamp.setText(time);
                msg_item.setText(message.msg);
            }
        }

        private class MessageProductViewHolder extends RecyclerView.ViewHolder {
            private TextView title;
            private ImageView image;
            private Button openButton;

            MessageProductViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.product_message_title);
                image = view.findViewById(R.id.product_message_image);
                openButton = view.findViewById(R.id.product_open_button);
            }

            public void bind(final Message message) {
                title.setText(message.msg);

                mDatabase.child("products").child(message.uid).child("images/0").addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String imageUrl = (String) dataSnapshot.getValue();
                        if (TextUtil.isUrl(imageUrl)) {
                            //TODO: placeholder and error
                            Glide.with(rootView.getContext()).load(imageUrl).centerCrop().into(image);
                        } else {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + message.uid + "/" + imageUrl);
                            Glide.with(rootView.getContext()).load(storageReference).centerCrop().into(image);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled MessageProductViewHolder: " + databaseError.getMessage());
                    }
                });

                openButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ProductActivity.class);
                        intent.putExtra(ProductActivity.EXTRA_PRODUCT_KEY, message.uid);
                        startActivity(intent);
                    }
                });
            }
        }
    }

}