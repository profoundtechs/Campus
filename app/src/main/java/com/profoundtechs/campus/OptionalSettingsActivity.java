package com.profoundtechs.campus;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OptionalSettingsActivity extends AppCompatActivity {

    Toolbar toolbarOptionalSettings;
    Button btnOptionalSettingsSave,btnOptionalSettingsLater;
    TextView tvOptionalSettingsUniversity,tvOptionalSettingsRole;
    TextView tvOptionalSettingsDepartment,tvOptionalSettingsLevel,tvOptionalSettingsDob;
    ImageButton ibOptionalSettingsDatePicker;
    LinearLayout llOptionalSettingsUniversity, llOptionalSettingsRole,llOptionalSettingsDepartment,llOptionalSettingsLevel;

    //Date picker
    private Calendar calendar;
    private int year, month, day;

    private ProgressDialog mProgress;

    private DatabaseReference mOptionalSettingsDatabase;
    private FirebaseUser mOptionalSettingsUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optional_settings);

        toolbarOptionalSettings=(Toolbar)findViewById(R.id.toolbarOptionalSettings);
        setSupportActionBar(toolbarOptionalSettings);
        getSupportActionBar().setTitle("Academic Info");

        //Date picker
        ibOptionalSettingsDatePicker = (ImageButton)findViewById(R.id.ibOptionalSettingsDatePicker);
        tvOptionalSettingsDob = (TextView)findViewById(R.id.tvOptionalSettingsDob);
        calendar=Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        //Initializing firebase
        mOptionalSettingsUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid=mOptionalSettingsUser.getUid();
        mOptionalSettingsDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        //Connecting views
        tvOptionalSettingsUniversity=(TextView)findViewById(R.id.tvOptionalSettingsUniversity);
        tvOptionalSettingsRole=(TextView)findViewById(R.id.tvOptionalSettingsRole);
        tvOptionalSettingsDepartment=(TextView)findViewById(R.id.tvOptionalSettingsDepartment);
        tvOptionalSettingsLevel=(TextView)findViewById(R.id.tvOptionalSettingsLevel);
        tvOptionalSettingsDob=(TextView)findViewById(R.id.tvOptionalSettingsDob);
        ibOptionalSettingsDatePicker=(ImageButton)findViewById(R.id.ibOptionalSettingsDatePicker);
        llOptionalSettingsUniversity = (LinearLayout)findViewById(R.id.llOptionalSettingsUniversity);
        llOptionalSettingsRole = (LinearLayout)findViewById(R.id.llOptionalSettingsRole);
        llOptionalSettingsDepartment = (LinearLayout)findViewById(R.id.llOptionalSettingsDepartment);
        llOptionalSettingsLevel = (LinearLayout)findViewById(R.id.llOptionalSettingsLevel);
        btnOptionalSettingsSave=(Button) findViewById(R.id.btnOptionalSettingsSave);
        btnOptionalSettingsLater=(Button) findViewById(R.id.btnOptionalSettingsLater);

        //Saving changed settings
        btnOptionalSettingsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(tvOptionalSettingsUniversity.getText())&&!TextUtils.isEmpty(tvOptionalSettingsRole.getText())&&
                !TextUtils.isEmpty(tvOptionalSettingsDepartment.getText()) && !TextUtils.isEmpty(tvOptionalSettingsLevel.getText())&&
                !TextUtils.isEmpty(tvOptionalSettingsDob.getText())) {

                    //Show progress dialog
                    mProgress=new ProgressDialog(OptionalSettingsActivity.this);
                    mProgress.setTitle("Updating Status");
                    mProgress.setMessage("Please wait while your status is updated...");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    //Pass the values given to the users database
                    String university=tvOptionalSettingsUniversity.getText().toString();
                    String role=tvOptionalSettingsRole.getText().toString();
                    String department=tvOptionalSettingsDepartment.getText().toString();
                    String level=tvOptionalSettingsLevel.getText().toString();
                    String dob=tvOptionalSettingsDob.getText().toString();

                    Map changedUserData = new HashMap<>();
                    changedUserData.put("dob",dob);
                    changedUserData.put("university",university);
                    changedUserData.put("role",role);
                    changedUserData.put("department",department);
                    changedUserData.put("level",level);

                    mOptionalSettingsDatabase.updateChildren(changedUserData, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mProgress.dismiss();
                                Intent mainIntent=new Intent(OptionalSettingsActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }else {
                                mProgress.dismiss();
                                String error = databaseError.getMessage();
                                Toast.makeText(OptionalSettingsActivity.this,error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

        //Display the main activity
        btnOptionalSettingsLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent=new Intent(OptionalSettingsActivity.this,MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }
        });

        //University layout click listener
        llOptionalSettingsUniversity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(OptionalSettingsActivity.this);
                builder.setTitle("Select University");
                final CharSequence universities[] = new CharSequence[]{
                        "General user","AASTU","AAU","AGU","AMU","ARU","ASTU","ASU","AXU","BDU","DBU","DMU",
                        "DTU","DU","DDU","GU","HMU","HU","JJU","JU","MTU","MU","MWU","SU","WDU","WLU","WKU","WU"
                };
                builder.setItems(universities, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tvOptionalSettingsUniversity.setText(universities[i]);
                    }
                });
                builder.show();
            }
        });

        //Role layout click listener
        llOptionalSettingsRole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(OptionalSettingsActivity.this);
                builder.setTitle("Select Role");
                final CharSequence roles[] = new CharSequence[]{
                        "General user","Teacher","Student"
                };
                builder.setItems(roles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tvOptionalSettingsRole.setText(roles[i]);
                    }
                });
                builder.show();
            }
        });

        //Department layout click listener
        llOptionalSettingsDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(OptionalSettingsActivity.this);
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
                        tvOptionalSettingsDepartment.setText(departments[i]);
                    }
                });
                builder.show();
            }
        });

        //Level layout click listener
        llOptionalSettingsLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(OptionalSettingsActivity.this);
                builder.setTitle("Select Level");
                final CharSequence levelsStudent[] = new CharSequence[]{
                        "General user","1st Year","2nd Year","3rd Year","4th Year","5th Year"
                };
                final CharSequence levelsTeacher[] = new CharSequence[]{
                        "General user","BSc","MSc","PhD"
                };
                if (tvOptionalSettingsRole.getText().toString().equals("Student")){
                    builder.setItems(levelsStudent, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tvOptionalSettingsLevel.setText(levelsStudent[i]);
                        }
                    });
                    builder.show();
                }
                if (tvOptionalSettingsRole.getText().toString().equals("Teacher")){
                    builder.setItems(levelsTeacher, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            tvOptionalSettingsLevel.setText(levelsTeacher[i]);
                        }
                    });
                    builder.show();
                }
            }
        });

        //Date picker edit button click listener
        ibOptionalSettingsDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(999);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id==999){
            return new DatePickerDialog(this,myDateListener,year,month,day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(i,i1,i2,0,0,0);
            Date selectedDate = cal.getTime();
            DateFormat dateFormatUK = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);
            String dateUK = dateFormatUK.format(selectedDate);
            tvOptionalSettingsDob.setText(dateUK);
        }
    };
}
