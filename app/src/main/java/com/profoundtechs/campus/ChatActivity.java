package com.profoundtechs.campus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    //Views
    private Toolbar toolbarChat;
    private TextView tvChatBarDisplayName;
    private TextView tvChatBarLastSeen;
    private CircleImageView ivChatBar;
    private ImageButton ibChatAdd;
    private ImageButton ibChatSend;
    private EditText etChatMessage;
    private RecyclerView rvChatMessageList;
    private SwipeRefreshLayout srlChat;
    private LinearLayoutManager mLinearLayout;
    private AdView adChat;

    //Firebase
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    //Variables
    public static String mChatUser;
    private String userName;
    private MessageAdapter mAdapter;
    private final List<Messages> messagesList = new ArrayList<>();
    private static final int TOTAL_ITEMS_TO_LOAD = 30;
    private int mCurrentPage = 1;
    private int itemPos;
    private String mLastKey = "";
    private String mPrevKey = "";
    long prevMessageDate[]=new long[2];
//    private long prevMessageDate2;
//    private static final int GALLERY_PICK = 1;
//    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //AdView
        adChat = (AdView)findViewById(R.id.adChat);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adChat.loadAd(adRequest);

        //Getting the id and name of the chat receiver from the previous activity
        mChatUser = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");

        //Firebase
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
//        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Displaying the toolbar
        toolbarChat = (Toolbar) findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbarChat);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(userName);
        //Displaying the header of the chat activity at the toolbar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_chat_bar,null);
        actionBar.setCustomView(view);
        //--------------- CUSTOM ACTION BAR ITEMS---------------------
        tvChatBarDisplayName = (TextView)findViewById(R.id.tvChatBarDisplayName);
        tvChatBarLastSeen = (TextView)findViewById(R.id.tvChatBarLastSeen);
        ivChatBar = (CircleImageView)findViewById(R.id.ivChatBar);

        //Initializing the views used for displaying message
        mAdapter = new MessageAdapter(messagesList);
        ibChatAdd = (ImageButton) findViewById(R.id.ibChatAdd);
        ibChatSend = (ImageButton) findViewById(R.id.ibChatSend);
        etChatMessage = (EditText) findViewById(R.id.etChatMessage);
        rvChatMessageList = (RecyclerView) findViewById(R.id.rvChatMessageList);
        srlChat = (SwipeRefreshLayout) findViewById(R.id.srlChat);
        mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setStackFromEnd(true);
        rvChatMessageList.setHasFixedSize(true);
        rvChatMessageList.setLayoutManager(mLinearLayout);
        rvChatMessageList.setAdapter(mAdapter);

        //Loading messages
        loadMessages();

        //Filling the views in the toolbar
        tvChatBarDisplayName.setText(userName);
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                if (online.equals("true")) {
                    tvChatBarLastSeen.setText("Online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    tvChatBarLastSeen.setText(lastSeenTime);
                }

                if (!image.equals("default_image")){
                    Picasso.with(ChatActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_image).into(ivChatBar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.default_image).into(ivChatBar);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //This adds the chat receiver and the current user in their respective chat lists, if they were not before
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError!=null){
                                Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //This sends the message
        ibChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        //Loads the next page in the pagination when refreshed
        srlChat.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });

        //Getting date of the last two messages
        Query lastMessageQuery = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).limitToLast(2);
        lastMessageQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i =0;
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    String time = data.child("time").getValue().toString();
                    prevMessageDate[i] = Long.parseLong(time);
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //A method for loading more messages when more pages are loaded
    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(30);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos==1){
                    mLastKey = messageKey;
                }

                mAdapter.notifyDataSetChanged();

                srlChat.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(30,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //Method for loading message
    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if (itemPos==1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                rvChatMessageList.smoothScrollToPosition(messagesList.size()-1);

                srlChat.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Method that sends message
    private void sendMessage() {
        String message = etChatMessage.getText().toString();

        //Check if the message edit text is not empty and send
        if (!TextUtils.isEmpty(message)) {
            String currentUserRef = "Messages/" + mCurrentUserId + "/" + mChatUser;
            String chatUserRef = "Messages/" + mChatUser + "/" + mCurrentUserId;

            Map messageMap1 = new HashMap();
            messageMap1.put("type", "tempo");
            messageMap1.put("time", ServerValue.TIMESTAMP);

            //Generate random key to reference for the message & date message and getting the push ids
            DatabaseReference userMessagePush1 = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).push();
            String pushId1 = userMessagePush1.getKey();

            //This will prepare the message entry
            Map messageUserMap1 = new HashMap();
            messageUserMap1.put(currentUserRef + "/" + pushId1,messageMap1);
            messageUserMap1.put(chatUserRef + "/" + pushId1,messageMap1);

            //This will put the message entry to the database
//            mRootRef.updateChildren(messageUserMap1, new DatabaseReference.CompletionListener() {
//                @Override
//                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                    if (databaseError!=null){
//                        Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });

            //Hash map for the main text message
            Map messageMap2 = new HashMap();
            messageMap2.put("message",message);
            messageMap2.put("seen", "false");
            messageMap2.put("type", "text");
            messageMap2.put("time", ServerValue.TIMESTAMP);
            messageMap2.put("from", mCurrentUserId);

            Map messageUserMap2 = new HashMap();

            if (!DateFormat.getDateInstance(DateFormat.SHORT).format(prevMessageDate[0])
                    .equals(DateFormat.getDateInstance(DateFormat.SHORT).format(prevMessageDate[1]))){

                Map dateMap = new HashMap();
                dateMap.put("message","date");
                dateMap.put("seen", "false");
                dateMap.put("type", "date");
                dateMap.put("time", ServerValue.TIMESTAMP);
                dateMap.put("from", mCurrentUserId);

                //Generate random key to reference for the message & date message and getting the push ids
                DatabaseReference userDateMessagePush = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).push();
                String datePushId = userDateMessagePush.getKey();
                DatabaseReference userMessagePush2 = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).push();
                String pushId2 = userMessagePush2.getKey();

                //This will prepare the date and message entries
//                messageUserMap2.put(currentUserRef + "/" + pushId1,null);
//                messageUserMap2.put(chatUserRef + "/" + pushId1,null);
                messageUserMap2.put(currentUserRef + "/" + datePushId,dateMap);
                messageUserMap2.put(currentUserRef + "/" + pushId2,messageMap2);
                messageUserMap2.put(chatUserRef + "/" + datePushId,dateMap);
                messageUserMap2.put(chatUserRef + "/" + pushId2,messageMap2);

            } else {

                //Generate random key to reference for the message and getting the push id
                DatabaseReference userMessagePush2 = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser).push();
                String pushId2 = userMessagePush2.getKey();

                //This will prepare the date and message entries
                messageUserMap2.put(currentUserRef + "/" + pushId1,null);
                messageUserMap2.put(chatUserRef + "/" + pushId1,null);
                messageUserMap2.put(currentUserRef + "/" + pushId2,messageMap2);
                messageUserMap2.put(chatUserRef + "/" + pushId2,messageMap2);
            }

            //This will make the text input box empty after the message is sent
            etChatMessage.setText("");

            //This will put the date and message entries to the database
            mRootRef.updateChildren(messageUserMap2, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError!=null){
                        Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Makes any unseen messages seen
        final Query unseenMessagesQuery = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser)
                .orderByChild("seen").equalTo("false");
        unseenMessagesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //here is your every post
                    key = snapshot.getKey();
                }

                if (dataSnapshot.exists()&&dataSnapshot.child(key).child("from").getValue().toString().equals(mChatUser)){
//                    dataSnapshot.getRef().child(key).child("seen").setValue("true");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final Query unseenMessagesQuery2 = mRootRef.child("Messages").child(mChatUser).child(mCurrentUserId)
                .orderByChild("seen").equalTo("false");
        unseenMessagesQuery2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //here is your every post
                    key = snapshot.getKey();
                }

                if (dataSnapshot.exists()&&dataSnapshot.child(key).child("from").getValue().toString().equals(mChatUser)){
//                    dataSnapshot.getRef().child(key).child("seen").setValue("true");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
