package com.profoundtechs.campus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    //Views
    private Toolbar toolbarSearch;
    private RecyclerView rvSearch;
    private EditText etSearchText;
    private RelativeLayout rlSearchNoConnection,rlSearchNoResult;
    private ProgressBar pbSearch;
    private AdView adSearch;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private Query searchQuery;

    //Variables
    private String searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //AdView
        adSearch = (AdView)findViewById(R.id.adSearch);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("2BC6E04CB887FA7970F7F0FF851B9EDB").build();
        adSearch.loadAd(adRequest);

        //Inflating toolbar
        toolbarSearch=(Toolbar)findViewById(R.id.toolbarSearch);
        setSupportActionBar(toolbarSearch);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Binding views
        etSearchText = (EditText)findViewById(R.id.etSearchText);
        pbSearch = (ProgressBar)findViewById(R.id.pbSearch);
        rlSearchNoConnection = (RelativeLayout)findViewById(R.id.rlSearchNoConnection);
        rlSearchNoResult = (RelativeLayout)findViewById(R.id.rlSearchNoResult);


        //Initializing Firebase
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //Inflating recyclerview
        rvSearch=(RecyclerView)findViewById(R.id.rvSearch);
        rvSearch.setHasFixedSize(true);
        rvSearch.setLayoutManager(new LinearLayoutManager(this));

        etSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean connected = dataSnapshot.getValue(Boolean.class);
                        if (connected){

                            searchText = etSearchText.getText().toString();

                            if (!searchText.equals("")){
                                //Shows the progressbar until the list loads
                                rvSearch.setVisibility(View.INVISIBLE);
                                rlSearchNoResult.setVisibility(View.INVISIBLE);
                                pbSearch.setVisibility(View.VISIBLE);
                                rlSearchNoConnection.setVisibility(View.INVISIBLE);

                                searchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
                                FirebaseRecyclerAdapter<Users,SearchActivity.SearchViewHolder> searchRecyclerAdapter=new FirebaseRecyclerAdapter<Users, SearchViewHolder>(
                                        Users.class,
                                        R.layout.layout_single_user,
                                        SearchActivity.SearchViewHolder.class,
                                        searchQuery
                                ) {
                                    @Override
                                    protected void populateViewHolder(final SearchActivity.SearchViewHolder viewHolder, Users model, int position) {

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
                                                Intent profileIntent=new Intent(SearchActivity.this,ProfileActivity.class);
                                                profileIntent.putExtra("user_id",user_id);
                                                startActivity(profileIntent);
                                            }
                                        });

                                    }

                                    @Override
                                    public void onDataChanged() {
                                        super.onDataChanged();
                                        //Displays the list if the list is not null, otherwise displays no result to be displayed
                                        pbSearch.setVisibility(View.INVISIBLE);
                                        searchQuery.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getChildrenCount()==0){
                                                    rlSearchNoResult.setVisibility(View.VISIBLE);
                                                }else {
                                                    rvSearch.setVisibility(View.VISIBLE);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                };
                                rvSearch.setAdapter(searchRecyclerAdapter);
                            } else {
                                //Sets no result to be displayed if there is no search result
                                rvSearch.setVisibility(View.INVISIBLE);
                                rlSearchNoResult.setVisibility(View.VISIBLE);
                                pbSearch.setVisibility(View.INVISIBLE);
                                rlSearchNoConnection.setVisibility(View.INVISIBLE);
                            }

                        } else {
                            //Sets no connection to be displayed if there is no connection
                            rvSearch.setVisibility(View.INVISIBLE);
                            rlSearchNoResult.setVisibility(View.INVISIBLE);
                            pbSearch.setVisibility(View.INVISIBLE);
                            rlSearchNoConnection.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser!=null) {
//            mUserRef.child("online").setValue("true");
//        }


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

    public static class SearchViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public SearchViewHolder(View itemView) {
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
