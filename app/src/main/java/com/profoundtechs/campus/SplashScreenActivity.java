package com.profoundtechs.campus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView ivSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ivSplash = (ImageView) findViewById(R.id.ivSplash);

        Animation animSplash = AnimationUtils.loadAnimation(this,R.anim.anim_splash);
        ivSplash.startAnimation(animSplash);

        final Thread trdSplash = new Thread(){
            public void run(){
                try{
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                    finish();
                }
            }
        };
        trdSplash.start();
    }
}
