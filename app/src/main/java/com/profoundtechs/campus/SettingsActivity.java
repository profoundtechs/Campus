package com.profoundtechs.campus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    //Initializing views
    ImageView ivSettingsPic;
    TextView tvSettingsName,tvSettingsUniversity,tvSettingsRole;
    TextView tvSettingsDepartment,tvSettingsLevel,tvSettingsDob;
    ImageButton ibSettingsName, ibSettingsDatePicker;
    FloatingActionButton fabSettingsChangePic;
    private ProgressDialog mProgressDialog;
    LinearLayout llSettingsUniversity, llSettingsRole,llSettingsDepartment,llSettingsLevel;

    //Date picker
    private Calendar calendar;
    private int year, month, day;

    //For changing profile picture
    private static final int GALLERY_PICK=1;

    //Initializing firebase
    private StorageReference mImageStorage;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Connecting views
        ivSettingsPic=(ImageView)findViewById(R.id.ivSettingsPic);
        tvSettingsName=(TextView)findViewById(R.id.tvSettingsName);
        tvSettingsUniversity=(TextView)findViewById(R.id.tvSettingsUniversity);
        tvSettingsRole=(TextView)findViewById(R.id.tvSettingsRole);
        tvSettingsDepartment=(TextView)findViewById(R.id.tvSettingsDepartment);
        tvSettingsLevel=(TextView)findViewById(R.id.tvSettingsLevel);
        tvSettingsDob=(TextView)findViewById(R.id.tvSettingsDob);
        ibSettingsName=(ImageButton)findViewById(R.id.ibSettingsName);
        ibSettingsDatePicker=(ImageButton)findViewById(R.id.ibSettingsDatePicker);
        fabSettingsChangePic=(FloatingActionButton)findViewById(R.id.fabSettingsChangePic);
        llSettingsUniversity = (LinearLayout)findViewById(R.id.llSettingsUniversity);
        llSettingsRole = (LinearLayout)findViewById(R.id.llSettingsRole);
        llSettingsDepartment = (LinearLayout)findViewById(R.id.llSettingsDepartment);
        llSettingsLevel = (LinearLayout)findViewById(R.id.llSettingsLevel);

        //Date picker
        ibSettingsDatePicker = (ImageButton)findViewById(R.id.ibSettingsDatePicker);
        tvSettingsDob = (TextView)findViewById(R.id.tvSettingsDob);
        calendar=Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        //Code for displaying details of a user from the database
        mImageStorage=FirebaseStorage.getInstance().getReference();
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=mCurrentUser.getUid();
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mUserDatabase.keepSynced(true);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String university=dataSnapshot.child("university").getValue().toString();
                String role=dataSnapshot.child("role").getValue().toString();
                String department=dataSnapshot.child("department").getValue().toString();
                String level=dataSnapshot.child("level").getValue().toString();
                String dob=dataSnapshot.child("dob").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();

                //Setting values to views
                tvSettingsName.setText(name);
                tvSettingsUniversity.setText(university);
                tvSettingsRole.setText(role);
                tvSettingsDepartment.setText(department);
                tvSettingsLevel.setText(level);
                tvSettingsDob.setText(dob);
                if (!image.equals("default_image")){
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_image).into(ivSettingsPic);
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_image).into(ivSettingsPic, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_image).into(ivSettingsPic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Starts gallery. Code for selecting pic, putting to database and displaying is on the on onActivityResult
        fabSettingsChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(galleryIntent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        //Name ImageButton click listener
        ibSettingsName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                final EditText editText = new EditText(SettingsActivity.this);
                editText.setHint("Name");
                builder.setTitle("Your Name");
                builder.setView(editText);
                builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!editText.getText().toString().equals("")){
                            tvSettingsName.setText(editText.getText());
                            mUserDatabase.child("name").setValue(editText.getText().toString());
                        }
                    }
                });
                builder.show();
            }
        });

        //University layout click listener
        llSettingsUniversity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select University");
                final CharSequence universities[] = new CharSequence[]{
                        "General user","AASTU","AAU","AGU","AMU","ARU","ASTU","ASU","AXU","BDU","DBU","DMU",
                        "DTU","DU","DDU","GU","HMU","HU","JJU","JU","MTU","MU","MWU","SU","WDU","WLU","WKU","WU"
                };
                builder.setItems(universities, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tvSettingsUniversity.setText(universities[i]);
                        mUserDatabase.child("university").setValue(universities[i]);
                    }
                });
                builder.show();
            }
        });

        //Role layout click listener
        llSettingsRole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select Role");
                final CharSequence roles[] = new CharSequence[]{
                        "General user","Teacher","Student"
                };
                builder.setItems(roles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tvSettingsRole.setText(roles[i]);
                        mUserDatabase.child("role").setValue(roles[i]);
                    }
                });
                builder.show();
            }
        });

        //Department layout click listener
        llSettingsDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select Department");
                final CharSequence departments[] = new CharSequence[]{
                        "General user","Accounting","Animal Production","Architecture","Biology",
                        "Biomedical Sciences","Biotechnology","Chemical Engineering","Chemistry",
                        "Civil Engineering","Computer Science","CoTM","Economics","Electrical Engineering",
                        "English","Environmental Engineering","Environmental Sciences","Geography",
                        "Geology","History","Horticulture","Law","Management","Marketing","Mathematics",
                        "Mechanical Engineering","Medicine","Midwifery","NReM","Nursing","Pharmacy",
                        "Physics","Plant Sciences","Political Science","Psychology","Public Health",
                        "Sociology","Sport Science","Statistics","Surgery"
                };
                builder.setItems(departments, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tvSettingsDepartment.setText(departments[i]);
                        mUserDatabase.child("department").setValue(departments[i]);
                    }
                });
                builder.show();
            }
        });

        //Level layout click listener
        llSettingsLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select Level");
                final CharSequence levelsStudent[] = new CharSequence[]{
                        "General user","1st Year","2nd Year","3rd Year","4th Year","5th Year"
                };
                final CharSequence levelsTeacher[] = new CharSequence[]{
                        "General user","BSc","MSc","PhD"
                };
                if (tvSettingsRole.getText().toString().equals("Student")){
                    builder.setItems(levelsStudent, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tvSettingsLevel.setText(levelsStudent[i]);
                            mUserDatabase.child("level").setValue(levelsStudent[i]);
                        }
                    });
                    builder.show();
                }
                if (tvSettingsRole.getText().toString().equals("Teacher")){
                    builder.setItems(levelsTeacher, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tvSettingsLevel.setText(levelsTeacher[i]);
                            mUserDatabase.child("level").setValue(levelsTeacher[i]);
                        }
                    });
                    builder.show();
                }
            }
        });

        //Date picker edit button click listener
        ibSettingsDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(999);
            }
        });

//        //Changing settings and submitting data to firebase database
//        btnSettingsChange.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String name=tvSettingsName.getText().toString();
//                String university=tvSettingsUniversity.getText().toString();
//                String role=tvSettingsRole.getText().toString();
//                String department=tvSettingsDepartment.getText().toString();
//                String level=tvSettingsLevel.getText().toString();
//                String dob=tvSettingsDob.getText().toString();
//                Intent changeSettingsIntent=new Intent(SettingsActivity.this, ChangeSettingsActivity.class);
//                changeSettingsIntent.putExtra("Name",name);
//                changeSettingsIntent.putExtra("University",university);
//                changeSettingsIntent.putExtra("Role",role);
//                changeSettingsIntent.putExtra("Department",department);
//                changeSettingsIntent.putExtra("Level",level);
//                changeSettingsIntent.putExtra("Dob",dob);
//                startActivity(changeSettingsIntent);
//            }
//        });

        //        btnSettingsChangeImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                /*// start picker to get image for cropping and then use the image in cropping activity
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this);*/
//
//                Intent galleryIntent=new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(galleryIntent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
//            }
//        });
//

//       RecyclerView rvSettings = findViewById(R.id.rvSettings);
//        rvSettings.setLayoutManager(new LinearLayoutManager(SettingsActivity.this));
//        adapter = new SettingsRecyclerAdapter(SettingsActivity.this,settingsData);
//        rvSettings.setAdapter(adapter);
//        rvSettings.setItemAnimator(new DefaultItemAnimator());
//        rvSettings.addItemDecoration(new DividerItemDecoration(SettingsActivity.this,LinearLayoutManager.HORIZONTAL));

//        String name=getIntent().getStringExtra("Name");
//        String university=getIntent().getStringExtra("University");
//        String role=getIntent().getStringExtra("Role");
//        String department=getIntent().getStringExtra("Department");
//        String level=getIntent().getStringExtra("Level");
//        String dob=getIntent().getStringExtra("Dob");

//        ArrayList<String> settingsData = new ArrayList<>();
//        settingsData.add(name);
//        settingsData.add(university);
//        settingsData.add(role);
//        settingsData.add(department);
//        settingsData.add(level);
//        settingsData.add(dob);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
            //Toast.makeText(this, imageUri, Toast.LENGTH_SHORT).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog=new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait until the image is uploaded...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumbFile=new File(resultUri.getPath());

                String currentUid=mCurrentUser.getUid();

                Bitmap thumbBitmap=new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumbFile);

                final ByteArrayOutputStream baos=new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumbByte=baos.toByteArray();

                StorageReference filePath=mImageStorage.child("profile_images").child(currentUid+".jpg");
                final StorageReference thumbFilePath=mImageStorage.child("profile_images").child("thumbs").child(currentUid+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String downloadUrl=task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask=thumbFilePath.putBytes(thumbByte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                    String thumbDownloadUrl=thumbTask.getResult().getDownloadUrl().toString();

                                    if (thumbTask.isSuccessful()){

                                        Map updateHashMap=new HashMap();
                                        updateHashMap.put("image",downloadUrl);
                                        updateHashMap.put("thumb_image",thumbDownloadUrl);

                                        mUserDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "Image successfully uploaded!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Problem occurred while uploading thumbnail", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(SettingsActivity.this, "Problem occurred while uploading", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    //Creating date picker dialog
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id==999){
            return new DatePickerDialog(this,myDateListener,year,month,day);
        }
        return null;
    }

    //Actions done when date is picked
    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(i,i1,i2,0,0,0);
            Date selectedDate = cal.getTime();
            DateFormat dateFormatUK = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
            String dateUK = dateFormatUK.format(selectedDate);
            tvSettingsDob.setText(dateUK);
            mUserDatabase.child("dob").setValue(tvSettingsDob.getText());
        }
    };


}
