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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    //Views
    private Toolbar toolbarFriends;
    private RecyclerView rvFriends;
    private RelativeLayout friendsRelativeLayout;
    private AdView adFriends;

    //Firebase
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    //Variables
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //AdView
        adFriends = (AdView)findViewById(R.id.adFriends);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adFriends.loadAd(adRequest);

        //Setting toolbar
        toolbarFriends=(Toolbar)findViewById(R.id.toolbarFriends);
        setSupportActionBar(toolbarFriends);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initializing Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        //Binding views
        friendsRelativeLayout = (RelativeLayout) findViewById(R.id.friendsRelativeLayout);
        rvFriends=(RecyclerView)findViewById(R.id.rvFriends);
        rvFriends.setHasFixedSize(true);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Firebase recycler adapter
        FirebaseRecyclerAdapter<Friends,FriendsActivity.FriendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsActivity.FriendsViewHolder>(
                Friends.class,
                R.layout.layout_single_user,
                FriendsActivity.FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsActivity.FriendsViewHolder viewHolder, Friends model, int position) {

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
                        viewHolder.setUserImage(userThumb,FriendsActivity.this);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open profile","Send message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setTitle("Select Options");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i==0){
                                            Intent profileIntent=new Intent(FriendsActivity.this,ProfileActivity.class);
                                            profileIntent.putExtra("user_id",listUserId);
                                            startActivity(profileIntent);
                                        }

                                        if (i==1){
                                            Intent chatIntent=new Intent(FriendsActivity.this,ChatActivity.class);
                                            chatIntent.putExtra("user_id",listUserId);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });

                                builder.show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        rvFriends.setAdapter(firebaseRecyclerAdapter);

        mFriendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    rvFriends.setVisibility(View.VISIBLE);
                    friendsRelativeLayout.setVisibility(View.INVISIBLE);
                }else {
                    rvFriends.setVisibility(View.INVISIBLE);
                    friendsRelativeLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public FriendsViewHolder(View itemView) {
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
