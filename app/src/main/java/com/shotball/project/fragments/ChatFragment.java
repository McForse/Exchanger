package com.shotball.project.fragments;

import android.annotation.SuppressLint;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
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
import com.shotball.project.models.ChatModel;
import com.shotball.project.models.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import static com.shotball.project.activities.MainActivity.location;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private View rootView;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private LinearLayoutManager linearLayoutManager;
    private EditText messageInput;
    private ImageView sendButton;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private DateFormat dateFormat;

    private String roomID;
    private String myUid;
    private String toUid;
    private Map<String, User> usersList = new HashMap<>();
    private int usersCount = 0;

    public static ChatFragment getInstance(String toUid, String roomID) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("toUid", toUid);
        bundle.putString("roomID", roomID);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        initComponents();

        /*
         two user: roomid or uid talking
         multi user: roomid
         */
        if (!"".equals(toUid) && toUid != null) { // find existing room for two user
            Log.d(TAG, "First option");
            findChatRoom(toUid);
        } else if (!"".equals(roomID) && roomID != null) { // existing room (multi user)
            Log.d(TAG, "Second option");
            usersCount = 2;
            setChatRoom(roomID);
        }

        if (roomID == null) { // new room for two user
            Log.d(TAG, "Third option");
            usersCount = 2;
            getUserInfoFromServer(myUid);
            getUserInfoFromServer(toUid);
        }

        return rootView;
    }

    private void initComponents() {
        recyclerView = rootView.findViewById(R.id.chat_recyclerView);
        linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        messageInput = rootView.findViewById(R.id.msg_input);
        sendButton = rootView.findViewById(R.id.btn_send);
        sendButton.setOnClickListener(sendButtonClickListener);

        if (getArguments() != null) {
            roomID = getArguments().getString("roomID");
            toUid = getArguments().getString("toUid");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage  = FirebaseStorage.getInstance().getReference();

        dateFormat = android.text.format.DateFormat.getDateFormat(getContext());

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    void getUserInfoFromServer(String id) {
        Log.d(TAG, "getUserInfoFromServer: " + id);
        mDatabase.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.setUid(dataSnapshot.getKey());
                usersList.put(user.getUid(), user);

                if (roomID != null & usersCount == usersList.size()) {
                    Log.d(TAG, "recyclerView.setAdapter");
                    mAdapter = new RecyclerViewAdapter();
                    recyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled getUserInfoFromServer: " + databaseError.getMessage());
            }
        });
    }

    void findChatRoom(final String toUid) {
        mDatabase.child("rooms").orderByChild("users/" + myUid).equalTo("i").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    Map<String, String> users = (Map<String, String>) item.child("users").getValue();

                    if (users.size() == 2 & users.get(toUid) != null) {
                        setChatRoom(item.getKey());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled findChatRoom: " + databaseError.getMessage());
            }
        });
    }

    void setChatRoom(String rid) {
        roomID = rid;
        mDatabase.child("rooms").child(roomID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item: dataSnapshot.getChildren()) {
                    //usersCount++;
                    getUserInfoFromServer(item.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled setChatRoom: " + databaseError.getMessage());
            }
        });
    }

    private Button.OnClickListener sendButtonClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            String message = messageInput.getText().toString();
            sendMessage(message, "0");
            messageInput.setText("");
        }
    };

    private void sendMessage(String msg, String msgtype) {
        sendButton.setEnabled(false);

        if (roomID == null) {
            Log.w(TAG, "Chat has been deleted from the database");
            Objects.requireNonNull(getActivity()).finish();
        }

        ChatModel.Message messages = new ChatModel.Message();
        messages.uid = myUid;
        messages.msg = msg;
        messages.msgtype= msgtype;
        messages.timestamp = ServerValue.TIMESTAMP;

        // save last message
        mDatabase.child("rooms").child(roomID).child("lastmessage").setValue(messages);

        // save message
        messages.readUsers.put(myUid, true);
        mDatabase.child("rooms").child(roomID).child("messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
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
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(90));

        List<ChatModel.Message> messagesList;
        String beforeDay;
        MessageViewHolder beforeViewHolder;

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
                        final ChatModel.Message message = item.getValue(ChatModel.Message.class);

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
            ChatModel.Message message = messagesList.get(position);
            if (myUid.equals(message.uid) ) {
                switch (message.msgtype) {
                    case "1": return R.layout.item_chatproduct_right;
                    default:  return R.layout.item_chatmsg_right;
                }
            } else {
                switch (message.msgtype) {
                    case "1": return R.layout.item_chatproduct_left;
                    default:  return R.layout.item_chatmsg_left;
                }
            }
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
            final ChatModel.Message message = messagesList.get(position);

            setReadCounter(position, messageViewHolder.read_counter);

            String day = dateFormat.format( new Date((long) message.timestamp));
            String timestamp = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date((long) message.timestamp));
            messageViewHolder.timestamp.setText(timestamp);
            if ("0".equals(message.msgtype)) {                                      // text message
                messageViewHolder.msg_item.setText(message.msg);
            } else if ("2".equals(message.msgtype)) {                                      // file transfer
                //TODO
            }

            if (! myUid.equals(message.uid)) {
                User userModel = usersList.get(message.uid);
                messageViewHolder.msg_name.setText(userModel.getUsername());

                if (userModel.getImage() == null) {
                    Glide.with(rootView.getContext()).load(R.drawable.ic_account_circle)
                            .apply(requestOptions)
                            .into(messageViewHolder.user_photo);
                } else {
                    Glide.with(rootView.getContext())
                            .load(userModel.getImage())
                            .apply(requestOptions)
                            .into(messageViewHolder.user_photo);
                }
            }

            messageViewHolder.divider.setVisibility(View.INVISIBLE);
            messageViewHolder.divider.getLayoutParams().height = 0;

            if (position==0) {
                messageViewHolder.divider_date.setText(day);
                messageViewHolder.divider.setVisibility(View.VISIBLE);
                messageViewHolder.divider.getLayoutParams().height = 60;
            };
            if (!day.equals(beforeDay) && beforeDay!=null) {
                beforeViewHolder.divider_date.setText(beforeDay);
                beforeViewHolder.divider.setVisibility(View.VISIBLE);
                beforeViewHolder.divider.getLayoutParams().height = 60;
            }
            beforeViewHolder = messageViewHolder;
            beforeDay = day;
        }

        void setReadCounter (final int pos, final TextView textView) {
            /*int cnt = usersList.size() - messagesList.get(pos).readUsers.size();
            if (cnt > 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(String.valueOf(cnt));
            } else {
                textView.setVisibility(View.INVISIBLE);
            }*/
        }

        @Override
        public int getItemCount() {
            return messagesList.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            ImageView user_photo;
            TextView msg_item;
            TextView msg_name;
            TextView timestamp;
            TextView read_counter;
            LinearLayout divider;
            TextView divider_date;

            MessageViewHolder(View view) {
                super(view);
                user_photo = view.findViewById(R.id.user_photo);
                msg_item = view.findViewById(R.id.msg_item);
                timestamp = view.findViewById(R.id.timestamp);
                msg_name = view.findViewById(R.id.msg_name);
                read_counter = view.findViewById(R.id.read_counter);
                divider = view.findViewById(R.id.divider);
                divider_date = view.findViewById(R.id.divider_date);
            }
        }
    }

    public void backPressed() {
        if (valueEventListener!=null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}