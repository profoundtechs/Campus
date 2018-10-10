package com.profoundtechs.campus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbarRegister;
    TextInputLayout tilRegisterName;
    TextInputLayout tilRegisterEmail;
    TextInputLayout tilRegisterPassword;
    Button btnRegisterNewAccount;

    private ProgressDialog progressDialogRegister;

    FirebaseAuth mAuth;

    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        toolbarRegister=(Toolbar)findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbarRegister);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialogRegister=new ProgressDialog(this);

        tilRegisterName=(TextInputLayout)findViewById(R.id.tilRegisterName);
        tilRegisterEmail=(TextInputLayout)findViewById(R.id.tilRegisterEmail);
        tilRegisterPassword=(TextInputLayout)findViewById(R.id.tilRegisterPassword);
        btnRegisterNewAccount=(Button)findViewById(R.id.btnRegisterNewAccount);

        btnRegisterNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName=tilRegisterName.getEditText().getText().toString();
                String email=tilRegisterEmail.getEditText().getText().toString();
                String password=tilRegisterPassword.getEditText().getText().toString();

                //It was originally done as || in the tutorial but should be &&
                //Otherwise the app crashes
                if (!TextUtils.isEmpty(displayName)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                    progressDialogRegister.setTitle("Registering User");
                    progressDialogRegister.setMessage("Please wait while we create your account...");
                    progressDialogRegister.setCanceledOnTouchOutside(false);
                    progressDialogRegister.show();
                    createAccount(displayName,email,password);
                }
            }
        });
    }

    private void createAccount(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=currentUser.getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    myRef=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> userMap=new HashMap<>();
                    userMap.put("name",displayName);
                    userMap.put("dob","Not yet set");
                    userMap.put("image","default_image");
                    userMap.put("thumb_image","default_image");
                    userMap.put("device_token",deviceToken);
                    userMap.put("university","General user");
                    userMap.put("online","true");
                    userMap.put("role","Not yet set");
                    userMap.put("department","Not yet set");
                    userMap.put("level","Not yet set");

                    myRef.setValue(userMap);

                    progressDialogRegister.dismiss();
                    Intent optionalSettingsIntent=new Intent(RegisterActivity.this,OptionalSettingsActivity.class);
                    optionalSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(optionalSettingsIntent);
                    finish();
                }else {
                    progressDialogRegister.hide();
                    Toast.makeText(RegisterActivity.this, "Registration failed. " +
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
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
}
