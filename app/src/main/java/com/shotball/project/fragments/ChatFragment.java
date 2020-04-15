package com.shotball.project.fragments;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.shotball.project.R;
import com.shotball.project.adapters.ProductAdapter;
import com.shotball.project.models.Message;
import com.shotball.project.models.User;
import com.shotball.project.viewHolders.ProductViewHolder;

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
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (!toUid.equals("") && !roomID.equals("")) {
            usersCount = 2;
            getUserInfoFromServer(myUid);
            getUserInfoFromServer(toUid);
        }

        mAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);

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

                if (roomID != null & usersCount == usersList.size()) {
                    Log.d(TAG, "recyclerView.setAdapter");

                }
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
            sendMessage(message, 0);
            messageInput.setText("");
        }
    };

    private void sendMessage(String msg, int msgtype) {
        sendButton.setEnabled(false);

        if (roomID == null) {
            Log.w(TAG, "Chat has been deleted from the database");
            Objects.requireNonNull(getActivity()).finish();
        }

        Message messages = new Message();
        messages.uid = myUid;
        messages.msg = msg;
        messages.msgtype = msgtype;
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

        private final int VIEW_MESSAGE = 0;
        private final int VIEW_PRODUCT = 1;

        List<Message> messagesList;
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

            if (message.msgtype == VIEW_MESSAGE) {
                if (message.uid.equals(myUid)) {
                    return R.layout.item_chatmsg_right;
                } else {
                    return R.layout.item_chatmsg_left;
                }
            } else {
                return R.layout.item_chatproduct;
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
            final Message message = messagesList.get(position);

            setReadCounter(position, messageViewHolder.read_counter);

            String day = dateFormat.format( new Date((long) message.timestamp));
            String timestamp = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date((long) message.timestamp));
            messageViewHolder.timestamp.setText(timestamp);
            if ("0".equals(message.msgtype)) {                                      // text message
                messageViewHolder.msg_item.setText(message.msg);
            } else if ("2".equals(message.msgtype)) {                                      // file transfer
                //TODO
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
            TextView msg_item;
            TextView timestamp;
            TextView read_counter;
            LinearLayout divider;
            TextView divider_date;

            MessageViewHolder(View view) {
                super(view);
                msg_item = view.findViewById(R.id.msg_item);
                timestamp = view.findViewById(R.id.timestamp);
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