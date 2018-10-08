package com.profoundtechs.campus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Initializing Firebase
    FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    //Initializing views
    private Toolbar toolbarMain;
    private TabLayout tabMain;
    private ViewPager viewPagerMain;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private CircleImageView ivNavMainPic;
    private TextView tvNavMainName;
    private TextView tvNavMainRole;

    String name,university,role,department,level,dob;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize admob sdk
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        //Toolbar
        toolbarMain=(Toolbar)findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbarMain);
        getSupportActionBar().setTitle("Campus");

        //Getting database references if a user is logged in
        mAuth =FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            mUserRef.keepSynced(true);
        }

        //Tabs at the top
        tabMain=(TabLayout)findViewById(R.id.tabMain);
        viewPagerMain=(ViewPager)findViewById(R.id.viewPagerMain);
        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());
        viewPagerMain.setAdapter(mSectionsPagerAdapter);
        tabMain.setupWithViewPager(viewPagerMain);

        //Floating action button which displays the new chat activity
        FloatingActionButton fabMain = (FloatingActionButton) findViewById(R.id.fabMain);
        fabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newMessageIntent=new Intent(MainActivity.this,NewMessageActivity.class);
                startActivity(newMessageIntent);
            }
        });

        //Navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbarMain, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Views in the navigation header
        View header = navigationView.getHeaderView(0);
        ivNavMainPic = (CircleImageView)header.findViewById(R.id.ivNavMainPic);
        tvNavMainName = (TextView)header.findViewById(R.id.tvNavMainName);
        tvNavMainRole = (TextView) header.findViewById(R.id.tvNavMainRole);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        if (currentUser==null){
            sendToStart();
        } else {

            //This will put the image, name and university on the navigation bar
            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name=dataSnapshot.child("name").getValue().toString();
                    university=dataSnapshot.child("university").getValue().toString();
                    role=dataSnapshot.child("role").getValue().toString();
                    department=dataSnapshot.child("department").getValue().toString();
                    level=dataSnapshot.child("level").getValue().toString();
                    dob=dataSnapshot.child("dob").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();

                    //Loads image using picasso with offline capability
                    if (!image.equals("default_image")){
                        //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_image).into(ivSettingsPic);
                        Picasso.with(MainActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_image).into(ivNavMainPic, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.default_image).into(ivNavMainPic);
                            }
                        });
                    }

                    tvNavMainName.setText(dataSnapshot.child("name").getValue().toString());

                    //Code for avoiding incorrect description like "Not yet set at General User"
                    String roleAt;
                    if (university.equals("General user")||role.equals("Not yet set")||role.equals("General user")){
                        roleAt = university;
                    } else {
                        roleAt = dataSnapshot.child("role").getValue().toString() + " at " + university;
                    }
                    tvNavMainRole.setText(roleAt);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    connected = dataSnapshot.getValue(Boolean.class);
                    if (connected){
                        mUserRef.child("online").setValue("true");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser!=null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        Intent startIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.searchMain){
            startActivity(new Intent(MainActivity.this,SearchActivity.class));
        }
        if (item.getItemId()==R.id.logoutMain){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Are you sure?");
            CharSequence confirmation[] = new CharSequence[]{"Yes Logout", "Cancel"};
            builder.setItems(confirmation, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i==0){
                        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                        FirebaseAuth.getInstance().signOut();
                        sendToStart();
                    }
                }
            });
            builder.show();
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_friends) {
            Intent friendsIntent=new Intent(MainActivity.this,FriendsActivity.class);
            startActivity(friendsIntent);
        } else if (id == R.id.nav_all_users) {
            Intent allUsersIntent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(allUsersIntent);
        } else if (id == R.id.nav_account) {
            Intent accountIntent=new Intent(MainActivity.this,SettingsActivity.class);
//            accountIntent.putExtra("Name",name);
//            accountIntent.putExtra("University",university);
//            accountIntent.putExtra("Role",role);
//            accountIntent.putExtra("Department",department);
//            accountIntent.putExtra("Level",level);
//            accountIntent.putExtra("Dob",dob);
            startActivity(accountIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
