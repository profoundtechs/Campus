package com.profoundtechs.campus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    //Views
    private Toolbar toolbarUsers;
    private RecyclerView rvUsers;
    private AdView adUsers;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //AdView
        adUsers = (AdView)findViewById(R.id.adUsers);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adUsers.loadAd(adRequest);

        //Inflating toolbar
        toolbarUsers=(Toolbar)findViewById(R.id.toolbarUsers);
        setSupportActionBar(toolbarUsers);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initializing Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mUserRef.keepSynced(true);

        //Inflating recyclerview
        rvUsers=(RecyclerView)findViewById(R.id.rvUsers);
        rvUsers.setHasFixedSize(true);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser!=null) {
//            mUserRef.child("online").setValue("true");
//        }

        FirebaseRecyclerAdapter<Users,UsersActivity.UsersViewHolder> usersRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.layout_single_user,
                UsersActivity.UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(final UsersActivity.UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setDisplayName(model.getName());

                //Code for avoiding incorrect description like "Not yet set at General User"
                String university = model.getUniversity();
                String role = model.getRole();
                String roleAt;
                if (university.equals("General user")||role.equals("Not yet set")||role.equals("General user")){
                    roleAt = university;
                } else {
                    roleAt = model.getRole() + " at " + university;
                }
                viewHolder.setUserRole(roleAt);

                viewHolder.setUserImage(model.getThumb_image(),getApplicationContext());

                final String user_id=getRef(position).getKey();

                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });



            }
        };
        rvUsers.setAdapter(usersRecyclerAdapter);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser!=null) {
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
//        }
//    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setDisplayName(String name) {
            TextView tvSingleUserName=(TextView)mView.findViewById(R.id.tvSingleUserName);
            tvSingleUserName.setText(name);
        }

        public void setUserRole(String role) {
            TextView tvSingleUserRole=(TextView)mView.findViewById(R.id.tvSingleUserRole);
            tvSingleUserRole.setText(role);
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
}
