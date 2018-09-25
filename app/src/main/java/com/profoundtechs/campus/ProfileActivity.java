package com.profoundtechs.campus;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    //Views
    private ImageView ivProfilePic;
    private TextView tvProfileDisplayName,tvProfileUniversity,tvProfileRole;
    private TextView tvProfileDepartment,tvProfileLevel,tvProfileDob;
    private Button btnProfileSendFriendRequest,btnProfileDeclineFriendRequest;
    private ProgressDialog mProgressDialog;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;

    //Variables
    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Getting the user id sent from users activity with the intent
        final String user_id=getIntent().getStringExtra("user_id");

        //Firebase initialization
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("Notifications");
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mAuth = FirebaseAuth.getInstance();

        //Binding views
        ivProfilePic=(ImageView)findViewById(R.id.ivProfilePic);
        tvProfileDisplayName=(TextView)findViewById(R.id.tvProfileDisplayName);
        tvProfileUniversity=(TextView)findViewById(R.id.tvProfileUniversity);
        tvProfileRole=(TextView)findViewById(R.id.tvProfileRole);
        tvProfileDepartment=(TextView)findViewById(R.id.tvProfileDepartment);
        tvProfileLevel=(TextView)findViewById(R.id.tvProfileLevel);
        tvProfileDob=(TextView)findViewById(R.id.tvProfileDob);


        btnProfileSendFriendRequest=(Button)findViewById(R.id.btnProfileSendFriendRequest);
        btnProfileDeclineFriendRequest=(Button)findViewById(R.id.btnProfileDeclineFriendRequest);

        //Default conditions
        mCurrentState="not_friends";
        btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
        btnProfileDeclineFriendRequest.setEnabled(false);

        //Showing progress dialog
        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while loading user data...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        //Controls the status of the friend request and decline buttons and creates and deletes different databases
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName=dataSnapshot.child("name").getValue().toString();
                String university=dataSnapshot.child("university").getValue().toString();
                String role=dataSnapshot.child("role").getValue().toString();
                String department=dataSnapshot.child("department").getValue().toString();
                String departmentFull="Department of " + dataSnapshot.child("department").getValue().toString();
                String level=dataSnapshot.child("level").getValue().toString();
                String dob=dataSnapshot.child("dob").getValue().toString();
                String dobFull="Born in " + dataSnapshot.child("dob").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                //Displaying values checking first if they are null
                tvProfileDisplayName.setText(displayName);
                tvProfileUniversity.setText(university);
                if (!role.equals("General user")||!role.equals("Not yet set")){
                    tvProfileRole.setText(role);
                }
                if (!department.equals("Not yet set")||!department.equals("General user")){
                    tvProfileDepartment.setText(departmentFull);
                }
                if (!level.equals("Not yet set")||!level.equals("General user")){
                    tvProfileLevel.setText(level);
                }
                //There may be a better condition than this
                if (!dob.equals("Not yet set")||!dob.equals("")){
                    tvProfileDob.setText(dobFull);
                }
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_image).into(ivProfilePic);

                //*********************FRIENDS LIST/ REQUEST FEATURE**********************
                //*********************This is initial state******************************
                if (mCurrentUser.getUid().equals(user_id)){
                    btnProfileSendFriendRequest.setVisibility(View.INVISIBLE);
                    btnProfileSendFriendRequest.setEnabled(false);
                    btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                    btnProfileDeclineFriendRequest.setEnabled(false);
                    mProgressDialog.dismiss();
                } else {
                    mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild(user_id)){
                                String reqType=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                                if (reqType.equals("received")){
                                    mCurrentState="req_received";
                                    btnProfileSendFriendRequest.setText("Accept Friend Request");
                                    btnProfileSendFriendRequest.setBackgroundColor(Color.BLUE);

                                    btnProfileDeclineFriendRequest.setVisibility(View.VISIBLE);
                                    btnProfileDeclineFriendRequest.setEnabled(true);
                                    btnProfileDeclineFriendRequest.setBackgroundColor(Color.RED);
                                } else if (reqType.equals("sent")){
                                    mCurrentState="req_sent";
                                    btnProfileSendFriendRequest.setText("Cancel Friend Request");
                                    btnProfileSendFriendRequest.setBackgroundColor(Color.RED);

                                    btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                    btnProfileDeclineFriendRequest.setEnabled(false);
                                }
                                mProgressDialog.dismiss();
                            } else {
                                mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(user_id)){
                                            mCurrentState="friends";
                                            btnProfileSendFriendRequest.setText("Unfriend this Person");
                                            btnProfileSendFriendRequest.setBackgroundColor(Color.RED);

                                            btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                            btnProfileDeclineFriendRequest.setEnabled(false);
                                        }
                                        mProgressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        mProgressDialog.dismiss();
                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnProfileSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnProfileSendFriendRequest.setEnabled(false);
                // ******************NOT FRIENDS STATE********************
                if (mCurrentState.equals("not_friends")){
                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String,String> notificationsData=new HashMap<>();
                    notificationsData.put("from",mCurrentUser.getUid());
                    notificationsData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type","received");
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationsData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCurrentState="req_sent";
                                btnProfileSendFriendRequest.setText("Cancel Friend Request");
                                btnProfileSendFriendRequest.setBackgroundColor(Color.RED);

                                btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnProfileDeclineFriendRequest.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,
                                        Toast.LENGTH_SHORT).show();
                            }
                            btnProfileSendFriendRequest.setEnabled(true);
                        }
                    });
                }

                // ******************CANCEL REQUEST STATE********************
                if (mCurrentState.equals("req_sent")){

                    Map cancelMap = new HashMap();
                    cancelMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(),null);
                    cancelMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id,null);

                    mRootRef.updateChildren(cancelMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCurrentState="not_friends";
                                btnProfileSendFriendRequest.setText("Send Friend Request");
                                btnProfileSendFriendRequest.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                                btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnProfileDeclineFriendRequest.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,
                                        Toast.LENGTH_SHORT).show();
                            }
                            btnProfileSendFriendRequest.setEnabled(true);
                        }
                    });
                }

                //*********************REQ RECEIVED STATE************************
                if (mCurrentState.equals("req_received")){

                    //This is where the date is formatted which will be inserted in the friends database
                    //final String currentDate= DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", ServerValue.TIMESTAMP);
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", ServerValue.TIMESTAMP);

                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(),null);
                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id,null);

                    friendsMap.put("Notifications/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCurrentState="friends";
                                btnProfileSendFriendRequest.setText("Unfriend this Person");
                                btnProfileSendFriendRequest.setBackgroundColor(Color.RED);

                                btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnProfileDeclineFriendRequest.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,
                                        Toast.LENGTH_SHORT).show();
                            }
                            btnProfileSendFriendRequest.setEnabled(true);
                        }
                    });

                }

                // ******************UNFRIEND STATE********************
                if (mCurrentState.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCurrentState="not_friends";
                                btnProfileSendFriendRequest.setText("Send Friend Request");
                                btnProfileSendFriendRequest.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                                btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnProfileDeclineFriendRequest.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,
                                        Toast.LENGTH_SHORT).show();
                            }
                            btnProfileSendFriendRequest.setEnabled(true);
                        }
                    });
                }


            }
        });

        btnProfileDeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ******************CANCEL REQUEST STATE********************
                if (mCurrentState.equals("req_received")){

                    Map rejectMap = new HashMap();
                    rejectMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(),null);
                    rejectMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id,null);

                    mRootRef.updateChildren(rejectMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCurrentState="not_friends";
                                btnProfileSendFriendRequest.setText("Send Friend Request");
                                btnProfileSendFriendRequest.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                                btnProfileDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnProfileDeclineFriendRequest.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,
                                        Toast.LENGTH_SHORT).show();
                            }
                            btnProfileSendFriendRequest.setEnabled(true);
                        }
                    });
                }
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

    @Override
    protected void onStop() {
        super.onStop();

//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser!=null) {
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
//        }

    }
}
