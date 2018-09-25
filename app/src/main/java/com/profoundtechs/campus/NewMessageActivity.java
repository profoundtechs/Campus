package com.profoundtechs.campus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewMessageActivity extends AppCompatActivity {

    //Views
    private Toolbar toolbarNewMessage;
    private RecyclerView rvNewMessage;
    private RelativeLayout newMessageRelativeLayout;
    private AdView adNewMessage;

    //Firebase
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        //AdView
        adNewMessage = (AdView)findViewById(R.id.adNewMessage);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adNewMessage.loadAd(adRequest);

        toolbarNewMessage=(Toolbar)findViewById(R.id.toolbarNewMessage);
        setSupportActionBar(toolbarNewMessage);
        getSupportActionBar().setTitle("New Message");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        newMessageRelativeLayout = (RelativeLayout) findViewById(R.id.newMessageRelativeLayout);

        rvNewMessage=(RecyclerView)findViewById(R.id.rvNewMessage);
        rvNewMessage.setHasFixedSize(true);
        rvNewMessage.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,NewMessageActivity.NewMessageViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, NewMessageActivity.NewMessageViewHolder>(
                Friends.class,
                R.layout.layout_single_user,
                NewMessageActivity.NewMessageViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final NewMessageActivity.NewMessageViewHolder viewHolder, Friends model, int position) {

                final String friendshipDate= DateFormat.getDateInstance().format(model.getDate());
                viewHolder.setDate(friendshipDate);

                final String listUserId = getRef(position).getKey();

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.setName(userName);
                        viewHolder.setUserImage(userThumb,NewMessageActivity.this);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chatIntent=new Intent(NewMessageActivity.this,ChatActivity.class);
                                chatIntent.putExtra("user_id",listUserId);
                                chatIntent.putExtra("user_name",userName);
                                startActivity(chatIntent);

                            }
                        });

                        mFriendsDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot!=null){
                                    rvNewMessage.setVisibility(View.VISIBLE);
                                    newMessageRelativeLayout.setVisibility(View.INVISIBLE);
                                }else {
                                    rvNewMessage.setVisibility(View.INVISIBLE);
                                    newMessageRelativeLayout.setVisibility(View.VISIBLE);
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
        rvNewMessage.setAdapter(firebaseRecyclerAdapter);
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

    public static class NewMessageViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public NewMessageViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date){
            String fSince = "Friends since " + date;
            TextView friendsSince = (TextView) mView.findViewById(R.id.tvSingleUserRole);
            friendsSince.setText(fSince);
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.tvSingleUserName);
            userNameView.setText(name);
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
