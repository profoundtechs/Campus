package com.profoundtechs.campus;


import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    //Views
    private View mMainView;
    private RecyclerView rvRequests;
    private RelativeLayout requestsRelativeLayout;
    private AdView adRequests;


    //Firebase
    private DatabaseReference mReqDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        //AdView
        adRequests = (AdView)mMainView.findViewById(R.id.adRequests);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adRequests.loadAd(adRequest);

        //Initializing Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentUserId);
        mReqDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        rvRequests = (RecyclerView) mMainView.findViewById(R.id.rvRequests);
        requestsRelativeLayout = (RelativeLayout) mMainView.findViewById(R.id.requestsRelativeLayout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        rvRequests.setHasFixedSize(true);
        rvRequests.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests,RequestsViewHolder> requestsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(
                Requests.class,
                R.layout.layout_single_user,
                RequestsViewHolder.class,
                mReqDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Requests model, int position) {
                final String listUserId = getRef(position).getKey();

                mReqDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(listUserId)){
                            rvRequests.setVisibility(View.VISIBLE);
                            requestsRelativeLayout.setVisibility(View.INVISIBLE);
                            String reqType = dataSnapshot.child(listUserId).child("request_type").getValue().toString();
                            viewHolder.setReqType(reqType);

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
                                    viewHolder.setUserImage(userThumb,getContext());

                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",listUserId);
                                            startActivity(profileIntent);
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {
                            requestsRelativeLayout.setVisibility(View.VISIBLE);
                            rvRequests.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        rvRequests.setAdapter(requestsRecyclerViewAdapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setReqType(String type){
            TextView tvSingleUserStatus = (TextView) mView.findViewById(R.id.tvSingleUserRole);
            tvSingleUserStatus.setText("Friend request "+type);
        }

        public void setName(String name){
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
