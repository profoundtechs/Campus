package com.profoundtechs.campus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    Button btnStartLogin;
    TextView tvStartNewAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnStartLogin=(Button)findViewById(R.id.btnStartLogin);
        tvStartNewAccount=(TextView) findViewById(R.id.tvStartNewAccount);

        btnStartLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        tvStartNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }
}
