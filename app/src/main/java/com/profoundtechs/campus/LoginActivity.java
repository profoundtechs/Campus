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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar toolbarLogin;
    TextInputLayout tilLoginEmail;
    TextInputLayout tilLoginPassword;
    Button btnLoginToAccount;

    private ProgressDialog progressDialogLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");

        toolbarLogin=(Toolbar)findViewById(R.id.toolbarLogin);
        setSupportActionBar(toolbarLogin);
        getSupportActionBar().setTitle("Login Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialogLogin=new ProgressDialog(this);

        tilLoginEmail=(TextInputLayout)findViewById(R.id.tilLoginEmail);
        tilLoginPassword=(TextInputLayout)findViewById(R.id.tilLoginPassword);
        btnLoginToAccount=(Button)findViewById(R.id.btnLoginToAccount);

        btnLoginToAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=tilLoginEmail.getEditText().getText().toString();
                String password=tilLoginPassword.getEditText().getText().toString();

                //It was originally done as || in the tutorial but should be &&
                //Otherwise the app crashes
                if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                    progressDialogLogin.setTitle("Logging in");
                    progressDialogLogin.setMessage("Please wait while we check your credentials...");
                    progressDialogLogin.setCanceledOnTouchOutside(false);
                    progressDialogLogin.show();
                    loginUser(email,password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialogLogin.dismiss();

                    String currentUserId=mAuth.getCurrentUser().getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(currentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

                }else {
                    progressDialogLogin.hide();
                    Toast.makeText(LoginActivity.this, "Cannot sign in. Please check the form and try again.", Toast.LENGTH_SHORT).show();
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
