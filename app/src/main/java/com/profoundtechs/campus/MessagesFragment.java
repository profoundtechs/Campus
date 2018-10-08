package com.profoundtechs.campus;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    //Views
    private View mMainView;
    private RecyclerView rvMessages;
    private RelativeLayout messagesRelativeLayout;
    private AdView adMessages;

    //Firebase
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private String lastMessageFrom;
    private boolean lastMessageFromCurrentUser;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_messages, container, false);

        //AdView
        adMessages = (AdView)mMainView.findViewById(R.id.adMessages);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adMessages.loadAd(adRequest);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrentUserId);
        mMessageDatabase.keepSynced(true);

        //Views
        messagesRelativeLayout = (RelativeLayout) mMainView.findViewById(R.id.messagesRelativeLayout);
        rvMessages = (RecyclerView) mMainView.findViewById(R.id.rvMessages);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvMessages.setHasFixedSize(true);
        rvMessages.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Query to display chat list based on timestamp
        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        //Firebase recycler adapter
        FirebaseRecyclerAdapter<Conv,MessagesViewHolder> chatsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Conv, MessagesViewHolder>(
                Conv.class,
                R.layout.layout_single_chat_list,
                MessagesViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final MessagesViewHolder viewHolder, final Conv model, int position) {

                //Getting user id from the database
                final String listUserId = getRef(position).getKey();

                //Getting the last message to display on the chat list
                Query lastMessageQuery = mMessageDatabase.child(listUserId).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.hasChild("message")){
                            String data = dataSnapshot.child("message").getValue().toString();
                            String seen = dataSnapshot.child("seen").getValue().toString();
                            lastMessageFrom = dataSnapshot.child("from").getValue().toString();
                            lastMessageFromCurrentUser = lastMessageFrom.equals(mCurrentUserId);
                            viewHolder.setMessage(data,seen);
                        }

                        //Displaying number of unread messages
                        if (!lastMessageFromCurrentUser){
                            Query unreadMessagesQuery = mMessageDatabase.child(listUserId).orderByChild("seen").equalTo("false");
                            unreadMessagesQuery.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        String uMessages = String.valueOf(dataSnapshot.getChildrenCount());
                                        viewHolder.showUnread(uMessages);
                                    }else {
                                        TextView tvUnreadMessages = (TextView) viewHolder.mView.findViewById(R.id.tvUnreadMessages);
                                        tvUnreadMessages.setVisibility(View.INVISIBLE);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
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

                //Fetching user data from the users database to display on the chat list
                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //Getting the name and thumb image url values from the user database
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        //Displaying whether the user is online or not
                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        //Setting the name and image
                        viewHolder.setName(userName);
                        viewHolder.setUserImage(userThumb,getContext());

                        //Sends do the chat activity when a list item is clicked
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("user_id",listUserId);
                                chatIntent.putExtra("user_name",userName);
                                startActivity(chatIntent);
                            }
                        });

                        //Displays the no chat layout if there is no item in the chat list
                        mConvDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot!=null){
                                    rvMessages.setVisibility(View.VISIBLE);
                                    messagesRelativeLayout.setVisibility(View.INVISIBLE);
                                }else {
                                    rvMessages.setVisibility(View.INVISIBLE);
                                    messagesRelativeLayout.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        rvMessages.setAdapter(chatsRecyclerViewAdapter);
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public MessagesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        //Makes the last message in the chat list bold if not seen and normal if seen
        public void setMessage(String message, String seen){
            TextView tvSingleUserStatus = (TextView) mView.findViewById(R.id.tvSingleUserRole);
            tvSingleUserStatus.setText(message);

            //ToDo the seen status of the message should be made to change
            if (!seen.equals("true")){
                tvSingleUserStatus.setTypeface(tvSingleUserStatus.getTypeface(), Typeface.BOLD);
            } else {
                tvSingleUserStatus.setTypeface(tvSingleUserStatus.getTypeface(),Typeface.NORMAL);
            }
        }

        public void showUnread(String nUnreadMessages){
            TextView tvUnreadMessages = (TextView) mView.findViewById(R.id.tvUnreadMessages);
            tvUnreadMessages.setText(nUnreadMessages);
            tvUnreadMessages.setVisibility(View.VISIBLE);
        }

        public void setName(String name) {
            TextView tvSingleUserName = (TextView) mView.findViewById(R.id.tvSingleUserName);
            tvSingleUserName.setText(name);
        }

        public void setUserImage(String thumbImage, Context context) {
            CircleImageView ivSingleUserPic=(CircleImageView)mView.findViewById(R.id.ivSingleUserPic);
            Picasso.with(context).load(thumbImage).placeholder(R.drawable.default_image).into(ivSingleUserPic);
        }

        public void setUserOnline(String onlineStatus) {
            ImageView ivOnlineStatus = (ImageView) mView.findViewById(R.id.ivOnlineStatus);

            if (onlineStatus.equals("true")) {
                ivOnlineStatus.setVisibility(View.VISIBLE);
            } else {
                ivOnlineStatus.setVisibility(View.INVISIBLE);
            }
        }

    }
}
