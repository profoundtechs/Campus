package com.profoundtechs.campus;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ActiveFragment extends Fragment {

    //Views
    private RecyclerView rvActive;
    private View mMainView;
    private RelativeLayout activeRelativeLayout,rlActiveProgressbar,rlNoConnection;
    private AdView adActive;

    //Firebase
    private DatabaseReference mActiveDatabase;
    private FirebaseAuth mAuth;
    private Query mUsersQuery;
    private String mCurrentUserId;

    //Variables


    public ActiveFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_active, container, false);

        //AdView
        adActive = (AdView)mMainView.findViewById(R.id.adActive);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adActive.loadAd(adRequest);

        //Initializing Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        //mActiveDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
//        mActiveDatabase.keepSynced(true);

        //Binding layouts
        activeRelativeLayout = (RelativeLayout) mMainView.findViewById(R.id.activeRelativeLayout);
        rlActiveProgressbar = (RelativeLayout) mMainView.findViewById(R.id.rlActiveProgressbar);
        rlNoConnection = (RelativeLayout) mMainView.findViewById(R.id.rlActiveNoConnection);
        rvActive = (RecyclerView)mMainView.findViewById(R.id.rvActive);
        rvActive.setHasFixedSize(true);
        rvActive.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mActiveDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersQuery = mActiveDatabase.orderByChild("online").equalTo("true");

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected){

                    //Shows the progressbar until the list loads
                    rlNoConnection.setVisibility(View.INVISIBLE);
                    rlActiveProgressbar.setVisibility(View.VISIBLE);

                    //Firebase recycler adapter
                    FirebaseRecyclerAdapter<Users,ActiveFragment.ActiveViewHolder> activeRecyclerViewAdapter=new FirebaseRecyclerAdapter<Users, ActiveViewHolder>(
                            Users.class,
                            R.layout.layout_single_user,
                            ActiveFragment.ActiveViewHolder.class,
                            mUsersQuery
                    ) {
                        @Override
                        protected void populateViewHolder(final ActiveFragment.ActiveViewHolder viewHolder, Users model, int position) {

                            //Set the name of the user
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

                            //Setting the thumb image of the user
                            viewHolder.setUserImage(model.getThumb_image(),getActivity());

                            //Getting the id of the user under consideration in the list
                            final String user_id=getRef(position).getKey();

                            //Displaying the online notification green circle if the user is online
                            mActiveDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
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

                            //Event called when an item in the list is clicked
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                    profileIntent.putExtra("user_id",user_id);
                                    startActivity(profileIntent);
                                }
                            });

                            //Displays the list if the list is not null, otherwise displays the no active user textview
                            if (mUsersQuery!=null){
                                rvActive.setVisibility(View.VISIBLE);
                                rlActiveProgressbar.setVisibility(View.INVISIBLE);
                                activeRelativeLayout.setVisibility(View.INVISIBLE);
                                rlNoConnection.setVisibility(View.INVISIBLE);
                            } else {
                                rvActive.setVisibility(View.INVISIBLE);
                                activeRelativeLayout.setVisibility(View.VISIBLE);
                                rlActiveProgressbar.setVisibility(View.INVISIBLE);
                                rlNoConnection.setVisibility(View.INVISIBLE);
                            }
                        }
                    };

                    rvActive.setAdapter(activeRecyclerViewAdapter);

                } else {
                    //Sets no connection to be displayed if the app is offline
                    rvActive.setVisibility(View.INVISIBLE);
                    activeRelativeLayout.setVisibility(View.INVISIBLE);
                    rlActiveProgressbar.setVisibility(View.INVISIBLE);
                    rlNoConnection.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //View holder class for the recycler view
    public static class ActiveViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public ActiveViewHolder(View itemView) {
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
}
